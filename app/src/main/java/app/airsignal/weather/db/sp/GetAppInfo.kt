package app.airsignal.weather.db.sp

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:19
 **/
object GetAppInfo {

    fun getUserTheme(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.userTheme)
    }

    fun getUserEmail(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.userEmail)
    }

    fun getUserLocation(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.userLocation)
    }

    fun getUserFontScale(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.userFontScale)
    }

    fun getUserNotiEnable(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(SpDao.notiEnable, true)
    }

    fun getUserNotiVibrate(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(SpDao.notiVibrate, true)
    }

    fun getUserNotiSound(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(SpDao.notiSound, false)
    }

    fun getUserLoginPlatform(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.lastLoginPlatform)
    }

    fun getUserLastAddress(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.lastAddress)
    }

    fun getUserProfileImage(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.userProfile)
    }

    fun getTopicNotification(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.NOTIFICATION_TOPIC_DAILY)
    }

    private fun getEntireSun(sunRise: String, sunSet: String): Int {
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
    private fun millsToString(mills: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(mills))
    }
    
    /** HH:mm 포맷의 시간을 분으로 변환 **/
    private fun parseTimeToMinutes(time: String): Int {
        return kotlin.runCatching {
            val timeSplit = time.replace(" ","")
            val hour = timeSplit.substring(0, 2).toInt()
            val minutes = timeSplit.substring(2, 4).toInt()
            hour * 60 + minutes
        }.getOrElse { 1 }
    }

    fun getIsNight(sunrise: String, sunset: String): Boolean {
        val dailySunProgress = 100 * (parseTimeToMinutes(
            millsToString(System.currentTimeMillis(), "HHmm")
        ) - parseTimeToMinutes(sunrise))/
                if (getEntireSun(sunrise, sunset) == 0) 1
                else getEntireSun(sunrise, sunset)
        return dailySunProgress >= 100 || dailySunProgress < 0
    }

    fun getNotificationAddress(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.NOTIFICATION_ADDRESS)
    }

    fun getInitLocPermission(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.INITIALIZED_LOC_PERMISSION)
    }

    fun getInitNotiPermission(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.INITIALIZED_NOTI_PERMISSION)
    }

    fun isPermedBackLoc(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(SpDao.IS_PERMED_BACK_LOG, false)
    }

    fun getWarningFixed(context: Context): String {
        return SharedPreferenceManager(context).getString(SpDao.WARNING_FIXED)
    }

    fun getLastRefreshTime42(context: Context): Long {
        return SharedPreferenceManager(context).getLong(SpDao.LAST_REFRESH42)
    }

    fun getLastRefreshTime22(context: Context): Long {
        return SharedPreferenceManager(context).getLong(SpDao.LAST_REFRESH22)
    }

    fun getInAppMsgEnabled(context: Context): Boolean {
        return SharedPreferenceManager(context).getBoolean(SpDao.IN_APP_MSG, false)
    }

    fun getInAppMsgTime(context: Context): Long {
        return SharedPreferenceManager(context).getLong(SpDao.IN_APP_MSG_TIME)
    }

    fun getWeatherBoxOpacity(context: Context): Int {
        return SharedPreferenceManager(context).getInt(SpDao.WEATHER_BOX_OPACITY, 60)
    }

    fun getWeatherBoxOpacity2(context: Context): Int {
        return SharedPreferenceManager(context).getInt(SpDao.WEATHER_BOX_OPACITY2, 60)
    }
}