package app.core_databse.db.sp

import android.content.Context
import app.core_databse.db.SharedPreferenceManager
import app.core_databse.db.sp.SpDao.INITIALIZED_LOC_PERMISSION
import app.core_databse.db.sp.SpDao.INITIALIZED_NOTI_PERMISSION
import app.core_databse.db.sp.SpDao.IN_APP_MSG_NAME
import app.core_databse.db.sp.SpDao.IS_INIT_BACK_LOC_PERMISSION
import app.core_databse.db.sp.SpDao.IS_PERMED_BACK_LOG
import app.core_databse.db.sp.SpDao.LANDING_NOTIFICATION
import app.core_databse.db.sp.SpDao.LAST_REFRESH22
import app.core_databse.db.sp.SpDao.LAST_REFRESH42
import app.core_databse.db.sp.SpDao.NOTIFICATION_ADDRESS
import app.core_databse.db.sp.SpDao.NOTIFICATION_TOPIC_DAILY
import app.core_databse.db.sp.SpDao.lastAddress
import app.core_databse.db.sp.SpDao.lastLoginPhone
import app.core_databse.db.sp.SpDao.lastLoginPlatform
import app.core_databse.db.sp.SpDao.userEmail
import app.core_databse.db.sp.SpDao.userFontScale
import app.core_databse.db.sp.SpDao.userId
import app.core_databse.db.sp.SpDao.userLocation
import app.core_databse.db.sp.SpDao.userProfile

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:28
 **/
object SetAppInfo {

    fun setUserFontScale(context: Context, type: String) {
        SharedPreferenceManager(context).setString(userFontScale, type)
    }

    fun setUserLocation(context: Context, location: String) {
        SharedPreferenceManager(context)
            .setString(userLocation, location)
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
        SharedPreferenceManager(context)
            .setString(userProfile, profile)
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
        SharedPreferenceManager(context)
            .setString(lastLoginPlatform, platform)
    }

    fun setTopicNotification(context: Context, topic: String) {
        SharedPreferenceManager(context)
            .setString(NOTIFICATION_TOPIC_DAILY, topic)
    }

    fun setNotificationAddress(context: Context, addr: String) {
        SharedPreferenceManager(context)
            .setString(NOTIFICATION_ADDRESS, addr)
    }

    fun setInitLocPermission(context: Context, s: String) {
        SharedPreferenceManager(context)
            .setString(INITIALIZED_LOC_PERMISSION, s)
    }

    fun setInitNotiPermission(context: Context, s: String) {
        SharedPreferenceManager(context)
            .setString(INITIALIZED_NOTI_PERMISSION, s)
    }

    fun setInitBackLocPermission(context: Context,b: Boolean) {
        SharedPreferenceManager(context)
            .setBoolean(IS_INIT_BACK_LOC_PERMISSION, b)
    }

    fun setPermedBackLog(context: Context,b: Boolean) {
        SharedPreferenceManager(context)
            .setBoolean(IS_PERMED_BACK_LOG, b)
    }

    fun setLastRefreshTime42(context: Context, time: Long) {
        SharedPreferenceManager(context).setLong(LAST_REFRESH42,time)
    }

    fun setLastRefreshTime22(context: Context, time: Long) {
        SharedPreferenceManager(context).setLong(LAST_REFRESH22,time)
    }

    fun setLandingNotification(context: Context, b: Boolean) {
        SharedPreferenceManager(context).setBoolean(LANDING_NOTIFICATION,b)
    }

    fun setInAppMsgEnabled(context: Context, name: String, enabled: Boolean) {
        SharedPreferenceManager(context).setBoolean("$IN_APP_MSG_NAME$name", enabled)
    }
}