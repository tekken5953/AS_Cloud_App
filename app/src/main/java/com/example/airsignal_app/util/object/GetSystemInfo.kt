package com.example.airsignal_app.util.`object`

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.pm.PackageInfoCompat
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLocation
import com.orhanobut.logger.Logger
import timber.log.Timber
import java.util.*


/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:08
 **/
object GetSystemInfo {

    /** 현재 테마가 다크인가**/
    fun isThemeNight(context: Context): Boolean {
        val nightModeFlag =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlag == Configuration.UI_MODE_NIGHT_YES
    }

    /** 현재 설정된 국가를 반환 **/
    fun getLocale(context: Context): Locale {
        return when (getUserLocation(context)) {
            "korean" -> {
                Locale.KOREAN
            }
            "english" -> {
                Locale.ENGLISH
            }
            else -> {
                Locale.getDefault()
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun androidID(context: Context): String {
        val id = Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        Timber.tag("buildUserInfo").i(id)
        return id
    }

    @Suppress("DEPRECATION")
    fun getDeviceWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Logger.t(TAG_D).i("Display Width : ${displayMetrics.widthPixels}")
        return displayMetrics.widthPixels
    }


    fun getApplicationVersion(context: Context): String {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val appVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            val appVersionName = packageInfo.versionName
           return "${appVersionName}.${appVersionCode}"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return ""
    }

    fun getPlayStoreURL(context: Context): String {
        return "market://details?id=${context.packageName}"
    }

    fun goToPlayStore(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getPlayStoreURL(activity))
        activity.startActivity(intent)
    }
}