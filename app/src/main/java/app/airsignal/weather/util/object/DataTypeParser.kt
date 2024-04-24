package app.airsignal.weather.util.`object`

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

/**
 * @author : Lee Jae Young
 * @since : 2023-03-20 오후 4:22
 **/
object DataTypeParser {
    /** 현재시간 불러오기 **/
    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    fun getAverageTime(time: Long): Int {
        val currentTime = parseLongToLocalDateTime(time)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return currentTime.format(dateFormatter).toInt()
    }

    fun getHourCountToTomorrow(): Int {
        val currentHour = parseLongToLocalDateTime(getCurrentTime()).hour
        return 24 - currentHour
    }

    fun currentDateTimeString(format: String): String {
        @SuppressLint("SimpleDateFormat") val mFormat = SimpleDateFormat(format)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }
        return mFormat.format(calendar.time)
    }

    fun dateTimeString(format: String, date: LocalDateTime?): String {
        @SuppressLint("SimpleDateFormat") val mFormat = SimpleDateFormat(format)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = parseLocalDateTimeToLong(date ?: LocalDateTime.now())
        }
        return mFormat.format(calendar.time)
    }

    /** 강수형태가 없으면 하늘상태 있으면 강수형태 - 텍스트 **/
    fun applySkyText(context: Context, rain: String?, sky: String?, thunder: Double?): String {
        return if (rain != "없음") if ((thunder == null) || (thunder < 0.2))  rain ?: "없음" else  context.getString(R.string.thunder_sunny)
         else if ((thunder == null) || (thunder < 0.2)) sky ?: "맑음" else  context.getString(R.string.thunder_rainy)
    }

    fun translateSkyText(sky: String): String {
        val id = when (sky) {
            "맑음" -> "clear"
            "구름많음", "흐림" -> "cloudy"
            "소나기", "비", "구름많고 소나기", "흐리고 비", "구름많고 비", "흐리고 소나기" -> "rainy"
            "구름많고 눈", "흐리고 눈", "눈" -> "snowy"
            "구름많고 비/눈", "흐리고 비/눈", "비/눈" -> "rainy/snowy"
            else -> ""
        }

        return id
    }

    fun koreaSky(sky: String?): String {
        val id = when(sky?.lowercase()) {
            "sunny","Sunny&Cloudy" -> "맑음"
            "cloudy", -> "흐림"
            "rainy" -> "비"
            "snowy" -> "눈"
            "rainy/snowy" -> "비/눈"
            else -> sky
        }

        return id ?: "눈"
    }

    /** 달 모양 반환 **/
    private fun applyLunarImg(date: Int): Int {
        return when (date) {
            29,30,1 -> R.drawable.moon_sak
            in 2..5 -> R.drawable.moon_cho
            in 6..9 -> R.drawable.moon_sang_d
            in 10..13 -> R.drawable.moon_sang_m
            in 14..16 -> R.drawable.moon_bo
            in 17..20 -> R.drawable.moon_ha_d
            in 21..24-> R.drawable.moon_ha_m
            in 25..28 -> R.drawable.moon_g
            else -> R.drawable.moon_bo
        }
    }

    /** 비가 오는지 안오는지 Flag **/
    fun isRainyDay(rainType: String?): Boolean { return rainType != "없음" }

    /** 어제 날씨와 오늘 날씨의 비교 값 반환 **/
    fun getComparedTemp(yesterday: Double?, today: Double?): Double? {
        val temp = yesterday?.let { y ->
            today?.let { t ->
                if (y != -100.0 && t != -100.0) ((today - yesterday) * 10).roundToInt() / 10.0
                else null
            }
        }
        return temp
    }

    /** rain type에 따른 이미지 설정 **/
    private fun getRainTypeLarge(context: Context, rain: String?): Drawable? {
        val rainMap = mapOf(
            "비" to R.drawable.b_ico_cloudy_rainy,
            "눈" to R.drawable.b_ico_snow,
            "비/눈" to R.drawable.b_ico_rainy_snow,
            "소나기" to R.drawable.b_ico_rainy
        )
        return rainMap[rain]?.let{ getDrawable(context,it)} ?: getDrawable(context,R.drawable.cancel)
    }

    /** sky value에 따른 이미지 설정 **/
    fun getSkyImgLarge(context: Context, sky: String?, isNight: Boolean, lunar: Int): Drawable? {
        val id = when(sky) {
            "맑음" ->
                if (!isNight) R.drawable.b_ico_sunny
//                else  applyLunarImg(lunar)
                 else R.drawable.ico_moon_big
            "구름많음" ->
                if (!isNight)  R.drawable.b_ico_m_cloudy
                else  R.drawable.b_ico_m_ncloudy
            "흐림" -> R.drawable.b_ico_cloudy
            "소나기", "비" -> R.drawable.b_ico_rainy
            "구름많고 눈", "눈", "흐리고 눈" -> R.drawable.b_ico_snow
            "구름많고 소나기", "흐리고 비", "구름많고 비", "흐리고 소나기" -> R.drawable.b_ico_cloudy_rainy
            "구름많고 비/눈", "흐리고 비/눈", "비/눈" -> R.drawable.b_ico_rainy_snow
            else -> R.drawable.cancel
        }

        return getDrawable(context,id)
    }

    fun getSkyImgWidget(rainType: String?, sky: String?, isNight: Boolean): Int {
        return if (rainType == "없음" || rainType == null) {
            when (sky) {
                "맑음" ->
                    if (isNight) R.drawable.w_ico_status
                    else R.drawable.b_ico_sunny
                "구름많음" ->
                    if (isNight) R.drawable.b_ico_m_ncloudy
                    else R.drawable.b_ico_m_cloudy
                "흐림" -> R.drawable.b_ico_cloudy
                "소나기", "비" -> R.drawable.b_ico_rainy
                "구름많고 눈", "눈", "흐리고 눈" -> R.drawable.b_ico_snow
                "구름많고 소나기", "흐리고 비", "구름많고 비", "흐리고 소나기" -> R.drawable.b_ico_cloudy_rainy
                "구름많고 비/눈", "흐리고 비/눈", "비/눈" -> R.drawable.b_ico_rainy_snow
                else -> R.drawable.cancel
            }
        } else {
            when(rainType) {
                "비" -> R.drawable.b_ico_cloudy_rainy
                "눈" -> R.drawable.sm_snow
                "비/눈" -> R.drawable.b_ico_rainy_snow
                "소나기" -> R.drawable.b_ico_rainy
                else -> R.drawable.cancel
            }
        }
    }

    fun getBackgroundImgWidget(sort: String, rainType: String?, sky: String?, isNight: Boolean): Int {
        return if (rainType == "없음" || rainType == null) {
            when (sky) {
                "맑음", "구름많음" -> {
                    if (sort == "22") if (isNight) R.drawable.w_bg_night else R.drawable.w_bg_sunny
                    else if (isNight) R.drawable.widget_bg4x2_night else R.drawable.widget_bg4x2_sunny
                }
                "구름많고 비/눈", "흐리고 비/눈", "비/눈", "구름많고 소나기",
                "흐리고 비", "구름많고 비", "흐리고 소나기", "소나기", "비", "흐림",
                "번개,뇌우", "비/번개" -> {
                    if (sort == "22") R.drawable.w_bg_cloudy else  R.drawable.widget_bg4x2_cloud
                }
                "구름많고 눈", "눈", "흐리고 눈" -> {
                    if (sort == "22") R.drawable.w_bg_snow else R.drawable.widget_bg4x2_snow
                }
                else -> if (sort == "22") R.drawable.w_bg_snow else R.drawable.widget_bg4x2_snow
            }
        } else {
            when (rainType) {
                "비","소나기" -> if(sort == "22") R.drawable.w_bg_cloudy else R.drawable.widget_bg4x2_cloud
                else -> if(sort == "22") R.drawable.w_bg_snow else R.drawable.widget_bg4x2_snow
            }
        }
    }

    /** rain type에 따른 이미지 설정 **/
    private fun getRainTypeSmall(context: Context, rain: String?): Drawable? {
        val rainMap = mapOf(
            "비" to R.drawable.b_ico_cloudy_rainy,
            "눈" to R.drawable.sm_snow,
            "비/눈" to R.drawable.b_ico_rainy_snow,
            "소나기" to R.drawable.b_ico_rainy
        )

        return rainMap[rain ?: ""]?.let { getDrawable(context,it) } ?: getDrawable(context,R.drawable.cancel)
    }

    /** sky value에 따른 이미지 설정 **/
    fun getSkyImgSmall(context: Context, sky: String?, isNight: Boolean): Drawable? {
        val id = when(sky) {
            "맑음" ->
                if (!isNight) R.drawable.b_ico_sunny
                else  R.drawable.sm_good_n
            "구름많음" ->
                if (!isNight)  R.drawable.b_ico_m_cloudy
                else  R.drawable.b_ico_m_ncloudy
            "흐림" -> R.drawable.b_ico_cloudy
            "소나기", "비" -> R.drawable.b_ico_rainy
            "구름많고 눈", "눈", "흐리고 눈" -> R.drawable.sm_snow
            "구름많고 소나기", "흐리고 비", "구름많고 비", "흐리고 소나기" -> R.drawable.b_ico_cloudy_rainy
            "구름많고 비/눈", "흐리고 비/눈", "비/눈" -> R.drawable.b_ico_rainy_snow
            else -> R.drawable.cancel
        }

        return getDrawable(context,id)
    }

    /** 강수형태가 없으면 하늘상태 있으면 강수형태 - 이미지 **/
    fun applySkyImg(
        context: Context,
        rain: String?,
        sky: String?,
        thunder: Double?,
        isLarge: Boolean,
        isNight: Boolean?,
        lunar: Int
    ): Drawable? {
        return if (rain != "없음" && (thunder == null || thunder < 0.2)) {
            if (isLarge) getRainTypeLarge(context, rain) ?: getDrawable(context, R.drawable.cancel)
            else getRainTypeSmall(context, rain) ?: getDrawable(context, R.drawable.cancel)
        } else if (rain == "없음" && (thunder == null || thunder < 0.2)) {
            if (isLarge) getSkyImgLarge(context, sky, isNight ?: false, lunar)
            else getSkyImgSmall(context, sky, isNight ?: false)
        } else getDrawable(context, R.drawable.b_ico_cloudy_th)
    }

    /** 등급에 따른 색상 변환 **/
    fun getDataColor(context: Context, grade: Int): Int {
        val colorMap = mapOf (
            1 to R.color.air_good,
            2 to R.color.air_normal,
            3 to R.color.air_bad,
            4 to R.color.air_very_bad
        )

        return colorMap[grade]?.let { ResourcesCompat.getColor(context.resources, it, null) }
            ?:  ResourcesCompat.getColor(context.resources, R.color.main_gray_color, null)
    }

    /** 등급에 따른 텍스트 변환 **/
    fun getDataText(context: Context, grade: Int): String {
        return when (grade) {
            1 -> context.getString(R.string.good)
            2 -> context.getString(R.string.normal)
            3 -> context.getString(R.string.bad)
            4 -> context.getString(R.string.worst)
            else -> context.getString(R.string.error)
        }
    }

    /** 지정 자릿수에서 반올림 **/
    fun parseDoubleToDecimal(double: Double, digit: Int): String {
        return String.format("%.${digit}f", double)
    }

    /** 요일 변환 **/
    fun parseDayOfWeekToKorean(context: Context, dayOfWeek: Int): String {
        val dayMap = mapOf (
            1 to R.string.mon,
            2 to R.string.tue,
            3 to R.string.wen,
            4 to R.string.thr,
            5 to R.string.fir,
            6 to R.string.sat,
            0 to R.string.sun
        )

        return dayMap[dayOfWeek % 7]?.let { context.getString(it) } ?: ""
    }

    /** 주소 포멧팅 **/
    fun convertAddress(addr: String): String {
        return addr.replace("특별시", "시").replace("광역시", "시")
            .replace("특별자치도", "도")
    }

    /** UV 범주 색상 적용 **/
    fun applyUvColor(context: Context, flag: String, textView: TextView) {
        val flagMap = mapOf (
            "낮음" to Pair(R.drawable.uv_low_bg,R.color.uv_low),
            "보통" to Pair(R.drawable.uv_normal_bg,R.color.uv_normal),
            "높음" to Pair(R.drawable.uv_high_bg,R.color.uv_high),
            "매우높음" to Pair(R.drawable.uv_veryhigh_bg,R.color.uv_very_high),
            "위험" to Pair(R.drawable.uv_caution_bg,R.color.uv_caution)
        )


        flagMap[flag]?.let {
            textView.setBackgroundResource(it.first)
            textView.setTextColor(context.getColor(it.second))
        }
    }

    /** 상태 바 설정 **/
    @Suppress("DEPRECATION")
    fun setStatusBar(activity: Activity) {
        activity.window.apply {
            statusBarColor = activity.getColor(R.color.theme_view_color)
            navigationBarColor = activity.getColor(android.R.color.transparent)
        }
    }
    
    /** 하늘상태를 국가에 맞게 변경 **/
    fun translateSky(context: Context, sky: String): String {
        val skyMap = mapOf(
            "맑음" to R.string.sky_sunny,
            "구름많음" to R.string.sky_sunny_cloudy,
            "흐림" to R.string.sky_cloudy,
            "소나기" to R.string.sky_shower,
            "비" to R.string.sky_rainy,
            "구름많고 눈" to R.string.sky_sunny_cloudy_snowy,
            "눈" to R.string.sky_snowy,
            "흐리고 눈" to R.string.sky_cloudy_snowy,
            "구름많고 소나기" to R.string.sky_sunny_cloudy_shower,
            "흐리고 비" to R.string.sky_cloudy_rainy,
            "구름많고 비" to R.string.sky_sunny_cloudy_rainy,
            "흐리고 소나기" to R.string.sky_cloudy_shower,
            "구름많고 비/눈" to R.string.sky_sunny_cloudy_rainy_snowy,
            "흐리고 비/눈" to R.string.sky_cloudy_rainy_snowy,
            "비/눈" to R.string.sky_rainy_snowy,
            context.getString(R.string.thunder_sunny) to R.string.thunder_sunny,
            context.getString(R.string.thunder_rainy) to R.string.thunder_rainy
        )

        return skyMap[sky]?.let { context.getString(it) } ?: ""
    }

    /** 자외선 지수 번역 **/
    fun translateUV(context: Context, uv: String): String {
        val uvMap = mapOf(
            "낮음" to R.string.uv_low,
            "보통" to R.string.uv_normal,
            "높음" to R.string.uv_high,
            "매우높음" to R.string.uv_very_high,
            "위험" to R.string.uv_caution
        )

        return uvMap[uv]?.let { context.getString(it) } ?: ""
    }

    /** LocalDateTime을 Long으로 파싱 **/
    fun parseLocalDateTimeToLong(localDateTime: LocalDateTime): Long {
        return try {
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: NullPointerException) {
            LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }

    /** Long을 LocalDateTime으로 파싱 **/
    fun parseLongToLocalDateTime(long: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(long), ZoneId.systemDefault())
    }

    /** 공기질 데이터 등급변환 **/
    fun convertValueToGrade(s: String, v: Double): Int {
        val gradeRanges = when (s) {
            "SO2" -> listOf(0.02, 0.05, 0.15)
            "CO" -> listOf(2.0, 9.0, 15.0)
            "O3" -> listOf(0.03, 0.09, 0.15)
            "NO2" -> listOf(0.03, 0.06, 0.2)
            "PM2.5" -> listOf(15.0, 35.0, 75.0)
            "PM10" -> listOf(30.0, 80.0, 150.0)
            else -> return 0 // 알 수 없는 물질
        }

        return when {
            v <= gradeRanges[0] -> 1
            v <= gradeRanges[1] -> 2
            v <= gradeRanges[2] -> 3
            else -> 4
        }
    }

    /** 날짜가 한자리일 때 앞에 0 붙이기 **/
    fun dateAppendZero(dateTime: LocalDateTime): String {
        return if (dateTime.monthValue / 10 == 0)
            if (dateTime.dayOfMonth / 10 == 0) "0${dateTime.monthValue}.0${dateTime.dayOfMonth}"
             else "0${dateTime.monthValue}.${dateTime.dayOfMonth}"
         else {
            if (dateTime.dayOfMonth / 10 == 0) "${dateTime.monthValue}.0${dateTime.dayOfMonth}"
             else "${dateTime.monthValue}.${dateTime.dayOfMonth}"
        }
    }

    /** 문자열에서 해당 문자의 인덱스 반환 **/
    fun findCharacterIndex(input: String, targetChar: Char): Int {
        for (index in input.indices) { if (input[index] == targetChar) return index }
        return -1 // 문자가 없는 경우 -1을 반환
    }

    private fun getDrawable(context: Context, resId: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, resId, null)
    }

    fun progressToHex(progress: Int): String {
        return if (progress == 0) "00" else if (progress == 100) "" else if (progress < 10) "0${progress}" else progress.toString()
    }
}