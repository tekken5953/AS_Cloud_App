package app.airsignal.weather.login

import android.app.Activity
import app.airsignal.weather.dao.RDBLogcat.LOGIN_GOOGLE
import app.airsignal.weather.dao.RDBLogcat.LOGIN_KAKAO
import app.airsignal.weather.dao.RDBLogcat.LOGIN_NAVER
import app.airsignal.weather.dao.RDBLogcat.writeLoginHistory
import app.core_databse.db.sp.GetAppInfo.getUserEmail
import app.core_databse.db.sp.GetAppInfo.getUserLoginPlatform

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 5:21
 **/
class SilentLoginClass {
    /** 플랫폼 별 자동로그인 **/
    fun login(activity: Activity) {
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
                }
            }
            LOGIN_NAVER -> {
                // 네이버 자동 로그인
                val naverLogin = NaverLogin(activity)
                if (naverLogin.getAccessToken() == null) {
                    naverLogin.silentLogin()
                    writeLoginHistory(isLogin = true, platform = LOGIN_NAVER, email = email,
                        isAuto = true, isSuccess = true)
                }
            }
        }
    }
}