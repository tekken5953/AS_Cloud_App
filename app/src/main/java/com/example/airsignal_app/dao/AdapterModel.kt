package com.example.airsignal_app.dao

import android.graphics.drawable.Drawable
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

object AdapterModel {
    // 디테일 페이지 공기질데이터 모델
    data class AirCondData(var title: String, var data: String?, var sort: String)

    // 장치검색 시 GET 할 데이터 모델
    data class GetDeviceList(
        val userId: String, val device: String,
        val deviceName: String?, val businessType: String?,
        @SerializedName("CAIval") var cqiValue: String?,
        @SerializedName("Virusval") var virusValue: String?,
        var starred: Boolean, val owned: Boolean
    )

    data class GridItem(val img: Drawable, val text: String)

    data class WeatherItem(
        val address: String,
        val temp: String,
        val sunRise: String,
        val sunSet: String,
        val sky: String,
        val humid: String,
        val wind: String,
        val rainPer: String,
        val pm2p5Grade: Int,
        val pm10Grade: Int,
        @SerializedName("temperatureMin") val minTemp: String,
        @SerializedName("temperatureMax") val maxTemp: String
    )

    data class DailyWeatherItem(
        val time: String,
        val img: Drawable?,
        val value: String,
        val date: String
    )

    data class WeeklyWeatherItem(
        val day: String,
        val minImg: Drawable?,
        val maxImg: Drawable?,
        val minText: String,
        val maxText: String
    )

    data class NoticeItem(
        val date: String,
        val title: String
    )

    data class DetailItem(
        val title: String,
        val content: String,
        val isNotice: Boolean
    )

    data class TestItem(
        val font: String,
        val size: String,
        val color: String,
        val value: String
    )

    data class AppVersionItem(
        val version: String
    )

    data class EventItem(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val date: String,
        val title: String,
        val content: Any?
    )

    data class AirQualityItem(
        val title: String,
        val data: String?
    )

    data class AddressListItem(
        val addr: String
    )
}