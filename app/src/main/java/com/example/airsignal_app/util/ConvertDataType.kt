package com.example.airsignal_app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-03-20 오후 4:22
 **/
class ConvertDataType {
    // 국가를 대한민국으로 설정합니다
    fun setLocaleToKorea(context: Context) {
        val configuration = Configuration()
        configuration.setLocale(Locale.KOREA)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    // 국가를 영어권으로 설정합니다
    fun setLocaleToEnglish(context: Context) {
        val configuration = Configuration()
        configuration.setLocale(Locale.ENGLISH)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    // 국가를 시스템으로 설정합니다
    fun setLocaleToSystem(context: Context) {
        val configuration = Configuration()
        configuration.setLocale(Locale.getDefault())
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    // 화면을 풀 스크린으로 사용합니다
    @Suppress("DEPRECATION")
    fun setFullScreenMode(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}