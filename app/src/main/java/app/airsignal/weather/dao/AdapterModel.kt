package app.airsignal.weather.dao

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

    // 실시간 공기질 타이틀 아이템
    data class AirQTitleItem(
        var isSelect: Boolean = false,
        val position: Int,
        val nameKR: String,
        val name: String,
        val unit: String,
        val value: String,
        val grade: Int
    )

    // 영문/국문 주소 쌍 모델
    data class AddressListItem(
        val kr: String?,
        val en: String?
    )
}