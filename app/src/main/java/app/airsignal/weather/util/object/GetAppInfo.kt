package app.airsignal.weather.util.`object`

import android.content.Context
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.dao.IgnoredKeyFile.lastAddress
import app.airsignal.weather.dao.IgnoredKeyFile.lastLoginPlatform
import app.airsignal.weather.dao.IgnoredKeyFile.notiEnable
import app.airsignal.weather.dao.IgnoredKeyFile.notiSound
import app.airsignal.weather.dao.IgnoredKeyFile.notiVibrate
import app.airsignal.weather.dao.IgnoredKeyFile.userEmail
import app.airsignal.weather.dao.IgnoredKeyFile.userFontScale
import app.airsignal.weather.dao.IgnoredKeyFile.userLocation
import app.airsignal.weather.dao.IgnoredKeyFile.userProfile
import app.airsignal.weather.dao.StaticDataObject.CURRENT_GPS_ID
import app.airsignal.weather.dao.StaticDataObject.INITIALIZED_LOC_PERMISSION
import app.airsignal.weather.dao.StaticDataObject.INITIALIZED_NOTI_PERMISSION
import app.airsignal.weather.dao.StaticDataObject.IS_BACK_LOCATION_ENABLE
import app.airsignal.weather.dao.StaticDataObject.LAST_REFRESH_WIDGET_TIME
import app.airsignal.weather.dao.StaticDataObject.NOTIFICATION_ADDRESS
import app.airsignal.weather.db.SharedPreferenceManager

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

    fun getUserNotiEnable(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(notiEnable)
    }

    fun getUserNotiVibrate(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(notiVibrate)
    }

    fun getUserNotiSound(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(notiSound)
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
        val currentTime = DataTypeParser.millsToString(
            DataTypeParser.getCurrentTime(),
            "HHmm"
        )
        val degreeToSunRise =
            DataTypeParser.convertTimeToMinutes(currentTime) - DataTypeParser.convertTimeToMinutes(
                sunRise
            )
        val currentSun = if (degreeToSunRise < 0) {
            ((100 * (degreeToSunRise + 2400)) / getEntireSun(sunRise, sunSet)) % 100 + 100
        } else {
            (100 * degreeToSunRise) / getEntireSun(sunRise, sunSet)
        }
        return currentSun
    }

    fun getIsNight(progress: Int): Boolean {
        return progress >= 100 || progress < 0
    }

    fun getNotificationAddress(context: Context): String {
        return SharedPreferenceManager(context).getString(NOTIFICATION_ADDRESS)
    }

    fun getLastRefreshTime(context: Context): Long {
        return SharedPreferenceManager(context).getLong(LAST_REFRESH_WIDGET_TIME)
    }

    fun getInitLocPermission(context: Context): String {
        return SharedPreferenceManager(context).getString(INITIALIZED_LOC_PERMISSION)
    }

    fun getInitNotiPermission(context: Context): String {
        return SharedPreferenceManager(context).getString(INITIALIZED_NOTI_PERMISSION)
    }

    fun getCurrentLocation(context: Context): String {
        return SharedPreferenceManager(context).getString(CURRENT_GPS_ID)
    }

    fun getBackLocationEnable(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(IS_BACK_LOCATION_ENABLE)
    }
}