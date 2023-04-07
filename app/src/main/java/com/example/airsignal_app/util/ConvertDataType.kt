package com.example.airsignal_app.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import java.text.SimpleDateFormat
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
}