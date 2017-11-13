package Database;

import org.telegram.telegrambots.logging.BotLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by dauren on 1/27/17.
 */
public class Database {

    private static final class UsersTable {

        private static final String TABLENAME       = "Users"   ;
        private static final String COLUMN_ID       = "userId"  ;
        private static final String COLUMN_USERNAME = "username";
        private static final String COLUMN_PASSWORD = "password";
        private static final String COLUMN_LOGGED   = "logged"  ;
        private static final String COLUMN_STATE    = "state"   ;

        private static final int ID = 1;
        private static final int USERNAME = 2;
        private static final int PASSWORD = 3;
        private static final int STATE = 4;
        private static final int LOGGED = 5;

    }

    public static final class Message {

        private static final String TEXT_LOGIN    = "LOGIN";
        private static final String TEXT_CANCEL   = "CANCEL";
        private static final String TEXT_LOGOUT   = "LOGOUT";
        private static final String TEXT_TODAY    = "TODAY";
        private static final String TEXT_REGISTER = "REGISTER";
        private static final String TEXT_YESTERDAY = "YESTERDAY";

        private static final String TEXT_MAC_ALDIK = "14-DA-E9-72-CA-D5";

        private static final String MESSAGE_START = "Please, login!";

        private static final String MESSAGE_LOGIN = "Please, enter you credentials in this manner: \n" +
                "'username'\n'password'\n\n If this message keeps popping up, it means there is no corresping " +
                "username and password.";
        private static final String MESSAGE_MAIN_MENU = "You entered into the system, please, press logout, if you " +
                "want to logout";
        private static final String MESSAGE_REGISTER = "Please, enter you credentials in this manner: \n" +
                "'username'\n'password'\n\n If successfull, you will be redicrected to Login menu, if no then to start" +
                " menu. Please, try once more with different username";

        public static String getLoginText() {
            return TEXT_LOGIN;
        }
        public static String getRegisterText() {
            return TEXT_REGISTER;
        }
        public static String getMacAldikText() {
            return TEXT_MAC_ALDIK;
        }
        public static String getCancelText() {
            return TEXT_CANCEL;
        }
        public static String getLogoutText() {
            return TEXT_LOGOUT;
        }
        public static String getTodayText() {
            return TEXT_TODAY;
        }
        public static String getYesterdayText() {
            return TEXT_YESTERDAY;
        }

        public static String getStartMessage() {
            return MESSAGE_START;
        }
        public static String getLoginMessage() {
            return MESSAGE_LOGIN;
        }
        public static String getRegisterMessage() {
            return MESSAGE_REGISTER;
        }
        public static String getMainMenuMessage() {
            return MESSAGE_MAIN_MENU;
        }
    }

    private static final String DATABASE_TAG  = "DATABASE";
    private static final String DATABASE_NAME = "finappstelegram";
    private static final String URL = "jdbc:postgresql://127.0.0.1:5432/";
    private static final String USERNAME  = "postgres";
    private static final String PASSWORD  = "vendetta94";

    private static final String TRUE  = "TRUE";
    private static final String FALSE = "FALSE";


    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS " + UsersTable.TABLENAME + "("    +
            UsersTable.COLUMN_ID + " NUMERIC NOT NULL PRIMARY KEY, "      +
            UsersTable.COLUMN_USERNAME + " VARCHAR(20) UNIQUE, " +
            UsersTable.COLUMN_PASSWORD + " VARCHAR(20), "        +
            UsersTable.COLUMN_STATE    + " NUMERIC DEFAULT 0, "                     +
            UsersTable.COLUMN_LOGGED   + " BOOLEAN DEFAULT FALSE);";


    private static final String UPDATE_LOGGED = "UPDATE " + UsersTable.TABLENAME +
            " SET " + UsersTable.COLUMN_LOGGED + " = %s " +
            " WHERE " + UsersTable.COLUMN_ID + " = %s;";

    private static final String UPDATE_USER = "UPDATE " + UsersTable.TABLENAME +
            " SET " + UsersTable.COLUMN_USERNAME+ " = '%s', " + UsersTable.COLUMN_PASSWORD +
            " = '%s' WHERE " + UsersTable.COLUMN_ID + "= %s;";

    private static final String INSERT_USER = "INSERT INTO " + UsersTable.TABLENAME +
            "(" + UsersTable.COLUMN_ID + ") VALUES (%s);";

    private static final String UPDATE_STATE = "UPDATE " + Database.UsersTable.TABLENAME +
            " SET " + Database.UsersTable.COLUMN_STATE + " = %s " +
            " WHERE " + Database.UsersTable.COLUMN_ID + " = %s;";

    private static final String SEARCH_USER = "SELECT * FROM " + Database.UsersTable.TABLENAME +
            " WHERE " + Database.UsersTable.COLUMN_ID + " = %s;";

    private static final String SEARCH_CREDTS = "SELECT * FROM " + Database.UsersTable.TABLENAME +
            " WHERE " + UsersTable.COLUMN_USERNAME + " = '%s' AND " +
            UsersTable.COLUMN_PASSWORD + " = '%s';";

    public static Connection createConnection() {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL + DATABASE_NAME, USERNAME, PASSWORD);
            connection.setAutoCommit(false);
            init(connection);
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }
        return connection;
    }

    public static void close(Connection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }
    }

    private static void init(Connection connection) {
        Statement statement = null;
        try{
            statement = connection.createStatement();

            statement.execute(CREATE_TABLE_USERS    );
            statement.close  ();
            connection.commit();
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }
    }

    public static void insertUser(Connection connection, int userId) {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String num = Integer.toString(userId);
            statement.executeUpdate(String.format(INSERT_USER, num));
            statement.close();
            connection.commit();
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }
    }

    public static void updateUser(Connection connection, int userId, String username, String password) {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String num = Integer.toString(userId);
            statement.executeUpdate(String.format(UPDATE_USER, username, password, num));
            statement.close();
            connection.commit();
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }
    }

    public static void updateLogged(Connection connection, int userId, boolean logged) {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String num, bool;
            num = Integer.toString(userId);
            if (logged) bool = TRUE;
            else        bool = FALSE;

            statement.executeUpdate(String.format(UPDATE_LOGGED, bool, num));
            statement.close();
            connection.commit();
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }
    }

    public static boolean isLogged(Connection connection, int userId) {
        Statement statement = null;
        boolean ret = false;
        try {
            statement = connection.createStatement();

            String num = Integer.toString(userId);
            ResultSet rs = statement.executeQuery(String.format(SEARCH_USER, num));

            if (rs.next()) ret = rs.getBoolean(Database.UsersTable.LOGGED);
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }

        return ret;
    }

    public static void updateState(Connection connection, int userId, int stateTo) {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String num, stateNum;
            num      = Integer.toString(userId );
            stateNum = Integer.toString(stateTo);

            statement.executeUpdate(String.format(UPDATE_STATE, stateNum, num));
            statement.close();
            connection.commit();
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }
    }

    public static boolean searchUser(Connection connection, int userId) {
        Statement statement = null;
        boolean ret = false;
        try {
            statement = connection.createStatement();

            String num = Integer.toString(userId);
            ResultSet rs = statement.executeQuery(String.format(SEARCH_USER, num));

            if (rs.next()) ret = true ;
            else           ret = false;
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }

        return ret;
    }

    public static int getState(Connection connection, int userId) {
        Statement statement = null;
        int state = 0;
        try {
            statement = connection.createStatement();

            String num = Integer.toString(userId);
            ResultSet rs = statement.executeQuery(String.format(SEARCH_USER, num));

            if (rs.next()) state = rs.getInt(Database.UsersTable.STATE);
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }

        return state;
    }

    public static boolean searchCredts(Connection connection, String username, String password) {
        Statement statement = null;
        boolean ret = false;
        try {
            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(String.format(SEARCH_CREDTS, username, password));

            if (rs.next()) ret = true ;
            else           ret = false;
        } catch (Exception e) {
            BotLogger.error(DATABASE_TAG, e);
        }

        return ret;
    }
}
