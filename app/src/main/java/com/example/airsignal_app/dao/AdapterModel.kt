package com.example.airsignal_app.dao

import android.graphics.drawable.Drawable

object AdapterModel {

    // 시간별 날씨
    data class DailyWeatherItem(
        val time: String,
        val img: Drawable?,
        val value: String,
        val date: String,
        val rainP: Double?,
        val isRain: Boolean
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
//
//    // 공지사항 내용
//    data class DetailItem(
//        val title: String,
//        val content: String,
//        val isNotice: Boolean
//    )

//    // 앱 버전
//    data class AppVersionItem(
//        val version: String
//    )

//    // 이벤트 리스트
//    data class EventItem(
//        @PrimaryKey(autoGenerate = true) val id: Int,
//        val date: String,
//        val title: String,
//        val content: Any?
//    )

    // 자외선 지수 범례
    data class UVLegendItem(
        val value: String,
        val color: Int,
        val grade: String
        )

    // 자외선 단계별 대응요령
    data class UVResponseItem(
        val text: String
    )

    // 날씨 특보 뷰페이저 아이템
    data class ReportItem(
        val text: String
    )
}