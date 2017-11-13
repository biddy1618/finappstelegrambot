package retrofit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dauren on 2/6/17.
 */
public class AttendanceItem {
    public String came_time;
    public String date;
    public String dd;
    public String employeeName;
    public String gone_time;
    public String stay_hours;

    @Override
    public String toString() {
        return employeeName + ": " + came_time + " - " + gone_time;
    }
}
