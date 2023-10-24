package app.airsignal.weather.util.`object`

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import app.airsignal.weather.R
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:11
 **/
object SetSystemInfo {

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

    /** UV 범주 색상 적용 **/
    fun setUvBackgroundColor(context: Context, flag: String, cardView: CardView) {
        val flagMap = mapOf(
            "낮음" to R.color.uv_low,
            "보통" to R.color.uv_normal,
            "높음" to R.color.uv_high,
            "매우높음" to R.color.uv_very_high,
            "위험" to R.color.uv_caution
        )

        return flagMap[flag]?.let { cardView.setCardBackgroundColor(ColorStateList.valueOf(it))}!!
    }

    /** 상태 바 설정 **/
    @Suppress("DEPRECATION")
    fun setStatusBar(activity: Activity) {
        activity.window.apply {
            statusBarColor = activity.getColor(R.color.theme_view_color)
            navigationBarColor = activity.getColor(android.R.color.transparent)

            this.decorView.systemUiVisibility =
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                    this.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}