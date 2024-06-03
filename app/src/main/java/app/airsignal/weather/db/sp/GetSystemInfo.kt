package app.airsignal.weather.db.sp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import androidx.core.content.pm.PackageInfoCompat
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:08
 **/
object GetSystemInfo {

    /** 현재 테마가 다크인가**/
    fun isThemeNight(context: Context): Boolean {
        val nightModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlag == Configuration.UI_MODE_NIGHT_YES
    }

    /** 현재 설정된 국가를 반환 **/
    fun getLocale(context: Context): Locale {
        return when (GetAppInfo.getUserLocation(context)) {
            SpDao.LANG_KR -> Locale.KOREA
            SpDao.LANG_EN -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
    }

    /** 현재 앱 버전 반환 **/
    fun getApplicationVersionName(context: Context): String {
        kotlin.runCatching {
            val packageManager = context.packageManager
            @Suppress("DEPRECATION")
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        }.exceptionOrNull()?.stackTraceToString()

        return ""
    }

    /** 현재 앱 버전 반환 **/
    fun getApplicationVersionCode(context: Context): String {
        kotlin.runCatching {
            val packageManager = context.packageManager
            @Suppress("DEPRECATION")
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val appVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            return appVersionCode.toString()
        }.exceptionOrNull()?.stackTraceToString()

        return ""
    }

    // 플레이 스토어 주소 반환
    fun getPlayStoreURL(context: Context): String = "market://details?id=${context.packageName}"

    /** 플레이 스토어로 이동 **/
    fun goToPlayStore(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getPlayStoreURL(activity))
        activity.startActivity(intent)
    }

    // 디바이스 높이 구하기
    private fun getWindowHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context as Activity?)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    // 다이얼로그 비율설정
    fun getBottomSheetDialogDefaultHeight(context: Context, per: Int): Int =
        getWindowHeight(context) * per / 100

    fun isAppInstalled(activity: Activity, packageName: String): Boolean {
        return try {
            activity.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}