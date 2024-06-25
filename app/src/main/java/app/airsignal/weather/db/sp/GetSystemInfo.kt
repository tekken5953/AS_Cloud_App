package app.airsignal.weather.db.sp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-06-12 오후 2:08
 **/
object GetSystemInfo {

    /** 현재 테마가 다크인가**/
    fun isThemeNight(context: Context): Boolean =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

    /** 현재 설정된 국가를 반환 **/
    fun getLocale(): Locale = when (GetAppInfo.getUserLocation()) {
        SpDao.LANG_KR -> Locale.KOREA
        SpDao.LANG_EN -> Locale.ENGLISH
        else -> Locale.getDefault()
    }

    /** 현재 앱 버전 반환 **/
    fun getApplicationVersionName(context: Context): String =
        kotlin.runCatching {
            val packageManager = context.packageManager
            @Suppress("DEPRECATION")
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        }.getOrElse { "" }

    /** 현재 앱 버전 반환 **/
    fun getApplicationVersionCode(context: Context): String =
        kotlin.runCatching {
            val packageManager = context.packageManager
            @Suppress("DEPRECATION")
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val appVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            appVersionCode.toString()
        }.getOrElse { "" }

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
    private fun getBottomSheetDialogDefaultHeight(context: Context, per: Int): Int =
        getWindowHeight(context) * per / 100

    fun setupRatio(context: Context, bottomSheetDialog: BottomSheetDialog, ratio: Int) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight(context,ratio)
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheet.background = ResourcesCompat.getDrawable(context.resources, R.drawable.loc_perm_bg, null)
    }
}