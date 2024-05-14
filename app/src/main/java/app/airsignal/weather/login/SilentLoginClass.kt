package app.airsignal.weather.login

import android.app.Activity
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.db.sp.GetAppInfo

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 5:21
 **/
class SilentLoginClass {
    /** 플랫폼 별 자동로그인 **/
    fun login(activity: Activity) {
        val email = GetAppInfo.getUserEmail(activity)

        when (GetAppInfo.getUserLoginPlatform(activity)) {
            RDBLogcat.LOGIN_GOOGLE -> {
                // 구글 자동 로그인
                val googleLogin = GoogleLogin(activity)
                if (!googleLogin.isValidToken()) {
                    googleLogin.checkSilenceLogin()
                    RDBLogcat.writeLoginHistory(isLogin = true,
                        platform = RDBLogcat.LOGIN_GOOGLE,
                        email = email,
                        isAuto = true, isSuccess = true)
                }
            }
            RDBLogcat.LOGIN_KAKAO -> {
                // 카카오 자동 로그인
                val kakaoLogin = KakaoLogin(activity)
                if (!kakaoLogin.getAccessToken()) {
                    RDBLogcat.writeLoginHistory(isLogin = true, platform = RDBLogcat.LOGIN_KAKAO, email = email,
                        isAuto = true, isSuccess = true)
                }
            }
            RDBLogcat.LOGIN_NAVER -> {
                // 네이버 자동 로그인
                val naverLogin = NaverLogin(activity).init()
                if (naverLogin.getAccessToken() == null) {
                    naverLogin.silentLogin()
                    RDBLogcat.writeLoginHistory(isLogin = true, platform = RDBLogcat.LOGIN_NAVER, email = email,
                        isAuto = true, isSuccess = true)
                }
            }
        }
    }
}