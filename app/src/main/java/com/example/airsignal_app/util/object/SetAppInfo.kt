package com.example.airsignal_app.util.`object`

import android.content.Context
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPhone
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.dao.IgnoredKeyFile.loginVerificationCode
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.IgnoredKeyFile.userFontScale
import com.example.airsignal_app.dao.IgnoredKeyFile.userId
import com.example.airsignal_app.dao.IgnoredKeyFile.userLocation
import com.example.airsignal_app.dao.IgnoredKeyFile.userProfile
import com.example.airsignal_app.dao.StaticDataObject.INITIALIZED_BACK_LOC_PERMISSION
import com.example.airsignal_app.dao.StaticDataObject.INITIALIZED_LOC_PERMISSION
import com.example.airsignal_app.dao.StaticDataObject.INITIALIZED_NOTI_PERMISSION
import com.example.airsignal_app.dao.StaticDataObject.LAST_REFRESH_WIDGET_TIME
import com.example.airsignal_app.dao.StaticDataObject.NOTIFICATION_ADDRESS
import com.example.airsignal_app.db.SharedPreferenceManager

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:28
 **/
object SetAppInfo {

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

    fun setNotificationAddress(context: Context, addr: String?) {
        SharedPreferenceManager(context).setString(NOTIFICATION_ADDRESS, addr!!)
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
}