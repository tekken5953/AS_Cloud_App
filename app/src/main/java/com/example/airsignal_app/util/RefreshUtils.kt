package com.example.airsignal_app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlin.system.exitProcess

class RefreshUtils {
    // 액티비티 갱신
    fun refreshActivity(activity: Activity) {
        activity.let {
            it.finish() //인텐트 종료
            it.overridePendingTransition(0, 0) //인텐트 효과 없애기
            val intent = it.intent //인텐트
            it.startActivity(intent) //액티비티 열기
            it.overridePendingTransition(0, 0) //인텐트 효과 없애기
        }
    }

    fun refreshApplication(context: Context) {
        val packageManager: PackageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        exitProcess(0)
    }
//
//    fun logout(context: Activity) {
//        val intent = Intent(context, LoginActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
//        SharedPreferenceManager(context).clear() // 저장된 환경 초기화
//        context.startActivity(intent)
//        context.finish()
//    }
}