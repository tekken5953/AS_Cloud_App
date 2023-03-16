package com.example.airsignal_app.util

import android.app.Activity
import android.content.Intent
import com.example.airsignal_app.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.MainActivity
import com.example.airsignal_app.TestPageActivity
import com.example.airsignal_app.SignInActivity

/**
 *
 * @author : Lee Jae Young
 * @since : 2023-03-10 오전 10:55
 *
 * 페이지의 이동을 모아놓은 클래스
 **/

class EnterPage(mActivity: Activity) {
    private val activity = mActivity

    /**
     * 메인 페이지로 이동한다
     *
     * @param sort 간편로그인의 분류 ex) "카카오"
     */
    fun toMain(sort: String) {
        SharedPreferenceManager(activity).setString(lastLoginPlatform, sort)
        val intent = Intent(activity, TestPageActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    /**로그인 페이지로 이동한다*/
    fun toLogin() {
        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}