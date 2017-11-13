import retrofit.AttendanceItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by dauren on 1/26/17.
 */
public class Utils {

    public static final long DAY = 1000*60*60*24;

    public static final String EMPTY = "EMPTY";

    private static final String DATE_FORMAT = "dd.MM.YYYY";
    private static final String MONTH_FORMAT = "YYYY_MM";
    private static final String SEP = "\n\n";

    public static String getData(List<AttendanceItem> list, boolean today) {

        String date;
        if (today) date = new SimpleDateFormat(DATE_FORMAT).format(new Date().getTime());
        else date = new SimpleDateFormat(DATE_FORMAT).format((new Date().getTime() - DAY));

        StringBuilder sb = new StringBuilder();
        sb.append(date + SEP);
        for (AttendanceItem item: list) if (item.date.equals(date)) sb.append(item.toString() + SEP);
        return sb.toString();
    }

    public static String getYYYYMM() {
        DateFormat dateFormat = new SimpleDateFormat(MONTH_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String[] getCreds(String text) {
        String[] ret = text.split("\\s+");
        if (ret.length == 2) {
            ret[0] = ret[0].trim();
            ret[1] = ret[1].trim();
            if (ret[0].length() > 0 && ret[1].length() > 0)
                return ret;
        }
        return new String[]{EMPTY, EMPTY};
    }
}
