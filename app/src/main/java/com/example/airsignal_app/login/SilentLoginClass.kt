package com.example.airsignal_app.login

import android.app.Activity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.airsignal_app.firebase.db.RDBLogcat.sendLogInWithEmail
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 5:21
 **/
class SilentLoginClass {

    /** 플랫폼 별 자동로그인 **/
    fun login(activity: Activity, pbLayout: MotionLayout) {
        val email = getUserEmail(activity)

        when (getUserLoginPlatform(activity)) {
            "google" -> {
                // 구글 자동 로그인
                val googleLogin = GoogleLogin(activity)
                if (!googleLogin.isValidToken()) {
                    googleLogin.checkSilenceLogin()
                }
            }
            "kakao" -> {
                // 카카오 자동 로그인
                val kakaoLogin = KakaoLogin(activity)
                if (!kakaoLogin.getAccessToken()) {
                    sendLogInWithEmail(
                        "로그인 성공",
                        email,
                        "카카오",
                        "자동"
                    )
                    kakaoLogin.isValidToken(pbLayout)
                }
            }
            "naver" -> {
                // 네이버 자동 로그인
                val naverLogin = NaverLogin(activity)
                if (naverLogin.getAccessToken() == null) {
                    sendLogInWithEmail(
                        "로그인 성공",
                        email,
                        "네이버",
                        "자동"
                    )
                    naverLogin.silentLogin()
                }
            }
//            "email" -> {
//                EnterPage(activity).toMain("email")
//            }
        }
    }
}