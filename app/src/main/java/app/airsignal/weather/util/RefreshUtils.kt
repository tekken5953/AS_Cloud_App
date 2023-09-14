package app.airsignal.weather.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import kotlin.system.exitProcess

class RefreshUtils(private val context: Context) {

    /** 액티비티 갱신 **/
    fun refreshActivity() {
        (context as Activity).let {
            it.finish() //인텐트 종료
            it.overridePendingTransition(0, 0) //인텐트 효과 없애기
            val intent = it.intent //인텐트
            it.startActivity(intent) //액티비티 열기
            it.overridePendingTransition(0, 0) //인텐트 효과 없애기
        }
    }

    /** sec 초 이후에 액티비티 갱신 **/
    fun refreshActivityAfterSecond(sec: Int, pbLayout: ProgressBar?) {
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
        val packageName: String = context.packageName
        val packageManager: PackageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        exitProcess(0)
    }
}