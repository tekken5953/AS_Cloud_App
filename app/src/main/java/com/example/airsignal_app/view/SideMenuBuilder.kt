package com.example.airsignal_app.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.ConvertDataType
import com.example.airsignal_app.util.RefreshUtils
import java.util.concurrent.CompletableFuture
import javax.inject.Singleton

/**
 * @author : Lee Jae Young
 * @since : 2023-05-11 오전 11:55
 **/
class SideMenuBuilder(private val activity: Activity) {
    private var builder: AlertDialog.Builder =
        AlertDialog.Builder(activity, R.style.DialogAnimationMenu)
    private lateinit var alertDialog: AlertDialog

    init {
        setFontScale()
    }

    /** 다이얼로그 뒤로가기 버튼 리스너 등록 **/
    fun setBackPressed(imageView: View): SideMenuBuilder {
        imageView.setOnClickListener {
            dismiss()
        }
        return this
    }

    /** 다이얼로그 뒤로가기 버튼 후 액티비티 갱신 **/
    fun setBackPressRefresh(imageView: ImageView): SideMenuBuilder {
        imageView.setOnClickListener {
            dismiss()
            RefreshUtils(activity).refreshActivity()
        }
        return this
    }

    /** 다이얼로그 뷰 소멸 **/
    fun dismiss() {
        if (alertDialog.isShowing) {
            alertDialog.dismiss()
        }
    }

    /** 다이얼로그 뷰 갱신 **/
    fun show(v: View, cancelable: Boolean) {
        v.let {
            if (v.parent == null) {
                builder.setView(v).setCancelable(cancelable)
                alertDialog = builder.create()
                attributeDialog()
                alertDialog.show()
            } else {
                (v.parent as ViewGroup).removeView(v)
                builder.setView(v).setCancelable(cancelable)
                alertDialog = builder.create()
                attributeDialog()
                alertDialog.show()
            }
        }
    }

    fun setUserData(profile: ImageView, Id: TextView): SideMenuBuilder {
        val sp = SharedPreferenceManager(activity)
        Glide.with(activity)
            .load(Uri.parse(SharedPreferenceManager(activity).getString(IgnoredKeyFile.userProfile)))
            .into(profile)

        if (sp.getString(IgnoredKeyFile.userEmail) != "") {
            Id.text =
                sp.getString(IgnoredKeyFile.userEmail)
        } else Id.text =
            activity.getString(R.string.please_login)

        return this
    }

    fun setFontScale(): SideMenuBuilder {
        when (SharedPreferenceManager(builder.context).getString("scale")) {
            "small" -> {
                ConvertDataType.setTextSizeSmall(builder.context)
            }
            "big" -> {
                ConvertDataType.setTextSizeLarge(builder.context)
            }
            else -> {
                ConvertDataType.setTextSizeDefault(builder.context)
            }
        }
        return this
    }

    private fun attributeDialog() {
        val params: WindowManager.LayoutParams = alertDialog.window!!.attributes

        params.width = getBottomSheetDialogDefaultWidth(75)
//        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.gravity = Gravity.START

        // 열기&닫기 시 애니메이션 설정
        params.windowAnimations = R.style.DialogAnimationMenuAnim
        alertDialog.window!!.attributes = params
    }

    // 바텀 다이얼로그 비율설정
    private fun getBottomSheetDialogDefaultWidth(per: Int): Int {
        return getWindowWidth() * per / 100
    }

    private fun getWindowWidth(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") activity.windowManager.defaultDisplay.getMetrics(
            displayMetrics
        )
        return displayMetrics.widthPixels
    }
}