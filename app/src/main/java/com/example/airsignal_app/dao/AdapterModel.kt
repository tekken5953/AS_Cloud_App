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

    // 시간별 날씨
    data class DailyWeatherItem(
        val time: String,
        val img: Drawable?,
        val value: String,
        val date: String
    )

    // 주간별 날씨
    data class WeeklyWeatherItem(
        val day: String,
        val date: String,
        val minImg: Drawable?,
        val maxImg: Drawable?,
        val minText: String,
        val maxText: String
    )

    // 공지사항 리스트
    data class NoticeItem(
        val date: String,
        val title: String
    )

    // 공지사항 내용
    data class DetailItem(
        val title: String,
        val content: String,
        val isNotice: Boolean
    )

    // 디자인 테스트 - 설정
    data class TestItem(
        val font: String,
        val size: String,
        val color: String,
        val value: String
    )

    // 앱 버전
    data class AppVersionItem(
        val version: String
    )

    // 이벤트 리스트
    data class EventItem(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val date: String,
        val title: String,
        val content: Any?
    )

    // 공기질 데이터
    data class AirQualityItem(
        val title: String,
        val data: String?
    )
}