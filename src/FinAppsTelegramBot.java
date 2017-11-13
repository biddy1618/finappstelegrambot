import Database.Database;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import retrofit.Attendance;
import retrofit.AttendanceItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by dauren on 1/24/17.
 */
public class FinAppsTelegramBot extends TelegramLongPollingBot {

    private static final String LOGTAG = "TEST_TELEGRAM_FIN_APPS";

    public static class States {
        public static final int STARTSTATE = 0;
        public static final int LOGINSTATE = 1;
        public static final int REGISTERSTATE = 2;
        public static final int MAINMENUSTATE = 3;
    }

    private static Connection connection;

    public FinAppsTelegramBot() {
        super();
        connection = Database.createConnection();
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {

        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText())
                    handleIncommingMessage(message);
            }
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {  botsApi.registerBot(new FinAppsTelegramBot()); }
        catch (TelegramApiException e) { e.printStackTrace(); }
    }

    private void handleIncommingMessage(Message message) throws TelegramApiException {
        assert (message.getFrom() != null);

        int prevState = States.STARTSTATE;

        if (Database.searchUser(connection, message.getFrom().getId())) {
            if (Database.isLogged(connection, message.getFrom().getId())) {
                prevState = States.MAINMENUSTATE;
            }
            else prevState = Database.getState(connection, message.getFrom().getId());
        }else Database.insertUser(connection, message.getFrom().getId());
        SendMessage mess = getMessage(message, prevState);
        sendMessage(mess);
    }

    private SendMessage getMessage(Message message, int prevState) {
        int currState = getNextState(message, prevState);
        ReplyKeyboardMarkup replyKeyboardMarkup = getKeyboard(currState);

        Database.updateState(connection, message.getFrom().getId(), currState);

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(getReplyMessage(message, currState));
        return sendMessage;
    }

    private ReplyKeyboardMarkup getKeyboard(int currState) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow, keyboardSecondRow;

        switch (currState) {
            case States.STARTSTATE:
                keyboardFirstRow = new KeyboardRow();
                keyboardFirstRow.add(Database.Message.getLoginText());
                keyboardFirstRow.add(Database.Message.getRegisterText());
                keyboard.add(keyboardFirstRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                break;
            case States.LOGINSTATE:
                keyboardFirstRow = new KeyboardRow();
                keyboardFirstRow.add(Database.Message.getCancelText());
                keyboard.add(keyboardFirstRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                break;
            case States.REGISTERSTATE:
                keyboardFirstRow = new KeyboardRow();
                keyboardFirstRow.add(Database.Message.getCancelText());
                keyboard.add(keyboardFirstRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                break;
            case States.MAINMENUSTATE:
                keyboardFirstRow = new KeyboardRow();
                keyboardFirstRow.add(Database.Message.getYesterdayText());
                keyboardFirstRow.add(Database.Message.getTodayText());
                keyboard.add(keyboardFirstRow);
                keyboardSecondRow = new KeyboardRow();
                keyboardSecondRow.add(Database.Message.getLogoutText());
                keyboard.add(keyboardSecondRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                break;
            default:

        }

        return replyKeyboardMarkup;
    }

    private int getNextState(Message message, int prevState) {
        int ret = States.STARTSTATE;
        switch (prevState) {
            case States.STARTSTATE:
                if (message.hasText()) {
                    if (message.getText().equals(Database.Message.getLoginText()))
                        ret = States.LOGINSTATE;
                    if (message.getText().equals(Database.Message.getRegisterText()))
                        ret = States.REGISTERSTATE;
                }
                break;
            case States.LOGINSTATE:
                if (message.hasText()) {
                    if (message.getText().equals(Database.Message.getCancelText()))
                        ret = States.STARTSTATE;
                    else {
                        String[] creds = Utils.getCreds(message.getText());
                        if (Database.searchCredts(connection, creds[0], creds[1])) {
                            Database.updateLogged(connection, message.getFrom().getId(), true);
                            ret = States.MAINMENUSTATE;
                        }else ret = States.LOGINSTATE;
                    }
                }
                break;
            case States.REGISTERSTATE:
                if (message.hasText()) {
                    if (message.getText().equals(Database.Message.getCancelText()))
                        ret = States.STARTSTATE;
                    else {
                        String[] creds = Utils.getCreds(message.getText());
                        if (creds[0].equals(Utils.EMPTY) && creds[1].equals(Utils.EMPTY)) {
                            ret = States.STARTSTATE;
                        }else {
                            Database.updateUser(connection, message.getFrom().getId(), creds[0], creds[1]);
                            ret = States.LOGINSTATE;
                        }
                    }
                }
                break;
            case States.MAINMENUSTATE:
                if (message.hasText()) {
                    if (message.getText().equals(Database.Message.getLogoutText())) {
                        Database.updateLogged(connection, message.getFrom().getId(), false);
                        ret = States.STARTSTATE;
                    }
                    else ret = States.MAINMENUSTATE;
                }else ret = States.MAINMENUSTATE;
                break;
            default:
                ret = States.STARTSTATE;
        }
        return ret;
    }

    private String getReplyMessage(Message message, int state) {
        switch (state) {
            case States.STARTSTATE:
                return Database.Message.getStartMessage();
            case States.LOGINSTATE:
                return Database.Message.getLoginMessage();
            case States.REGISTERSTATE:
                return Database.Message.getRegisterMessage();
            case States.MAINMENUSTATE:
                if (message.hasText()) {
                    if (message.getText().equals(Database.Message.getTodayText()))
                        sendReport(true, message);
                    else if (message.getText().equals(Database.Message.getYesterdayText()))
                        sendReport(false, message);
                    return Database.Message.getMainMenuMessage();
                }
                return Database.Message.getMainMenuMessage();
            default:
                return Database.Message.getStartMessage();
        }
    }

    // TODO: work on this
    private void sendReport(boolean today, Message message) {

        String yyyy_mm = Utils.getYYYYMM();
        Attendance service = Attendance.retrofit.create(Attendance.class);
        Call<List<AttendanceItem>> call = service.getAttendance(Database.Message.getMacAldikText(), yyyy_mm);
        call.enqueue(new Callback<List<AttendanceItem>>() {

            @Override
            public void onResponse(Call<List<AttendanceItem>> call, Response<List<AttendanceItem>> response) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId());
                sendMessage.setText(Utils.getData(response.body(), today));
                try{
                    sendMessage(sendMessage);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceItem>> call, Throwable t) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId());
                sendMessage.setText(t.getMessage());
                t.printStackTrace();
                try{
                    sendMessage(sendMessage);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
