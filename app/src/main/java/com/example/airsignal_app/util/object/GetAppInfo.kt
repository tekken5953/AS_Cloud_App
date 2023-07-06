package com.example.airsignal_app.util.`object`

import android.content.Context
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.dao.IgnoredKeyFile.notiEvent
import com.example.airsignal_app.dao.IgnoredKeyFile.notiNight
import com.example.airsignal_app.dao.IgnoredKeyFile.notiPM
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.IgnoredKeyFile.userFontScale
import com.example.airsignal_app.dao.IgnoredKeyFile.userLocation
import com.example.airsignal_app.dao.IgnoredKeyFile.userProfile
import com.example.airsignal_app.dao.StaticDataObject.WEATHER_ALL_NOTI
import com.example.airsignal_app.db.SharedPreferenceManager

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:19
 **/
object GetAppInfo {
    fun getUserTheme(context: Context): String {
        return SharedPreferenceManager(context).getString("theme")
    }

    fun getUserEmail(context: Context): String {
        return SharedPreferenceManager(context).getString(userEmail)
    }

    fun getUserLocation(context: Context): String {
        return SharedPreferenceManager(context).getString(userLocation)
    }

    fun getUserFontScale(context: Context): String {
        return SharedPreferenceManager(context).getString(userFontScale)
    }

    fun getUserNotiPM(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(notiPM)
    }

    fun getUserNotiEvent(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(notiEvent)
    }

    fun getUserNotiNight(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(notiNight)
    }

    fun getUserLoginPlatform(context: Context): String {
        return SharedPreferenceManager(context).getString(lastLoginPlatform)
    }

    fun getUserLastAddress(context: Context): String {
        return SharedPreferenceManager(context).getString(lastAddress)
    }

    fun getUserProfileImage(context: Context): String {
        return SharedPreferenceManager(context).getString(userProfile)
    }

    fun getLoginVerificationCode(context: Context): String {
        return SharedPreferenceManager(context).getString(IgnoredKeyFile.loginVerificationCode)
    }

    fun getTopicNotification(context: Context): String {
        return SharedPreferenceManager(context).getString("Notification_All")
    }

    fun getEntireSun(sunRise: String, sunSet: String): Int {
        val sunsetTime = DataTypeParser.convertTimeToMinutes(sunSet)
        val sunriseTime = DataTypeParser.convertTimeToMinutes(sunRise)
        return sunsetTime - sunriseTime
    }

    fun getCurrentSun(sunRise: String, sunSet: String): Int {
        val currentTime = DataTypeParser.millsToString(DataTypeParser.getCurrentTime(), "HHmm")
        var currentSun =
            (100 * (DataTypeParser.convertTimeToMinutes(currentTime) - DataTypeParser.convertTimeToMinutes(
                sunRise
            ))) / getEntireSun(sunRise,sunSet)

        if (currentSun > 100) { currentSun = 100 }

        return currentSun
    }

    fun getIsNight(progress: Int): Boolean {
        return progress >= 100 || progress < 0
    }
}