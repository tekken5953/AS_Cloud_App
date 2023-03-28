package com.example.airsignal_app.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.example.airsignal_app.R

/**
 * @author : Lee Jae Young
 * @since : 2023-03-28 오전 11:15
 **/
class ShowDialogClass(context: Context) {
    private val mContext = context

    /** 다이얼로그 뷰 갱신 **/
    fun show(v: View, cancelable: Boolean) : androidx.appcompat.app.AlertDialog.Builder{
        v.let {
            val builder = androidx.appcompat.app.AlertDialog.Builder(mContext, R.style.AlertDialog)
            if (v.parent == null)
                builder.setView(v).show().setCancelable(cancelable)
            else {
                (v.parent as ViewGroup).removeView(v)
                builder.setView(v).show().setCancelable(cancelable)
            }
            return builder
        }
    }
}