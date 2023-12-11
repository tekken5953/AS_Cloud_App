package app.airsignal.weather.login

import android.app.Activity
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.dao.IgnoredKeyFile.KAKAO_NATIVE_APP_KEY
import app.airsignal.weather.dao.IgnoredKeyFile.lastLoginPhone
import app.airsignal.weather.dao.IgnoredKeyFile.userEmail
import app.airsignal.weather.dao.IgnoredKeyFile.userId
import app.airsignal.weather.dao.IgnoredKeyFile.userProfile
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.dao.RDBLogcat.LOGIN_KAKAO
import app.airsignal.weather.dao.RDBLogcat.LOGIN_KAKAO_EMAIL
import app.airsignal.weather.dao.RDBLogcat.writeLoginHistory
import app.airsignal.weather.dao.StaticDataObject.TAG_L
import app.airsignal.weather.koin.BaseApplication.Companion.logger
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.RefreshUtils
import app.core_databse.db.SharedPreferenceManager
import app.core_databse.db.sp.GetAppInfo.getUserEmail
import app.utils.ToastUtils
import com.airbnb.lottie.LottieAnimationView
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient

/**
 * @author : Lee Jae Young
 * @since : 2023-03-09 오전 11:29
 **/

class KakaoLogin(private val activity: Activity) {

    init {
        KakaoSdk.init(activity, KAKAO_NATIVE_APP_KEY)
    }

    /** 카카오톡 설치 확인 후 로그인**/
    fun checkInstallKakaoTalk(btn: AppCompatButton) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            btn.alpha = 0.7f
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                // 로그인 실패 부분
                if (error != null) {
                    btn.alpha = 1f
                    // 사용자가 취소
                    if ((error is ClientError) && (error.reason == ClientErrorCause.Cancelled)) {
                        logger.d(TAG_L,"카카오 로그인 취소")
                        return@loginWithKakaoTalk
                    }
                    // 다른 오류
                    else {
                        logger.d(TAG_L,"카카오 로그인 기타 오류 : ${error.localizedMessage}")
                        UserApiClient.instance.loginWithKakaoAccount(
                            activity,
                            callback = mCallback
                        )
                    }
                }
                else {
                    // 로그인 성공 부분
                    token?.let {
                        loginSilenceKakao()
                        enterMainPage()
                    }
                    UserApiClient.instance.me { user, _ ->
                        user?.kakaoAccount?.let { account ->
                            writeLoginHistory(
                                isLogin = true, platform = LOGIN_KAKAO, email = account.email!!,
                                isAuto = false, isSuccess = true
                            )
                            RDBLogcat.writeLoginPref(
                                activity,
                                platform = LOGIN_KAKAO,
                                email = getUserEmail(activity),
                                phone = null,
                                name = account.name,
                                profile = account.profile?.profileImageUrl
                            )
                        }
                    }
                }
            }
        } else {
            // 카카오 이메일 로그인
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = mCallback)
        }
    }

    /** 카카오 이메일 로그인 콜백 **/
    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            writeLoginHistory(
                isLogin = false, platform = LOGIN_KAKAO_EMAIL, email = getUserEmail(activity),
                isAuto = false, isSuccess = false
            )
        } else {
            token?.let {
                loginSilenceKakao()
                enterMainPage()
            }

            UserApiClient.instance.me { user, _ ->
                user?.kakaoAccount?.let { account ->
                    writeLoginHistory(
                        isLogin = true, platform = LOGIN_KAKAO_EMAIL, email = getUserEmail(activity),
                        isAuto = false, isSuccess = true
                    )
                    RDBLogcat.writeLoginPref(
                        activity,
                        platform = LOGIN_KAKAO_EMAIL,
                        email = getUserEmail(activity),
                        phone = account.phoneNumber,
                        name = account.name,
                        profile = account.profile?.profileImageUrl
                    )
                }
            }
        }
    }

    fun getAccessToken() : Boolean {
        return AuthApiClient.instance.hasToken()
    }

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
//                } else info.toString()//토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
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
    private fun loginSilenceKakao(): OAuthToken? {
        return TokenManagerProvider.instance.manager.getToken()
    }

    private fun enterMainPage() {
        saveUserSettings()
        Thread.sleep(1000)
        EnterPageUtil(activity).toMain(LOGIN_KAKAO)
    }

    private fun saveUserSettings() {
        UserApiClient.instance.me { user, _ ->
            user?.kakaoAccount?.let { account ->
                SharedPreferenceManager(activity)
                    .setString(lastLoginPhone, account.phoneNumber.toString())
                    .setString(userId, account.profile!!.nickname.toString())
                    .setString(userProfile, account.profile!!.profileImageUrl.toString())
                    .setString(userEmail, account.email.toString())
            }
        }
    }
//    private fun refreshToken() {
//        //토큰 갱신하기 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#refresh-token
//    }

//    /** 카카오 로그아웃 + 기록 **/
//    fun logout(email: String) {
//        try {
//            UserApiClient.instance.logout { error ->
//                if (error != null) {
//                    ToastUtils(activity)
//                        .showMessage("로그아웃에 실패했습니다",1)
//                    writeLoginHistory(
//                        isLogin = false, platform = LOGIN_KAKAO, email = getUserEmail(activity),
//                        isAuto = null, isSuccess = false
//                    )
//                } else {
//                    writeLoginHistory(
//                        isLogin = false,
//                        platform = LOGIN_KAKAO,
//                        email = email,
//                        isAuto = null,
//                        isSuccess = true
//                    )
//                    RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = null)
//                }
//            }
//        } catch (e: UninitializedPropertyAccessException) {
//            e.printStackTrace()
//        }
//    }

    /** 클라이언트와 완전히 연결 끊기 **/
    fun disconnectFromKakao(pb: LottieAnimationView?) {
        UserApiClient.instance.unlink {
            pb?.let {
                RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = it)
            }
        }
    }
}