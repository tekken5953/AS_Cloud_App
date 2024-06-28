package app.airsignal.weather.db.sp

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:19
 **/
object GetAppInfo : KoinComponent {
    val context: Context by inject()
    private val sp: SharedPreferenceManager by inject()

    fun getUserTheme(): String = sp.getString(SpDao.USER_THEME)

    fun getUserEmail(): String = sp.getString(SpDao.USER_EMAIL)

    fun getUserLocation(): String = sp.getString(SpDao.USER_LOCATION)

    fun getUserFontScale(): String = sp.getString(SpDao.USER_FONT_SCALE)

    fun getUserNotiEnable(): Boolean = sp.getBoolean(SpDao.NOTI_ENABLE, true)

    fun getUserNotiVibrate(): Boolean = sp.getBoolean(SpDao.NOTI_VIBRATE, true)

    fun getUserNotiSound(): Boolean = sp.getBoolean(SpDao.NOTI_SOUND, false)

    fun getUserLoginPlatform(): String = sp.getString(SpDao.LAST_LOGIN_PLATFORM)

    fun getUserLastAddress(): String = sp.getString(SpDao.LAST_ADDRESS)

    fun getUserProfileImage(): String = sp.getString(SpDao.USER_PROFILE)

    fun getTopicNotification(): String = sp.getString(SpDao.NOTIFICATION_TOPIC_DAILY)

    fun getNotificationAddress(): String = sp.getString(SpDao.NOTIFICATION_ADDRESS)

    fun getInitLocPermission(): String = sp.getString(SpDao.INITIALIZED_LOC_PERMISSION)

    fun getInitNotiPermission(): String = sp.getString(SpDao.INITIALIZED_NOTI_PERMISSION)

    fun isPermedBackLoc(): Boolean = sp.getBoolean(SpDao.IS_PERMED_BACK_LOG, false)

    fun getWarningFixed(): String = sp.getString(SpDao.WARNING_FIXED)

    fun getLastRefreshTime42(): Long = sp.getLong(SpDao.LAST_REFRESH42)

    fun getLastRefreshTime22(): Long = sp.getLong(SpDao.LAST_REFRESH22)

    fun getInAppMsgEnabled(): Boolean = sp.getBoolean(SpDao.IN_APP_MSG, false)

    fun getInAppMsgTime(): Long = sp.getLong(SpDao.IN_APP_MSG_TIME)

    fun getWeatherBoxOpacity(): Int = sp.getInt(SpDao.WEATHER_BOX_OPACITY, 60)

    fun getWeatherBoxOpacity2(): Int = sp.getInt(SpDao.WEATHER_BOX_OPACITY2, 60)

    private fun getEntireSun(sunRise: String, sunSet: String): Int =
        parseTimeToMinutes(sunSet) - parseTimeToMinutes(sunRise)

    fun getCurrentSun(sunRise: String, sunSet: String): Int {
        val currentTime = millsToString(System.currentTimeMillis(), "HHmm")
        val degreeToSunRise =
            parseTimeToMinutes(currentTime) - parseTimeToMinutes(sunRise)

        return if (degreeToSunRise < 0) ((100 * (degreeToSunRise + 2400)) / getEntireSun(sunRise, sunSet)) % 100 + 100
        else (100 * degreeToSunRise) / getEntireSun(sunRise, sunSet)
    }

    /** 데이터 포멧에 맞춰서 시간변환 **/
    private fun millsToString(mills: Long, pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(mills))
    
    /** HH:mm 포맷의 시간을 분으로 변환 **/
    private fun parseTimeToMinutes(time: String): Int =
        kotlin.runCatching {
            val timeSplit = time.replace(" ","")
            val hour = timeSplit.substring(0, 2).toInt()
            val minutes = timeSplit.substring(2, 4).toInt()
            hour * 60 + minutes
        }.getOrElse { 1 }

    fun getIsNight(sunrise: String, sunset: String): Boolean {
        val dailySunProgress = 100 * (parseTimeToMinutes(millsToString(System.currentTimeMillis(),"HHmm"))
                - parseTimeToMinutes(sunrise))/ if (getEntireSun(sunrise, sunset) == 0) 1 else getEntireSun(sunrise, sunset)
        return dailySunProgress >= 100 || dailySunProgress < 0
    }
}