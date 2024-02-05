package app.airsignal.weather.network.retrofit;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import app.airsignal.weather.as_eye.dao.EyeDataModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MyApiImpl {

    @GET("forecast")    // 전체 데이터 호출
    Call<ApiModel.GetEntireData> getForecast(
            @Nullable @Query("lat") Double lat,
            @Nullable @Query("lng") Double lng,
            @Nullable @Query("addr") String addr);

    @GET("forecast")    // 위젯 데이터 호출
    Call<ApiModel.WidgetData> getWidgetForecast(
            @Nullable @Query("lat") Double lat,
            @Nullable @Query("lng") Double lng,
            @Nullable @Query("rcount") Integer count);

    @GET("notice")  // 공지사항 호출
    Call<List<ApiModel.NoticeItem>> getNotice();

    @GET("version") // 앱 버전 호출
    Call<ApiModel.AppVersion> getVersion();

    @GET("forecast/broadcast") // 기상 특보 데이터 호출
    Call<ApiModel.BroadCastWeather> getBroadCast(
            @Query("code") int code);

    @GET("airsignal/current/{sn}") // Eye 측정 데이터 호출
    Call<EyeDataModel.Measured> getEntire(
            @NotNull @Path("sn") String sn);
}
