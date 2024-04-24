package app.airsignal.weather.dao

import android.graphics.drawable.Drawable
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

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
        val maxText: String,
        val minRain: Int,
        val maxRain: Int
    )

    // 자외선 단계별 대응요령
    data class UVResponseItem(
        val text: String
    )

    // 영문/국문 주소 쌍 모델
    data class AddressListItem(
        val kr: String?,
        val en: String?
    )
}