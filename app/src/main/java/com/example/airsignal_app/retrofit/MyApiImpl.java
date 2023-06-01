package com.example.airsignal_app.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyApiImpl {

    @GET("forecast")
    Call<ApiModel.GetEntireData> getForecast(
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("addr") String addr);

    @GET("forecast/realtime")
    Call<List<ApiModel.RealTimeData>> getRealTime(
            @Query("lat")Double lat,
            @Query("lng")Double lng);

    @GET("forecast/week")
    Call<ApiModel.WeeklyData> getWeeklyData(
            @Query("lat")Double lat,
            @Query("lng")Double lng);

    @GET("forecast/quality")
    Call<ApiModel.AirQualityData> getAirQuality(
            @Query("lat")Double lat,
            @Query("lng")Double lng);

    @GET("forecast/sun")
    Call<ApiModel.SunData> getSun(
            @Query("lat")Double lat,
            @Query("lng")Double lng);
}
