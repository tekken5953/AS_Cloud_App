package com.example.airsignal_app.util.`object`

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import com.example.airsignal_app.R
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLocation
import timber.log.Timber
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:08
 **/
object GetSystemInfo {

    /** 현재 테마가 다크인가**/
    fun isThemeNight(context: Context): Boolean {
        val nightModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlag == Configuration.UI_MODE_NIGHT_YES
    }

    /** 현재 설정된 국가를 반환 **/
    fun getLocale(context: Context): Locale {
        return when (getUserLocation(context)) {
            context.getString(
                R.string.korean) -> {
                Locale.KOREA
            }
            context.getString(
                R.string.english) -> {
                Locale.ENGLISH
            }
            else -> {
                Locale.getDefault()
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun androidID(activity: Context): String {
        val id = Settings.Secure.getString(
            activity.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        Timber.tag("buildUserInfo").i(id)
        return id
    }
}