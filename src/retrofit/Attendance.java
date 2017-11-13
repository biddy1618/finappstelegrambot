package retrofit;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * Created by dauren on 2/6/17.
 */
public interface Attendance {
    @GET("attback/ws/rest/getEmpAttCs")
    Call<List<AttendanceItem>> getAttendance(@Query("mac") String mac, @Query("yyyy_mm") String yyyy_mm);

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.7.16:2080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
