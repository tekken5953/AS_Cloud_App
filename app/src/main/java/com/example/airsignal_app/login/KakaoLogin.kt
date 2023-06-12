package com.example.airsignal_app.login

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.airsignal_app.dao.IgnoredKeyFile.KAKAO_NATIVE_APP_KEY
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPhone
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.IgnoredKeyFile.userId
import com.example.airsignal_app.dao.IgnoredKeyFile.userProfile
import com.example.airsignal_app.dao.StaticDataObject.TAG_LOGIN
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.firebase.db.RDBLogcat.sendLogInWithEmailForKakao
import com.example.airsignal_app.firebase.db.RDBLogcat.sendLogOutWithEmail
import com.example.airsignal_app.firebase.db.RDBLogcat.sendLogToFail
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author : Lee Jae Young
 * @since : 2023-03-09 오전 11:29
 **/

class KakaoLogin(private val activity: Activity) {

    init {
        KakaoSdk.init(activity, KAKAO_NATIVE_APP_KEY)
    }

    /** 앱 히시키 받아오기 **/
    fun getKeyHash(): String {
        return Utility.getKeyHash(activity)
    }

    /** 카카오톡 설치 확인 후 로그인**/
    fun checkInstallKakaoTalk(pb: LinearLayout) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            pb.visibility = View.VISIBLE
            pb.bringToFront()
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                // 로그인 실패 부분
                if (error != null) {
                    pb.visibility = View.GONE
                    Logger.t(TAG_LOGIN).e("로그인 실패")
                    // 사용자가 취소
                    if ((error is ClientError) && (error.reason == ClientErrorCause.Cancelled)) {
                        Logger.t(TAG_LOGIN).e("로그인 실패 원인 : 사용자가 취소 - $error")
                        return@loginWithKakaoTalk
                    }
                    // 다른 오류
                    else {
                        Logger.t(TAG_LOGIN).e("로그인 실패 원인 : 다른 오류 - $error")
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
                    sendLogInWithEmailForKakao(activity, "로그인 성공", "카카오톡", "수동")
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
            Logger.t(TAG_LOGIN).e("로그인 실패 : Cause is $error")
            sendLogToFail(
                getUserEmail(activity),
                "로그인 실패",
                error.toString())
        } else {
            token?.let {
                loginSilenceKakao()
                enterMainPage()
            }

            sendLogInWithEmailForKakao(activity, "로그인 성공", "카카오 이메일", "수동")
        }
    }

    fun getAccessToken() : Boolean {
        return AuthApiClient.instance.hasToken()
    }

    /** 자동 로그인 **/
    fun isValidToken(pb: MotionLayout) {
        pb.visibility = View.VISIBLE
        pb.bringToFront()
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error != null) {
                    pb.visibility = View.GONE
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        Logger.t(TAG_LOGIN).w("만료된 토큰입니다") // 만료된 토큰임 로그인 필요
                    } else {
                        Logger.t("TAG_LOG").e("기타 에러 발생 : $error") //기타 에러
                    }
                } else {
                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                    RefreshUtils(activity).refreshActivity()
                    tokenInfo?.let {
                        Logger.t(TAG_LOGIN)
                            .d(
                                "카카오 자동로그인 성공\n" +
                                        "user code is ${it}\n"
                            )
                    }
                }
            }
        } else {
            // 토큰이 없음 로그인 필요
            Logger.t("TAG_LOG").w("토큰이 없음 로그인 필요")
            pb.visibility = View.GONE
        }
    }

    /** 카카오 자동 로그인
     * @return OAuthToken? **/
    private fun loginSilenceKakao(): OAuthToken? {
        val token = TokenManagerProvider.instance.manager.getToken()
        token?.let {
            Logger.t(TAG_LOGIN)
                .d(
                    "카카오 로그인 성공\n" +
                            "user code is ${it.idToken}\n" +
                            "access is ${it.accessToken}\naccess was expired at ${it.accessTokenExpiresAt}\n" +
                            "refresh is ${it.refreshToken}\nrefresh was expired at ${it.refreshTokenExpiresAt}"
                )
        }
        return token
    }

    private fun enterMainPage() {
        CoroutineScope(Dispatchers.IO).launch {
            saveUserSettings()
            delay(1000)
            EnterPageUtil(activity).toMain("kakao")
        }
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
//
//    private fun refreshToken() {
//        //TODO 토큰 갱신하기 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#refresh-token
//    }

    /** 카카오 로그아웃 + 기록 **/
    fun logout(email: String) {
        try {
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Logger.t(TAG_LOGIN).e("로그아웃에 실패함 : $error")
                    sendLogToFail(
                        getUserEmail(activity),
                        "카카오 로그아웃 실패",
                        error.toString())
                } else {
                    Logger.t(TAG_LOGIN).d("정상적으로 로그아웃 성공")
                    sendLogOutWithEmail(email,"로그아웃 성공", "카카오")
                    RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = null)
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    /** 클라이언트와 완전히 연결 끊기 **/
    fun disconnectFromKakao() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Logger.t(TAG_LOGIN).e("연결 끊기 실패 : $error")
            } else {
                EnterPageUtil(activity).toLogin()
                Logger.t(TAG_LOGIN).i("연결 끊기 성공. SDK 에서 토큰 삭제 됨")
            }
        }
    }
}