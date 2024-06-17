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
    suspend fun login(activity: Activity) {
        when (GetAppInfo.getUserLoginPlatform(activity)) {
            RDBLogcat.LOGIN_GOOGLE -> {
                // 구글 자동 로그인
                val googleLogin = GoogleLogin(activity)
                if (!googleLogin.isValidToken()) googleLogin.checkSilenceLogin()
            }
            RDBLogcat.LOGIN_KAKAO -> {
                KakaoLogin(activity).checkInstallKakaoTalk(null) // 카카오 자동 로그인
            }

            RDBLogcat.LOGIN_NAVER -> {
                // 네이버 자동 로그인
                val naverLogin = NaverLogin(activity).init()
                if (naverLogin.getAccessToken() != null) naverLogin.silentLogin()
                else naverLogin.refreshToken()
            }
        }
    }
}