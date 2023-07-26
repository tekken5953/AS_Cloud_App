package com.example.airsignal_app.view

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.airsignal_app.R
import com.example.airsignal_app.util.VibrateUtil
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserFontScale
import com.example.airsignal_app.util.`object`.SetSystemInfo

/**
 * @author : Lee Jae Young
 * @since : 2023-03-28 오전 11:15
 **/

class ShowDialogClass(private val activity: Activity) {
    private var builder: androidx.appcompat.app.AlertDialog.Builder =
        androidx.appcompat.app.AlertDialog.Builder(activity, R.style.AlertDialog)
    private lateinit var alertDialog: androidx.appcompat.app.AlertDialog

    init {
        when(getUserFontScale(activity)) {
            "small" -> {
                SetSystemInfo.setTextSizeSmall(activity)
            }
            "big" -> {
                SetSystemInfo.setTextSizeLarge(activity)
            }
            else -> {
                SetSystemInfo.setTextSizeDefault(activity)
            }
        }
    }

    /** 다이얼로그 뒤로가기 버튼 리스너 등록 **/
    fun setBackPressed(imageView: View): ShowDialogClass {
        imageView.setOnClickListener {
//            makeVib()
            dismiss()
        }
        return this
    }

    /** 다이얼로그 뒤로가기 버튼 후 액티비티 갱신 **/
    fun setBackPressRefresh(imageView: ImageView): ShowDialogClass {
        imageView.setOnClickListener {
//            makeVib()
            dismiss()
            RefreshUtils(activity).refreshActivity()
        }
        return this
    }

    // 진동 발생
    private fun makeVib() {
        VibrateUtil(activity).make(20)
    }

    /** 다이얼로그 뷰 소멸 **/
    fun dismiss() {
        if (alertDialog.isShowing)
            alertDialog.dismiss()
    }

    /** 다이얼로그 뷰 갱신 **/
    fun show(v: View, cancelable: Boolean) {
        v.let {
            if (v.parent == null) {
                builder.setView(v).setCancelable(cancelable)
                alertDialog = builder.create()
                alertDialog.show()
            } else {
                (v.parent as ViewGroup).removeView(v)
                builder.setView(v).setCancelable(cancelable)
                alertDialog = builder.create()
                alertDialog.show()
            }
        }
    }
}