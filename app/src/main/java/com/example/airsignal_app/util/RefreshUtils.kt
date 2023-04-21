package com.example.airsignal_app.util

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import kotlin.system.exitProcess

class RefreshUtils(private val activity: Activity) {

    /** 액티비티 갱신 **/
    fun refreshActivity() {
        activity.let {
            it.finish() //인텐트 종료
            it.overridePendingTransition(0, 0) //인텐트 효과 없애기
            val intent = it.intent //인텐트
            it.startActivity(intent) //액티비티 열기
            it.overridePendingTransition(0, 0) //인텐트 효과 없애기
        }
    }

    /** sec 초 이후에 액티비티 갱신 **/
    fun refreshActivityAfterSecond(sec: Int, pbLayout: LinearLayout?) {
        pbLayout?.let {
            it.visibility = View.VISIBLE
        }
        Handler(Looper.getMainLooper()).postDelayed ({
           this.refreshActivity()
            pbLayout?.let {
                it.visibility = View.GONE
            }
        }, (sec * 1000).toLong())
    }

    /** 어플리케이션 재시작 **/
    fun refreshApplication() {
        val packageName: String = activity.packageName
        val packageManager: PackageManager = activity.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        activity.startActivity(mainIntent)
        exitProcess(0)
    }
}