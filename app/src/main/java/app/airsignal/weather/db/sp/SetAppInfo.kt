package app.airsignal.weather.db.sp

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:28
 **/
object SetAppInfo : KoinComponent {
    val context: Context by inject()
    private val sp: SharedPreferenceManager by inject()
    private val ioThread = CoroutineScope(Dispatchers.IO)

    fun setUserFontScale(type: String) =
        ioThread.launch { sp.setString(SpDao.userFontScale, type) }

    fun setUserLocation(location: String) =
        ioThread.launch { sp.setString(SpDao.userLocation, location) }

    fun setUserTheme(theme: String) =
        ioThread.launch { sp.setString(SpDao.userTheme, theme) }

    fun setUserNoti(tag: String, boolean: Boolean) =
        ioThread.launch { sp.setBoolean(tag, boolean) }

    fun setUserLastAddr(addr: String) =
        ioThread.launch { sp.setString(SpDao.lastAddress, addr) }

    fun setUserEmail(email: String) =
        ioThread.launch { sp.setString(SpDao.userEmail, email) }

    fun setUserProfile(profile: String) =
        ioThread.launch { sp.setString(SpDao.userProfile, profile) }

    fun setUserId(id: String) =
        ioThread.launch { sp.setString(SpDao.userId, id) }

    fun setUserLoginPlatform(platform: String) =
        ioThread.launch { sp.setString(SpDao.lastLoginPlatform, platform) }

    fun setTopicNotification(topic: String) =
        ioThread.launch { sp.setString(SpDao.NOTIFICATION_TOPIC_DAILY, topic) }

    fun setNotificationAddress(addr: String) =
        ioThread.launch { sp.setString(SpDao.NOTIFICATION_ADDRESS, addr) }

    fun setInitLocPermission(s: String) =
        ioThread.launch { sp.setString(SpDao.INITIALIZED_LOC_PERMISSION, s) }

    fun setInitNotiPermission(s: String) =
        ioThread.launch { sp.setString(SpDao.INITIALIZED_NOTI_PERMISSION, s) }

    fun setInitBackLocPermission(b: Boolean) =
        ioThread.launch { sp.setBoolean(SpDao.IS_INIT_BACK_LOC_PERMISSION, b) }

    fun setPermedBackLog(b: Boolean) =
        ioThread.launch { sp.setBoolean(SpDao.IS_PERMED_BACK_LOG, b) }

    fun setLastRefreshTime42(time: Long) =
        ioThread.launch { sp.setLong(SpDao.LAST_REFRESH42,time) }

    fun setLastRefreshTime22(time: Long) =
        ioThread.launch { sp.setLong(SpDao.LAST_REFRESH22,time) }

    fun setInAppMsgDenied(enabled: Boolean) {
        ioThread.launch {
            sp.setBoolean(SpDao.IN_APP_MSG, enabled)
            sp.setLong(SpDao.IN_APP_MSG_TIME, System.currentTimeMillis())
        }
    }

    fun setWeatherBoxOpacity(value: Int) =
        ioThread.launch { sp.setInt(SpDao.WEATHER_BOX_OPACITY, value) }

    fun setWeatherBoxOpacity2(value: Int) =
        ioThread.launch { sp.setInt(SpDao.WEATHER_BOX_OPACITY2, value) }

    fun removeSingleKey(key: String) =
        ioThread.launch { sp.removeKey(key) }

    fun removeAllKeys() =
        ioThread.launch {
            sp.run {
                removeKey(SpDao.userId)
                removeKey(SpDao.userProfile)
                removeKey(SpDao.lastLoginPhone)
                removeKey(SpDao.lastLoginPlatform)
                removeKey(SpDao.userEmail)
            }
        }
}