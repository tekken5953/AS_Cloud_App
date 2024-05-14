package app.airsignal.weather.db.sp

import android.content.Context

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:28
 **/
object SetAppInfo {

    fun setUserFontScale(context: Context, type: String) {
        SharedPreferenceManager(context).setString(SpDao.userFontScale, type)
    }

    fun setUserLocation(context: Context, location: String) {
        SharedPreferenceManager(context).setString(SpDao.userLocation, location)
    }

    fun setUserTheme(context: Context, theme: String) {
        SharedPreferenceManager(context).setString(SpDao.userTheme, theme)
    }

    fun setUserNoti(context: Context, tag: String, boolean: Boolean) {
        SharedPreferenceManager(context).setBoolean(tag, boolean)
    }

    fun setUserLastAddr(context: Context, addr: String) {
        SharedPreferenceManager(context).setString(SpDao.lastAddress, addr)
    }

    fun setUserEmail(context: Context, email: String) {
        SharedPreferenceManager(context).setString(SpDao.userEmail, email)
    }

    fun setUserProfile(context: Context, profile: String) {
        SharedPreferenceManager(context).setString(SpDao.userProfile, profile)
    }

    fun setUserId(context: Context, id: String) {
        SharedPreferenceManager(context).setString(SpDao.userId, id)
    }

    fun removeAllKeys(context: Context) {
        SharedPreferenceManager(context)
            .run {
                removeKey(SpDao.userId)
                removeKey(SpDao.userProfile)
                removeKey(SpDao.lastLoginPhone)
                removeKey(SpDao.lastLoginPlatform)
                removeKey(SpDao.userEmail)
            }
    }

    fun removeSingleKey(context: Context, key: String) {
        SharedPreferenceManager(context).removeKey(key)
    }

    fun setUserLoginPlatform(context: Context, platform: String) {
        SharedPreferenceManager(context).setString(SpDao.lastLoginPlatform, platform)
    }

    fun setTopicNotification(context: Context, topic: String) {
        SharedPreferenceManager(context).setString(SpDao.NOTIFICATION_TOPIC_DAILY, topic)
    }

    fun setNotificationAddress(context: Context, addr: String) {
        SharedPreferenceManager(context).setString(SpDao.NOTIFICATION_ADDRESS, addr)
    }

    fun setInitLocPermission(context: Context, s: String) {
        SharedPreferenceManager(context).setString(SpDao.INITIALIZED_LOC_PERMISSION, s)
    }

    fun setInitNotiPermission(context: Context, s: String) {
        SharedPreferenceManager(context).setString(SpDao.INITIALIZED_NOTI_PERMISSION, s)
    }

    fun setInitBackLocPermission(context: Context,b: Boolean) {
        SharedPreferenceManager(context).setBoolean(SpDao.IS_INIT_BACK_LOC_PERMISSION, b)
    }

    fun setPermedBackLog(context: Context,b: Boolean) {
        SharedPreferenceManager(context).setBoolean(SpDao.IS_PERMED_BACK_LOG, b)
    }

    fun setLastRefreshTime42(context: Context, time: Long) {
        SharedPreferenceManager(context).setLong(SpDao.LAST_REFRESH42,time)
    }

    fun setLastRefreshTime22(context: Context, time: Long) {
        SharedPreferenceManager(context).setLong(SpDao.LAST_REFRESH22,time)
    }

    fun setInAppMsgDenied(context: Context, enabled: Boolean) {
        SharedPreferenceManager(context).setBoolean(SpDao.IN_APP_MSG, enabled)
        SharedPreferenceManager(context).setLong(SpDao.IN_APP_MSG_TIME, System.currentTimeMillis())
    }

    fun setWeatherBoxOpacity(context: Context, value: Int) {
        SharedPreferenceManager(context).setInt(SpDao.WEATHER_BOX_OPACITY, value)
    }

    fun setWeatherBoxOpacity2(context: Context, value: Int) {
        SharedPreferenceManager(context).setInt(SpDao.WEATHER_BOX_OPACITY2, value)
    }
}