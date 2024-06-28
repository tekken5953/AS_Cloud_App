package app.airsignal.weather.view.custom

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.SetSystemInfo
import app.airsignal.weather.db.sp.SpDao.TEXT_SCALE_BIG
import app.airsignal.weather.db.sp.SpDao.TEXT_SCALE_SMALL
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-03-28 오전 11:15
 **/

class ShowDialogClass(activity: Activity, isEye: Boolean) {
    private var builder =
        androidx.appcompat.app.AlertDialog.Builder(activity, if (!isEye) R.style.AlertDialog else R.style.FullDialog)
    private lateinit var alertDialog: androidx.appcompat.app.AlertDialog

    enum class DialogTransition {
        END_TO_START, START_TO_END, BOTTOM_TO_TOP, TOP_TO_BOTTOM
    }

    init {
        // 폰트 크기 설정
        when(GetAppInfo.getUserFontScale()) {
            TEXT_SCALE_SMALL -> SetSystemInfo.setTextSizeSmall(activity)
            TEXT_SCALE_BIG -> SetSystemInfo.setTextSizeLarge(activity)
            else -> SetSystemInfo.setTextSizeDefault(activity)
        }

        when (GetAppInfo.getUserLocation()) {
            StaticDataObject.LANG_KR -> SetSystemInfo.updateConfiguration(activity, Locale.KOREA)
            StaticDataObject.LANG_EN -> SetSystemInfo.updateConfiguration(activity, Locale.ENGLISH)
            else -> SetSystemInfo.updateConfiguration(activity, Locale.getDefault())
        }
    }

    /** 다이얼로그 뒤로가기 버튼 리스너 등록 **/
    fun setBackPressed(view: View): ShowDialogClass {
        view.setOnClickListener { dismiss() }
        return this
    }

    /** 다이얼로그 뷰 소멸 **/
    fun dismiss() { if (alertDialog.isShowing) alertDialog.dismiss() }

    /** 다이얼로그 뷰 갱신 **/
    fun show(v: View, cancelable: Boolean, transition: DialogTransition?) {
        v.let {
            if(v.parent != null) (v.parent as ViewGroup).removeView(v)
            builder.setView(v).setCancelable(cancelable)
            alertDialog = builder.create()
            transition?.let {
                alertDialog.window?.attributes?.windowAnimations = when(it) {
                    DialogTransition.END_TO_START -> R.style.AlertDialogEndToStartAnimation
                    DialogTransition.START_TO_END -> R.style.AlertDialogStartToEndAnimation
                    DialogTransition.TOP_TO_BOTTOM -> R.style.AlertDialogTopToBottomAnimation
                    DialogTransition.BOTTOM_TO_TOP -> R.style.AlertDialogBottomToTopAnimation
                }
            }

            alertDialog.show()
        }
    }
}