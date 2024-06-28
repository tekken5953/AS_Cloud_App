package app.airsignal.weather.login

import android.app.Activity
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.db.sp.SharedPreferenceManager
import app.airsignal.weather.db.sp.SpDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 5:21
 **/
class SilentLoginClass(private val activity: Activity) : KoinComponent{
    private val googleLogin: GoogleLogin by inject(named("googleLogin")) { parametersOf(activity) }  // 구글 로그인
    private val kakaoLogin: KakaoLogin by inject(named("kakaoLogin")) { parametersOf(activity) }   // 카카오 로그인
    private val naverLogin: NaverLogin by inject(named("naverLogin")) { parametersOf(activity) }  // 네이버 로그인
    /** 플랫폼 별 자동로그인 **/
    suspend fun login() {
        naverLogin.init()

        when (GetAppInfo.getUserLoginPlatform()) {
            StaticDataObject.LOGIN_GOOGLE -> if (!googleLogin.isValidToken()) googleLogin.checkSilenceLogin() // 구글 자동 로그인
            StaticDataObject.LOGIN_KAKAO -> kakaoLogin.checkInstallKakaoTalk(null) // 카카오 자동 로그인
            StaticDataObject.LOGIN_NAVER -> {
                // 네이버 자동 로그인
                if (naverLogin.getAccessToken() != null) naverLogin.silentLogin()
                else SharedPreferenceManager(activity).removeKey(SpDao.LAST_LOGIN_PLATFORM)
            }
        }
    }
}