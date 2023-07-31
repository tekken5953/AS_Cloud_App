package com.example.airsignal_app.login

import android.app.Activity
import android.util.Log
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_GOOGLE
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_KAKAO
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_NAVER
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_PHONE
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLoginHistory
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.util.EnterPageUtil
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
            LOGIN_GOOGLE -> {
                // 구글 자동 로그인
                val googleLogin = GoogleLogin(activity)
                if (!googleLogin.isValidToken()) {
                    googleLogin.checkSilenceLogin()
                    writeLoginHistory(isLogin = true,
                        platform = LOGIN_GOOGLE,
                        email = email,
                        isAuto = true, isSuccess = true)
                }
            }
            LOGIN_KAKAO -> {
                // 카카오 자동 로그인
                val kakaoLogin = KakaoLogin(activity)
                if (!kakaoLogin.getAccessToken()) {
                    writeLoginHistory(isLogin = true, platform = LOGIN_KAKAO, email = email,
                        isAuto = true, isSuccess = true)
                    kakaoLogin.isValidToken(pbLayout)
                }
            }
            LOGIN_NAVER -> {
                // 네이버 자동 로그인
                val naverLogin = NaverLogin(activity)
                if (naverLogin.getAccessToken() == null) {
                    writeLoginHistory(isLogin = true, platform = LOGIN_NAVER, email = email,
                        isAuto = true, isSuccess = true)

                    naverLogin.silentLogin()
                }
            }
            LOGIN_PHONE -> {
                EnterPageUtil(activity).toMain(LOGIN_PHONE)
            }
        }
    }
}