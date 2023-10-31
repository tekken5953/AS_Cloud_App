package app.airsignal.weather.retrofit;

import java.util.List;

import app.airsignal.weather.dao.AdapterModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyApiImpl {

    @GET("forecast")    // 전체 데이터 호출
    Call<ApiModel.GetEntireData> getForecast(
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("addr") String addr);

    @GET("forecast")    // 위젯 데이터 호출
    Call<ApiModel.WidgetData> getWidgetForecast(
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("rcount") Integer count);

    @GET("notice")  // 공지사항 호출
    Call<List<AdapterModel.NoticeItem>> getNotice();

    @GET("faq") // 자주 묻는 질문 호출
    Call<List<AdapterModel.FaqItem>> getFaq();

    @GET("version") // 앱 버전 호출
    Call<ApiModel.AppVersion> getVersion();

    @GET("forecast/broadcast") // 기상특보 데이터 호출
    Call<ApiModel.BroadCastWeather> getBroadCast(
            @Query("code") int code);
}
