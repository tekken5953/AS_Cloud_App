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
        configuration.fontScale = 1.2f
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

    fun updateConfiguration(context: Context, locale: Locale) {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }
}