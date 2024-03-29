package app.airsignal.weather.db.sp

import android.content.Context
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.sp.SpDao.INITIALIZED_LOC_PERMISSION
import app.airsignal.weather.db.sp.SpDao.INITIALIZED_NOTI_PERMISSION
import app.airsignal.weather.db.sp.SpDao.IN_APP_MSG_TIME
import app.airsignal.weather.db.sp.SpDao.IS_PERMED_BACK_LOG
import app.airsignal.weather.db.sp.SpDao.LANDING_NOTIFICATION
import app.airsignal.weather.db.sp.SpDao.LAST_REFRESH22
import app.airsignal.weather.db.sp.SpDao.LAST_REFRESH42
import app.airsignal.weather.db.sp.SpDao.NOTIFICATION_ADDRESS
import app.airsignal.weather.db.sp.SpDao.NOTIFICATION_TOPIC_DAILY
import app.airsignal.weather.db.sp.SpDao.WARNING_FIXED
import app.airsignal.weather.db.sp.SpDao.lastAddress
import app.airsignal.weather.db.sp.SpDao.lastLoginPlatform
import app.airsignal.weather.db.sp.SpDao.notiEnable
import app.airsignal.weather.db.sp.SpDao.notiSound
import app.airsignal.weather.db.sp.SpDao.notiVibrate
import app.airsignal.weather.db.sp.SpDao.userEmail
import app.airsignal.weather.db.sp.SpDao.userFontScale
import app.airsignal.weather.db.sp.SpDao.userLocation
import app.airsignal.weather.db.sp.SpDao.userProfile
import java.text.SimpleDateFormat
import java.util.*

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
        return SharedPreferenceManager(context).getBoolean(notiEnable, true)
    }

    fun getUserNotiVibrate(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(notiVibrate, true)
    }

    fun getUserNotiSound(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(notiSound, false)
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

    fun getTopicNotification(context: Context): String {
        return SharedPreferenceManager(context).getString(NOTIFICATION_TOPIC_DAILY)
    }

    fun getEntireSun(sunRise: String, sunSet: String): Int {
        val sunsetTime = parseTimeToMinutes(sunSet)
        val sunriseTime = parseTimeToMinutes(sunRise)
        return sunsetTime - sunriseTime
    }

    fun getCurrentSun(sunRise: String, sunSet: String): Int {
        val currentTime = millsToString(System.currentTimeMillis(), "HHmm")
        val degreeToSunRise =
            parseTimeToMinutes(currentTime) - parseTimeToMinutes(sunRise)
        val currentSun = if (degreeToSunRise < 0) {
            ((100 * (degreeToSunRise + 2400)) / getEntireSun(sunRise, sunSet)) % 100 + 100
        } else { (100 * degreeToSunRise) / getEntireSun(sunRise, sunSet) }

        return currentSun
    }

    /** 데이터 포멧에 맞춰서 시간변환 **/
    fun millsToString(mills: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(mills))
    }
    
    /** HH:mm 포맷의 시간을 분으로 변환 **/
    fun parseTimeToMinutes(time: String): Int {
        return try {
            val timeSplit = time.replace(" ", "")
            val hour = timeSplit.substring(0, 2).toInt()
            val minutes = timeSplit.substring(2, 4).toInt()
            hour * 60 + minutes
        } catch (e: java.lang.NumberFormatException) { 1 }
    }

    fun getIsNight(progress: Int): Boolean { return progress >= 100 || progress < 0 }

    fun getIsNight(sunrise: String, sunset: String): Boolean {
        val dailySunProgress = 100 * (parseTimeToMinutes(
            millsToString(System.currentTimeMillis(), "HHmm")
        ) - parseTimeToMinutes(sunrise))/
                if (getEntireSun(sunrise, sunset) == 0) 1
                else getEntireSun(sunrise, sunset)
        return dailySunProgress >= 100 || dailySunProgress < 0
    }

    fun getNotificationAddress(context: Context): String {
        return SharedPreferenceManager(context).getString(NOTIFICATION_ADDRESS)
    }

    fun getInitLocPermission(context: Context): String {
        return SharedPreferenceManager(context).getString(INITIALIZED_LOC_PERMISSION)
    }

    fun getInitNotiPermission(context: Context): String {
        return SharedPreferenceManager(context).getString(INITIALIZED_NOTI_PERMISSION)
    }

    fun isPermedBackLoc(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(IS_PERMED_BACK_LOG, false)
    }

    fun getWarningFixed(context: Context): String {
        return SharedPreferenceManager(context).getString(WARNING_FIXED)
    }

    fun getLastRefreshTime42(context: Context): Long {
        return SharedPreferenceManager(context).getLong(LAST_REFRESH42)
    }

    fun getLastRefreshTime22(context: Context): Long {
        return SharedPreferenceManager(context).getLong(LAST_REFRESH22)
    }

    fun isLandingNotification(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(LANDING_NOTIFICATION, false)
    }

    fun getInAppMsgEnabled(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(SpDao.IN_APP_MSG, false)
    }

    fun getInAppMsgTime(context: Context): Long {
        return SharedPreferenceManager(context).getLong(IN_APP_MSG_TIME)
    }
}