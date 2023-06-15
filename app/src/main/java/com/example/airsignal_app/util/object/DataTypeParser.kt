package com.example.airsignal_app.util.`object`

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-03-20 오후 4:22
 **/
object DataTypeParser {

    /** pixel을 DP로 변환 **/
    fun pixelToDp(context: Context, px: Int): Int {
        return px / (context.resources.displayMetrics.densityDpi / 160)
    }

    /** 현재시간 불러오기 **/
    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    /** 위젯용 현재시간 타임포멧 **/
    fun currentDateTimeString(context: Context): String {
        @SuppressLint("SimpleDateFormat") val format =
            SimpleDateFormat(context.getString(R.string.widget_time_format))
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        return format.format(calendar.time)
    }

    /** 데이터 포멧에 맞춰서 시간변환 **/
    fun millsToString(mills: Long, pattern: String): String {
        @SuppressLint("SimpleDateFormat") val format =
            SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(Date(mills))
    }

    /** Email을 RealtimeDB의 child 형식에 맞게 변환**/
    fun formatEmailToRDB(email: String): String {
        return email.replace(".", "_")
    }

    /** Current의 rainType의 에러 방지 **/
    fun modifyCurrentRainType(rainTypeCurrent: String?, rainTypeReal: String?): String? {
        return if (rainTypeCurrent != null) {
            when(rainTypeCurrent) {
                "비","눈","비/눈","소나기","없음" -> {
                    rainTypeCurrent
                }
                else -> rainTypeReal
            }
        } else {
            rainTypeReal
        }
    }

    /** Current의 Temperature의 에러 방지 **/
    fun modifyCurrentTempType(tempCurrent: Double?, tempReal: Double?): Double? {
        return if (tempCurrent != null) {
            if (tempCurrent < 50.0 && tempCurrent > -50.0) {
                return tempCurrent
            } else {
                return tempReal
            }
        } else {
            return tempReal
        }
    }

    /** rain type에 따른 이미지 설정 **/
    fun getRainTypeLarge(context: Context, rain: String?): Drawable? {
        return when (rain) {
            "비" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_cloudy, null)
            }
            "눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.snow_test, null)
            }
            "비/눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_snow, null)
            }
            "소나기" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_cloudy, null)
            }
            else -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.cancel, null)
            }
        }
    }

    /** sky value에 따른 이미지 설정 **/
    fun getSkyImgLarge(context: Context, sky: String?, isNight: Boolean): Drawable? {
        return when (sky) {
            "맑음" -> {
                if (!isNight) {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.ico_sunny, null)
                } else {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.main_moon, null)
                }
            }
            "구름많음" -> {
                if (!isNight) {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.lg_cloudy, null)
                } else {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.lg_cloudy, null)
                }
            }
            "흐림" -> {
                if (!isNight) {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.lg_more_cloudy, null)
                } else {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.lg_more_cloudy, null)
                }
            }
            "소나기", "비" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_cloudy, null)
            }
            "구름많고 눈", "눈", "흐리고 눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.snow_test, null)
            }
            "구름많고 소나기", "흐리고 비", "구름많고 비", "흐리고 소나기" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_cloudy, null)
            }
            "구름많고 비/눈", "흐리고 비/눈", "비/눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.snow_test, null)
            }
            else -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.cancel, null)
            }
        }
    }

    /** rain type에 따른 이미지 설정 **/
    fun getRainTypeSmall(context: Context, rain: String?): Drawable? {
        return when (rain) {
            "비" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sm_cloudrain, null)
            }
            "눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sm_snow, null)
            }
            "비/눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sm_cloudsnow, null)
            }
            "소나기" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sm_rainy, null)
            }
            else -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.cancel, null)
            }
        }
    }

    /** sky value에 따른 이미지 설정 **/
    fun getSkyImgSmall(context: Context, sky: String?, isNight: Boolean): Drawable? {
        return when (sky) {
            "맑음" -> {
                if (!isNight) {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.sm_good, null)
                } else {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.sm_good_n, null)
                }
            }
            "구름많음" -> {
                if (!isNight) {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.sm_cloudy, null)
                } else {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.sm_cloudy_n, null)
                }
            }
            "흐림" -> {
                if (!isNight) {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.sm_more_cloudy, null)
                } else {
                    ResourcesCompat.getDrawable(context.resources, R.drawable.sm_more_cloudy_n, null)
                }
            }
            "소나기", "비" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sm_rainy, null)
            }
            "구름많고 눈", "눈", "흐리고 눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sm_snow, null)
            }
            "구름많고 소나기", "흐리고 비", "구름많고 비", "흐리고 소나기" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sm_cloudrain, null)
            }
            "구름많고 비/눈", "흐리고 비/눈", "비/눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sm_cloudsnow, null)
            }
            else -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.cancel, null)
            }
        }
    }

    /** 등급에 따른 색상 변환 **/
    fun getDataColor(context: Context, grade: Int): Int {
        return when (grade) {
            0 -> ResourcesCompat.getColor(context.resources, R.color.air_good, null)
            1 -> ResourcesCompat.getColor(context.resources, R.color.air_normal, null)
            2 -> ResourcesCompat.getColor(context.resources, R.color.air_bad, null)
            3 -> ResourcesCompat.getColor(context.resources, R.color.air_very_bad , null)
            else -> ResourcesCompat.getColor(context.resources, R.color.progressError, null)
        }
    }

    /** Double을 지정 자릿수에서 반올림 **/
    fun convertDoubleToDecimal(double: Double, digit: Int): String {
        return String.format("%.${digit}f", double)
    }

    /** 요일 변환 **/
    fun convertDayOfWeekToKorean(context: Context, dayOfWeek: Int): String {
        return when (dayOfWeek % 7) {
            1 -> context.getString(R.string.mon)
            2 -> context.getString(R.string.tue)
            3 -> context.getString(R.string.wen)
            4 -> context.getString(R.string.thr)
            5 -> context.getString(R.string.fir)
            6 -> context.getString(R.string.sat)
            0 -> context.getString(R.string.sun)
            else -> ""
        }
    }

    /** 주소 포멧팅 **/
    fun convertAddress(addr: String): String {
        return addr.replace("특별시", "시").replace("광역시", "시")
            .replace("제주특별자치도", "제주도")
    }

    /** HH:mm 포맷의 시간을 분으로 변환 **/
    fun convertTimeToMinutes(time: String): Int {
        return try {
            val timeSplit = time.replace(" ", "")
            val hour = timeSplit.substring(0, 2).toInt()
            val minutes = timeSplit.substring(2, 4).toInt()
            hour * 60 + minutes
        } catch (e: java.lang.NumberFormatException) {
            e.printStackTrace()
            0
        }
    }

    /** 하늘상태를 국가에 맞게 변경 **/
    fun translateSky(context: Context, sky: String): String {
        return when(sky) {
            "맑음" -> {context.getString(R.string.sky_sunny)}
            "구름많음" -> {context.getString(R.string.sky_sunny_cloudy)}
            "흐림" -> {context.getString(R.string.sky_cloudy)}
            "소나기" -> {context.getString(R.string.sky_shower)}
            "비" -> {context.getString(R.string.sky_rainy)}
            "구름많고 눈" -> {context.getString(R.string.sky_sunny_cloudy_snowy)}
            "눈" -> {context.getString(R.string.sky_snowy)}
            "흐리고 눈" -> {context.getString(R.string.sky_cloudy_snowy)}
            "구름많고 소나기" -> {context.getString(R.string.sky_sunny_cloudy_shower)}
            "흐리고 비" -> {context.getString(R.string.sky_cloudy_rainy)}
            "구름많고 비" -> {context.getString(R.string.sky_sunny_cloudy_rainy)}
            "흐리고 소나기" -> {context.getString(R.string.sky_cloudy_shower)}
            "구름많고 비/눈" -> {context.getString(R.string.sky_sunny_cloudy_rainy_snowy)}
            "흐리고 비/눈" -> {context.getString(R.string.sky_cloudy_rainy_snowy)}
            "비/눈" -> {context.getString(R.string.sky_rainy_snowy)}
            context.getString(R.string.thunder_sunny) -> {context.getString(R.string.thunder_sunny)}
            context.getString(R.string.thunder_rainy) -> {context.getString(R.string.thunder_rainy)}
            else -> {""}
        }
    }

    /** 자외선 지수 번역 **/
    fun translateUV(context: Context, uv: String): String {
        return when(uv) {
            "낮음" -> {context.getString(R.string.uv_low)}
            "보통" -> {context.getString(R.string.uv_normal)}
            "높음" -> {context.getString(R.string.uv_high)}
            "매우높음" -> {context.getString(R.string.uv_very_high)}
            "위험" -> {context.getString(R.string.uv_caution)}
            else -> {""}
        }
    }

    /** LocalDateTime을 Long으로 파싱 **/
    fun convertLocalDateTimeToLong(localDateTime: LocalDateTime): Long {
        return localDateTime.atZone(
            ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}