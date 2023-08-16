package app.airsignal.weather.util

import android.app.Activity
import android.content.Intent
import app.airsignal.weather.util.`object`.SetAppInfo.setUserLoginPlatform
import app.airsignal.weather.view.activity.LoginActivity
import app.airsignal.weather.view.activity.MainActivity
import app.airsignal.weather.view.activity.PermissionActivity
import kotlin.system.exitProcess

/**
 *
 * @author : Lee Jae Young
 * @since : 2023-03-10 오전 10:55
 *
 * 페이지의 이동을 모아놓은 클래스
 **/

class EnterPageUtil(private val activity: Activity) {

    /**
     * 메인 페이지로 이동한다
     *
     * @param sort 간편로그인의 분류 ex) "카카오"
     */
    fun toMain(sort: String?) {
        sort?.let {
            setUserLoginPlatform(activity, it)
        }
        val intent = Intent(activity, MainActivity::class.java)
        System.runFinalization() // 현재 구동중인 쓰레드가 다 종료되면 종료
        activity.startActivity(intent)
        activity.finish()
    }

    /**로그인 페이지로 이동한다*/
    fun toLogin() {
        val intent = Intent(activity, LoginActivity::class.java)
        activity.startActivity(intent)
        activity.overridePendingTransition(0,0)
    }

    /** 액티비티를 완전히 종료한다 **/
    fun fullyExit() {
        activity.run {
            finishAffinity()  // 해당 어플리케이션의 루트 액티비티를 종료
            System.runFinalization() // 현재 구동중인 쓰레드가 다 종료되면 종료
            exitProcess(0) // 현재의 액티비티를 종료
        }
    }

    /** 권한 요청 페이지로 이동 **/
    fun toPermission() {
        val intent = Intent(activity, PermissionActivity::class.java)
        activity.startActivity(intent)
        activity.overridePendingTransition(0,0)
        activity.finish()
    }
}