package app.airsignal.weather.login

import android.app.Activity
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.db.sp.GetAppInfo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 5:21
 **/
class SilentLoginClass(private val activity: Activity) : KoinComponent{
    private val googleLogin: GoogleLogin by inject { parametersOf(activity) }  // 구글 로그인
    private val kakaoLogin: KakaoLogin by inject { parametersOf(activity) }   // 카카오 로그인
    private val naverLogin: NaverLogin by inject { parametersOf(activity) }  // 네이버 로그인
    /** 플랫폼 별 자동로그인 **/
    suspend fun login() {
        when (GetAppInfo.getUserLoginPlatform(activity)) {
            RDBLogcat.LOGIN_GOOGLE -> {
                // 구글 자동 로그인
                if (!googleLogin.isValidToken()) googleLogin.checkSilenceLogin()
            }
            RDBLogcat.LOGIN_KAKAO -> {
                kakaoLogin.checkInstallKakaoTalk(null) // 카카오 자동 로그인
            }

            RDBLogcat.LOGIN_NAVER -> {
                // 네이버 자동 로그인
                if (naverLogin.getAccessToken() != null) naverLogin.silentLogin()
                else naverLogin.refreshToken()
            }
        }
    }
}