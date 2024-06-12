package app.airsignal.weather.api.retrofit;

import androidx.annotation.Nullable;

import java.util.List;

import app.airsignal.weather.api.NetworkIgnored;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyApiImpl {
    @GET(NetworkIgnored.weatherPoint) // 전체 데이터 호출
    Call<ApiModel.GetEntireData> getForecast(
            @Nullable @Query(NetworkIgnored.weatherParamLeft) Double lat,
            @Nullable @Query(NetworkIgnored.weatherParamCenter) Double lng,
            @Nullable @Query(NetworkIgnored.weatherParamRight) String addr);

    @GET(NetworkIgnored.weatherPoint) // 위젯 데이터 호출
    Call<ApiModel.WidgetData> getWidgetForecast(
            @Nullable @Query(NetworkIgnored.weatherParamLeft) Double lat,
            @Nullable @Query(NetworkIgnored.weatherParamCenter) Double lng,
            @Nullable @Query(NetworkIgnored.weatherParamElse) Integer count);

    @GET(NetworkIgnored.notiPoint) // 공지사항 호출
    Call<List<ApiModel.NoticeItem>> getNotice();

    @GET(NetworkIgnored.splashPoint) // 앱 버전 호출
    Call<ApiModel.AppVersion> getVersion();

    @GET(NetworkIgnored.warningPoint) // 기상 특보 데이터 호출
    Call<ApiModel.BroadCastWeather> getBroadCast(
            @Query(NetworkIgnored.warningParam) int code);
}
