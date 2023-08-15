package com.example.airsignal_app.retrofit;

import com.example.airsignal_app.dao.AdapterModel;

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

    @GET("forecast")
    Call<ApiModel.Widget4x2Data> getWidgetForecast(
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("rcount") Integer count);

    @GET("notice")
    Call<List<AdapterModel.NoticeItem>> getNotice();

    @GET("faq")
    Call<List<AdapterModel.FaqItem>> getFaq();

    @GET("version")
    Call<ApiModel.AppVersion> getVersion();
}
