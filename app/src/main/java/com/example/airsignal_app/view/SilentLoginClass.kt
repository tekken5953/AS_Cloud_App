package com.example.airsignal_app.view

import android.app.Activity
import android.widget.ProgressBar
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 5:21
 **/
class SilentLoginClass {
    // 플랫폼 별 자동로그인
    fun login(activity: Activity, pb: ProgressBar) {
        when (SharedPreferenceManager(activity).getString(IgnoredKeyFile.lastLoginPlatform)) {
            "google" -> {
                // 구글 자동 로그인
                val googleLogin = GoogleLogin(activity)
                if (!googleLogin.isValidToken())
                    googleLogin.checkSilenceLogin()
            }
            "kakao" -> {
                // 카카오 자동 로그인
                val kakaoLogin = KakaoLogin(activity).initialize()
                if (!kakaoLogin.getAccessToken())
                    kakaoLogin.isValidToken(pb)
            }
            "naver" -> {
                // 네이버 자동 로그인
                val naverLogin = NaverLogin(activity).initialize()
                if (naverLogin.getAccessToken() == null)
                    naverLogin.silentLogin()
            }

            "email" -> {
                //TODO 이메일 자동 로그인
            }
        }
    }
}