package com.example.airsignal_app.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import com.example.airsignal_app.R
import kotlin.system.exitProcess

class RefreshUtils(mContext: Context) {
    private val context = mContext
    /** 액티비티 갱신 **/
    fun refreshActivity(activity: Activity) {
        activity.let {
            it.finish() //인텐트 종료
            it.overridePendingTransition(0, 0) //인텐트 효과 없애기
            val intent = it.intent //인텐트
            it.startActivity(intent) //액티비티 열기
            it.overridePendingTransition(0, 0) //인텐트 효과 없애기
        }
    }

    /** 어플리케이션 재시작 **/
    fun refreshApplication() {
        val packageManager: PackageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        exitProcess(0)
    }

    /** 다이얼로그 뷰 갱신 **/
    fun showDialog(v: View, cancelable: Boolean) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialog)
        v.let {
            if (v.parent == null)
                builder.setView(v).show().setCancelable(cancelable)
            else {
                (v.parent as ViewGroup).removeView(v)
                builder.setView(v).show().setCancelable(cancelable)
            }
        }
    }
}