package app.airsignal.weather.db.sp

import android.content.Context
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:11
 **/
object SetSystemInfo {

    /** 폰트 크기를 작게 변경 **/
    fun setTextSizeSmall(context: Context) {
        val configuration = context.resources.configuration
        configuration.fontScale = 0.9f
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
        configuration.fontScale = 1.1f
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

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
}