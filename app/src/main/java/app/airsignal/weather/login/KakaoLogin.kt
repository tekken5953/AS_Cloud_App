package app.airsignal.weather.login

import android.app.Activity
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.db.sp.SharedPreferenceManager
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.utils.view.RefreshUtils
import app.airsignal.weather.utils.plain.ToastUtils
import com.airbnb.lottie.LottieAnimationView
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author : Lee Jae Young
 * @since : 2023-03-09 오전 11:29
 **/

class KakaoLogin(private val activity: Activity): KoinComponent {

    private val toast: ToastUtils by inject()

    init { KakaoSdk.init(activity, IgnoredKeyFile.KAKAO_NATIVE_APP_KEY) }

    private val sp: SharedPreferenceManager by inject()

    /** 카카오톡 설치 확인 후 로그인**/
    fun checkInstallKakaoTalk(btn: AppCompatButton?) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            btn?.alpha = 0.7f
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                // 로그인 실패 부분
                if (error != null) {
                    btn?.alpha = 1f
                    // 사용자가 취소
                    if ((error is ClientError) && (error.reason == ClientErrorCause.Cancelled))
                        return@loginWithKakaoTalk
                    // 다른 오류
                    else UserApiClient.instance.loginWithKakaoAccount(activity, callback = mCallback)
                }
                else {
                    UserApiClient.instance.me { _, _ -> }
                    // 로그인 성공 부분
                    token?.let {
                        loginSilenceKakao()
                        enterMainPage()
                    }
                }
            }
        }
        // 카카오 이메일 로그인
        else UserApiClient.instance.loginWithKakaoAccount(activity, callback = mCallback)
    }

    /** 카카오 이메일 로그인 콜백 **/
    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error == null) {
            token?.let {
                loginSilenceKakao()
                enterMainPage()
            }

            UserApiClient.instance.me { user, _ -> user?.kakaoAccount }
        }
    }

    fun getAccessToken() : Boolean = AuthApiClient.instance.hasToken()

//    /** 자동 로그인 **/
//    private fun isValidToken(): String? {
//        var token:String? = null
//        if (AuthApiClient.instance.hasToken()) {
//            UserApiClient.instance.accessTokenInfo { info, error ->
//                token = if (error != null) {
//                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
//                        logger.w(TAG_L,"만료된 토큰입니다")  // 만료된 토큰임 로그인 필요
//                        "valid"
//                    } else {
//                        logger.e(TAG_L,"기타 에러 발생 : $error") //기타 에러
//                        "error"
//                    }
//                } else info.toString() //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
//            }
//            return token
//        } else {
//            // 토큰이 없음 로그인 필요
//           return "has not token"
//        }
//    }

    /** 카카오 자동 로그인
     * @return OAuthToken? **/
//    private fun loginSilenceKakao(): OAuthToken? {
//        val token = TokenManagerProvider.instance.manager.getToken()
//        val isValid = isValidToken()
//        return if (isValid == "valid" || isValid == "error") {
//            Toast.makeText(activity, "다시 로그인해주세요", Toast.LENGTH_SHORT).show()
//            disconnectFromKakao(null)
//            null
//        } else if (isValid == "has not token") {
//            Toast.makeText(activity, "만료된 토큰입니다", Toast.LENGTH_SHORT).show()
//            disconnectFromKakao(null)
//            null
//        }
//        else {
//            token
//        }
//    }
    private fun loginSilenceKakao(): OAuthToken? = TokenManagerProvider.instance.manager.getToken()

    private fun enterMainPage() {
        CoroutineScope(Dispatchers.IO).launch {
            saveUserSettings()
            SetAppInfo.setUserLoginPlatform(StaticDataObject.LOGIN_KAKAO)
            withContext(Dispatchers.Main) {
                delay(500)
                activity.finish()
            }
        }
    }

    private fun saveUserSettings() {
        UserApiClient.instance.me { user, _ ->
            user?.kakaoAccount?.let { account ->
                sp.setString(SpDao.LAST_LOGIN_PHONE, account.phoneNumber.toString())
                    .setString(SpDao.USER_ID, account.profile?.nickname.toString())
                    .setString(SpDao.USER_PROFILE, account.profile?.profileImageUrl.toString())
                    .setString(SpDao.USER_EMAIL, account.email.toString())
            }
        }
    }
//    private fun refreshToken() {
//        //토큰 갱신하기 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#refresh-token
//    }

    /** 카카오 로그아웃 + 기록 **/
    fun logout(pb: LottieAnimationView?) {
        kotlin.runCatching {
            UserApiClient.instance.logout { error ->
                if (error != null) toast.showMessage("로그아웃에 실패했습니다",1)
                else {
                    activity.runOnUiThread {
                        RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = pb)
                    }
                }
            }
        }.exceptionOrNull()?.stackTraceToString()
    }

    /** 클라이언트와 완전히 연결 끊기 **/
    fun disconnectFromKakao(pb: LottieAnimationView?) {
        UserApiClient.instance.unlink {
            pb?.let { RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = it) }
        }
    }
}