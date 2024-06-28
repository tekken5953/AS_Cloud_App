package app.airsignal.weather.api.retrofit;

import androidx.annotation.Nullable;

import java.util.List;

import app.airsignal.weather.api.NetworkIgnored;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyApiImpl {
    @GET(NetworkIgnored.WEATHER_POINT) // 전체 데이터 호출
    Call<ApiModel.GetEntireData> getForecast(
            @Nullable @Query(NetworkIgnored.WEATHER_PARAM_LEFT) Double lat,
            @Nullable @Query(NetworkIgnored.WEATHER_PARAM_CENTER) Double lng,
            @Nullable @Query(NetworkIgnored.WEATHER_PARAM_RIGHT) String addr);

    @GET(NetworkIgnored.WEATHER_POINT) // 위젯 데이터 호출
    Call<ApiModel.WidgetData> getWidgetForecast(
            @Nullable @Query(NetworkIgnored.WEATHER_PARAM_LEFT) Double lat,
            @Nullable @Query(NetworkIgnored.WEATHER_PARAM_CENTER) Double lng,
            @Nullable @Query(NetworkIgnored.WEATHER_PARAM_COUNT) Integer count);

    @GET(NetworkIgnored.NOTIFICATION_POINT) // 공지사항 호출
    Call<List<ApiModel.NoticeItem>> getNotice();

    @GET(NetworkIgnored.SPLASH_POINT) // 앱 버전 호출
    Call<ApiModel.AppVersion> getVersion();

    @GET(NetworkIgnored.WARNING_POINT) // 기상 특보 데이터 호출
    Call<ApiModel.BroadCastWeather> getBroadCast(
            @Query(NetworkIgnored.WARNING_PARAM) int code);
}
