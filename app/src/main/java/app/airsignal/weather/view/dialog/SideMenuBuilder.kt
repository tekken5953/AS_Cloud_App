package app.airsignal.weather.view.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.SetSystemInfo
import app.airsignal.weather.db.sp.SpDao
import com.bumptech.glide.Glide

/**
 * @author : Lee Jae Young
 * @since : 2023-05-11 오전 11:55
 **/
class SideMenuBuilder(private val context: Context) {
    private var builder: AlertDialog.Builder =
        AlertDialog.Builder(context, R.style.DialogAnimationMenu)
    private lateinit var alertDialog: AlertDialog

    init { setFontScale() }

    /** 다이얼로그 뒤로가기 버튼 리스너 등록 **/
    fun setBackPressed(imageView: View): SideMenuBuilder {
        imageView.setOnClickListener { dismiss() }
        return this
    }

    /** 다이얼로그 뷰 소멸 **/
    fun dismiss() { if (alertDialog.isShowing) alertDialog.dismiss() }

    /** 다이얼로그 뷰 갱신 **/
    fun show(v: View, cancelable: Boolean): AlertDialog {
        v.let {
            if (v.parent != null) (v.parent as ViewGroup).removeView(v)
            builder.setView(v).setCancelable(cancelable)
            alertDialog = builder.create()
            attributeDialog()
            alertDialog.show()
        }
        return alertDialog
    }

    // 로그인 정보 반환
    fun setUserData(profile: ImageView, id: TextView): SideMenuBuilder {
        Glide.with(context)
            .load(Uri.parse(GetAppInfo.getUserProfileImage()))
            .error(ResourcesCompat.getDrawable(context.resources,R.mipmap.ic_launcher_round,null))
            .into(profile)

        val email = GetAppInfo.getUserEmail()
        id.text = if (email != "") email else context.getString(R.string.please_login)

        return this
    }

    // 폰트 크기 반환
    private fun setFontScale(): SideMenuBuilder {
        when (GetAppInfo.getUserFontScale()) {
            SpDao.TEXT_SCALE_SMALL -> SetSystemInfo.setTextSizeSmall(builder.context)
            SpDao.TEXT_SCALE_BIG -> SetSystemInfo.setTextSizeLarge(builder.context)
            else -> SetSystemInfo.setTextSizeDefault(builder.context)
        }
        return this
    }

    // 바텀 다이얼로그 가로 비율 설정
    private fun attributeDialog() {
        val params: WindowManager.LayoutParams? = alertDialog.window?.attributes
        params?.let {
            it.width = getBottomSheetDialogDefaultWidth(75)
            it.gravity = Gravity.START
            it.windowAnimations = R.style.DialogAnimationMenuAnim // 열기 & 닫기 시 애니메이션 설정
        }

        alertDialog.window?.attributes = params
    }

    // 바텀 다이얼로그 세로 비율 설정
    private fun getBottomSheetDialogDefaultWidth(per: Int): Int = getWindowWidth() * per / 100

    // 디바이스 넓이 구하기
    private fun getWindowWidth(): Int {
        val displayMetrics = DisplayMetrics()   // 디바이스의 width 를 구한다
        @Suppress("DEPRECATION")
        if (context is Activity) context.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }
}