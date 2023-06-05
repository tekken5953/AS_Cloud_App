package com.example.airsignal_app.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.userLocation
import com.example.airsignal_app.db.SharedPreferenceManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-03-20 오후 4:22
 **/
object ConvertDataType {
    /** 국가를 대한민국으로 설정합니다 **/
    fun setLocaleToKorea(context: Context) {
        val configuration = context.resources.configuration
        configuration.setLocale(Locale.KOREA)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /** 국가를 영어권으로 설정합니다 **/
    fun setLocaleToEnglish(context: Context) {
        val configuration = context.resources.configuration
        configuration.setLocale(Locale.ENGLISH)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /** 국가를 시스템으로 설정합니다 **/
    fun setLocaleToSystem(context: Context) {
        val configuration = context.resources.configuration
        configuration.setLocale(Locale.getDefault())
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /** 폰트 크기를 작게 변경 **/
    fun setTextSizeSmall(context: Context) {
        val configuration = context.resources.configuration
        configuration.fontScale = 0.7f
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /** 폰트 크기를 크게 변경 **/
    fun setTextSizeLarge(context: Context) {
        val configuration = context.resources.configuration
        configuration.fontScale = 1.3f
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /** 폰트 크기를 기본으로 변경 **/
    fun setTextSizeDefault(context: Context) {
        val configuration = context.resources.configuration
        configuration.fontScale = 1f
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /** pixel을 DP로 변환 **/
    fun pixelToDp(context: Context, px: Int): Int {
        return px / (context.resources.displayMetrics.densityDpi / 160)
    }

    /** 화면을 풀 스크린으로 사용합니다 **/
    @Suppress("DEPRECATION")
    fun setFullScreenMode(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
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

    /** rain type에 따른 이미지 설정 **/
    fun getRainType(context: Context, rain: String?): Drawable? {
        return when (rain) {
            "비" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rainy_test, null)
            }
            "눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.snow_test, null)
            }
            "비/눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_snow, null)
            }
            "소나기" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_per, null)
            }
            else -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.cancel, null)
            }
        }
    }

    /** sky value에 따른 이미지 설정 **/
    fun getSkyImg(context: Context, sky: String?): Drawable? {
        return when (sky) {
            "맑음" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.sunny_test, null)
            }
            "구름많음" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.ico_cloud, null)
            }
            "흐림" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.ico_cloud, null)
            }
            "소나기", "비" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_per, null)
            }
            "구름많고 눈", "눈", "흐리고 눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.snow, null)
            }
            "구름많고 소나기", "흐리고 비", "구름많고 비", "흐리고 소나기" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_cloudy, null)
            }
            "구름많고 비/눈", "흐리고 비/눈", "비/눈" -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.rain_snow, null)
            }
            else -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.cancel, null)
            }
        }
    }

//    /** 등급에 따른 텍스트 변환 **/
//    private fun getDataString(context: Context, grade: Int): String {
//        return when (grade) {
//            0 -> context.getString(R.string.progress_good)
//            1 -> context.getString(R.string.progress_normal)
//            2 -> context.getString(R.string.progress_bad)
//            3 -> context.getString(R.string.progress_worst)
//            else -> ""
//        }
//    }

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

//    /** Double을 지정 자릿수에서 반올림 **/
//    fun convertDoubleToDecimal(double: Double, digit: Int): String {
//        return String.format("%.${digit}f", double)
//    }

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
        return addr.replace("특별시", "시").replace("광역시", "시").replace("제주특별자치도", "제주도")
    }

    /** 현재 설정된 국가를 반환 **/
    fun getLocale(context: Context): Locale {
        return if (SharedPreferenceManager(context).getString(userLocation) == context.getString(R.string.korean)) {
            Locale.KOREA
        } else if (SharedPreferenceManager(context).getString(userLocation) == context.getString(R.string.english)) {
            Locale.ENGLISH
        } else {
            Locale.getDefault()
        }
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
}