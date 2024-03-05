package app.airsignal.weather.network.retrofit;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import app.airsignal.weather.as_eye.dao.EyeDataModel;
import app.airsignal.weather.dao.AdapterModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MyApiImpl {

    @GET("forecast")
        // 전체 데이터 호출
    Call<ApiModel.GetEntireData> getForecast(
            @Nullable @Query("lat") Double lat,
            @Nullable @Query("lng") Double lng,
            @Nullable @Query("addr") String addr);

    @GET("forecast")
        // 위젯 데이터 호출
    Call<ApiModel.WidgetData> getWidgetForecast(
            @Nullable @Query("lat") Double lat,
            @Nullable @Query("lng") Double lng,
            @Nullable @Query("rcount") Integer count);

    @GET("notice")
        // 공지사항 호출
    Call<List<ApiModel.NoticeItem>> getNotice();

    @GET("version")
        // 앱 버전 호출
    Call<ApiModel.AppVersion> getVersion();

    @GET("forecast/broadcast")
        // 기상 특보 데이터 호출
    Call<ApiModel.BroadCastWeather> getBroadCast(
            @Query("code") int code);

    @GET("airsignal/current/{sn}")
        // Eye 측정 데이터 호출
    Call<EyeDataModel.Entire> getEntire(
            @NotNull @Path("sn") String sn,
            @Nullable @Query("flag") String flag,
            @Nullable @Query("start") Integer start,
            @Nullable @Query("end") Integer end
    );

    @POST("device/{sn}")
        // Eye Alias 변경
    Call<String> updateAlias(
            @Path("sn") String sn,
            @Body String alias
    );

    @POST("device")
    // EYE 기기 추가
    Call<String> postDevice(
            @Query("id") String id,
            @Body EyeDataModel.PostDevice item
    );

    @GET("device")
        // Eye 기기 조회
    Call<List<EyeDataModel.Device>> getDeviceList();

    @DELETE("device/{sn}")
        // Eye 기기 삭제
    Call<String> deleteDevice(
            @Path("sn") String sn,
            @Query("id") String id
    );

    @GET("noise/{sn}")
    Call<List<AdapterModel.NoiseDetailItem>> getNoise(
            @NotNull @Path("sn") String sn,
            @Nullable @Query("flag") String flag,
            @Nullable @Query("start") Integer start,
            @Nullable @Query("end") Integer end
    );

    @GET("owners/{sn}")
    Call<List<String>> getOwner(
            @NotNull @Path("sn") String sn
    );
}
