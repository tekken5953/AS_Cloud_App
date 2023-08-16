package app.airsignal.weather.util.`object`

import android.app.Activity
import android.content.Context
import android.view.View
import app.airsignal.weather.dao.IgnoredKeyFile.lastAddress
import app.airsignal.weather.dao.IgnoredKeyFile.lastLoginPhone
import app.airsignal.weather.dao.IgnoredKeyFile.lastLoginPlatform
import app.airsignal.weather.dao.IgnoredKeyFile.loginVerificationCode
import app.airsignal.weather.dao.IgnoredKeyFile.userEmail
import app.airsignal.weather.dao.IgnoredKeyFile.userFontScale
import app.airsignal.weather.dao.IgnoredKeyFile.userId
import app.airsignal.weather.dao.IgnoredKeyFile.userLocation
import app.airsignal.weather.dao.IgnoredKeyFile.userProfile
import app.airsignal.weather.dao.StaticDataObject.CURRENT_GPS_ID
import app.airsignal.weather.dao.StaticDataObject.INITIALIZED_LOC_PERMISSION
import app.airsignal.weather.dao.StaticDataObject.INITIALIZED_NOTI_PERMISSION
import app.airsignal.weather.dao.StaticDataObject.LAST_REFRESH_WIDGET_TIME
import app.airsignal.weather.dao.StaticDataObject.NOTIFICATION_ADDRESS
import app.airsignal.weather.db.SharedPreferenceManager

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:28
 **/
object SetAppInfo {

    /** 몰입모드로 전환됩니다 **/
    fun fullScreenMode(activity: Activity) {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    fun setUserFontScale(context: Context, type: String) {
        SharedPreferenceManager(context).setString(userFontScale, type)
    }

    fun setUserLocation(context: Context, location: String) {
        SharedPreferenceManager(context).setString(userLocation, location)
    }

    fun setUserTheme(context: Context, theme: String) {
        SharedPreferenceManager(context).setString("theme", theme)
    }

    fun setUserNoti(context: Context, tag: String, boolean: Boolean) {
        SharedPreferenceManager(context).setBoolean(tag, boolean)
    }

    fun setUserLastAddr(context: Context, addr: String) {
        SharedPreferenceManager(context).setString(lastAddress, addr)
    }

    fun setUserEmail(context: Context, email: String) {
        SharedPreferenceManager(context).setString(userEmail, email)
    }

    fun setUserProfile(context: Context, profile: String) {
        SharedPreferenceManager(context).setString(userProfile, profile)
    }

    fun setUserId(context: Context, id: String) {
        SharedPreferenceManager(context).setString(userId, id)
    }

    fun removeAllKeys(context: Context) {
        SharedPreferenceManager(context)
            .run {
                removeKey(userId)
                removeKey(userProfile)
                removeKey(lastLoginPhone)
                removeKey(lastLoginPlatform)
                removeKey(userEmail)
            }
    }

    fun removeSingleKey(context: Context, key: String) {
        SharedPreferenceManager(context)
            .removeKey(key)
    }

    fun setUserLoginPlatform(context: Context, platform: String) {
        SharedPreferenceManager(context).setString(lastLoginPlatform, platform)
    }

    fun setLoginVerificationCode(context: Context, code: String) {
        SharedPreferenceManager(context).setString(loginVerificationCode, code)
    }

    fun setTopicNotification(context: Context, topic: String) {
        SharedPreferenceManager(context).setString("Notification_All", topic)
    }

    fun setNotificationAddress(context: Context, addr: String) {
        SharedPreferenceManager(context).setString(NOTIFICATION_ADDRESS, addr)
    }

    fun setLastRefreshTime(context: Context, l: Long) {
        SharedPreferenceManager(context).setLong(LAST_REFRESH_WIDGET_TIME, l)
    }

    fun setInitLocPermission(context: Context, s: String) {
        SharedPreferenceManager(context).setString(INITIALIZED_LOC_PERMISSION, s)
    }

    fun setInitNotiPermission(context: Context, s: String) {
        SharedPreferenceManager(context).setString(INITIALIZED_NOTI_PERMISSION, s)
    }

    fun setCurrentLocation(context: Context, loc: String) {
        SharedPreferenceManager(context).setString(CURRENT_GPS_ID, loc)
    }

}