package com.example.airsignal_app.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-03-20 오후 4:22
 **/
object ConvertDataType {
    /** 국가를 대한민국으로 설정합니다 **/
    fun setLocaleToKorea(context: Context) {
        val configuration = Configuration()
        configuration.setLocale(Locale.KOREA)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /** 국가를 영어권으로 설정합니다 **/
    fun setLocaleToEnglish(context: Context) {
        val configuration = Configuration()
        configuration.setLocale(Locale.ENGLISH)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /** 국가를 시스템으로 설정합니다 **/
    fun setLocaleToSystem(context: Context) {
        val configuration = Configuration()
        configuration.setLocale(Locale.getDefault())
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
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

    /** 데이터 포멧에 맞춰서 시간변환 **/
    fun millsToString(mills: Long, pattern: String): String {
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat(pattern, Locale.KOREA)
        return format.format(Date(mills))
    }

    /** Email을 RealtimeDB의 child 형식에 맞게 변환**/
    fun formatEmailToRDB(email: String) : String {
        return email.replace(".", "_")
    }

    /** sky value에 따른 이미지 설정 **/
    fun getSkyImg(context: Context, sky: String?) : Drawable? {
        when(sky) {
            "맑음" -> {
                return ResourcesCompat.getDrawable(context.resources, R.drawable.sunny_test,null)
            }
            "구름많음" -> {
                return ResourcesCompat.getDrawable(context.resources, R.drawable.cloud2_test,null)
            }
            "흐림" -> {
                return ResourcesCompat.getDrawable(context.resources, R.drawable.cloud_test,null)
            }
            "비" -> {
                return ResourcesCompat.getDrawable(context.resources, R.drawable.rain_per,null)
            }
            "눈" -> {
                return ResourcesCompat.getDrawable(context.resources, R.drawable.snow_test,null)
            }
            "흐리고 비" -> {
                return ResourcesCompat.getDrawable(context.resources, R.drawable.rainy_test,null)
            }
            else -> {
                return ResourcesCompat.getDrawable(context.resources, R.drawable.cancel,null)
            }
        }
    }

    /** 등급에 따른 텍스트 변환 **/
    private fun getDataString(context: Context, grade: Int): String {
        return when (grade) {
            0 -> context.getString(R.string.progress_good)
            1 -> context.getString(R.string.progress_normal)
            2 -> context.getString(R.string.progress_bad)
            3 -> context.getString(R.string.progress_worst)
            else -> ""
        }
    }

    /** 등급에 따른 색상 변환 **/
    private fun getDataColor(context: Context, grade: Int): Int {
        return when (grade) {
            0 -> ResourcesCompat.getColor(context.resources, R.color.progressGood, null)
            1 -> ResourcesCompat.getColor(context.resources, R.color.progressNormal, null)
            2 -> ResourcesCompat.getColor(context.resources, R.color.progressBad, null)
            3 -> ResourcesCompat.getColor(context.resources, R.color.progressWorst, null)
            else -> ResourcesCompat.getColor(context.resources, R.color.progressError, null)
        }
    }

    /** 미세먼지 & 초미세먼지 글자 색 설정 **/
    fun settingSpan(context: Context, view: TextView, grade: Int) {
        val span = SpannableStringBuilder(getDataString(context,grade))
        span.setSpan(
            ForegroundColorSpan(getDataColor(context,grade)),
            0,
            span.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = span
    }

    /** Double을 지정 자릿수에서 반올림 **/
    fun convertDoubleToDecimal(double: Double, digit: Int) : String {
        return String.format("%.${digit}f",double)
    }

    // 요일 변환
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDayOfWeekToKorean(context: Context, datOfWeek: Int) : String {
        return when(datOfWeek % 7) {
            1-> context.getString(R.string.mon)
            2 -> context.getString(R.string.tue)
            3 -> context.getString(R.string.wen)
            4 -> context.getString(R.string.thr)
            5 -> context.getString(R.string.fir)
            6 -> context.getString(R.string.sat)
            0 -> context.getString(R.string.sun)
            else -> ""
        }
    }
}