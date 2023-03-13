package com.example.airsignal_app.login

import android.app.Activity
import android.content.Intent
import com.example.airsignal_app.IgnoredKeyFile.NATIVE_APP_KEY
import com.example.airsignal_app.IgnoredKeyFile.TAG_LOGIN
import com.example.airsignal_app.SignInActivity
import com.example.airsignal_app.firebase.RDBLogcat
import com.example.airsignal_app.util.EnterPage
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.orhanobut.logger.Logger
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-03-09 오전 11:29
 **/

class KakaoLogin(mActivity: Activity) {
    private val activity = mActivity
    private val rdbLog = RDBLogcat("Log")

    fun initialize  () {
        KakaoSdk.init(activity, NATIVE_APP_KEY)
    }

    /** 카카오톡 설치 확인 **/
    fun checkInstallKakaoTalk() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                // 로그인 실패 부분
                if (error != null) {
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
                        ) // 카카오 이메일 로그인
                    }
                }
                // 로그인 성공 부분
                else {
                    token?.let {
                        loginSilenceKakao()
                        enterMainPage()
                    }

                    rdbLog.sendLogInWithPhoneForKakao(activity,"로그인 성공", "카카오톡", "수동")
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
            rdbLog.sendLogToFail("로그인 실패", error.toString())
        } else {
            token?.let {
                loginSilenceKakao()
                enterMainPage()
            }

            rdbLog.sendLogInWithPhoneForKakao(activity,"로그인 성공", "카카오 이메일", "수동")
        }
    }

    /** 자동 로그인 **/
    fun isValidToken() {
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        // 만료된 토큰임 로그인 필요
                        Logger.t(TAG_LOGIN).w("만료된 토큰입니다")
                    } else {
                        //기타 에러
                        Logger.t("TAG_LOG").e("기타 에러 발생 : $error")
                    }
                } else {
                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                    enterMainPage()
                    tokenInfo?.let {
                        Logger.t(TAG_LOGIN)
                            .d(
                                "카카오 자동로그인 성공\n" +
                                        "user code is ${it}\n"
                            )


                    }
                    rdbLog.sendLogInWithPhoneForKakao(activity,"로그인 성공", "카카오", "자동")

//                    getUserData()
                }
            }
        } else {
            // 토큰이 없음 로그인 필요
            Logger.t("TAG_LOG").w("토큰이 없음 로그인 필요")
        }
    }

    /** 카카오 자동 로그인
     * @return OAuthToken? **/
    private fun loginSilenceKakao() : OAuthToken? {
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
        EnterPage(activity).toMain("카카오")
    }

    private fun getUserAllData() {
        UserApiClient.instance.me { user, _ ->
            if (user != null) {
                val array = user.toString()
                    .replace("User(", "\n------------------------------\n")
                    .replace(user.toString().last().toString(), "\n------------------------------")
                    .replace("=", ":")
                    .replace(",", "\n")
                Timber.tag(TAG_LOGIN).d(array)
            }
        }
    }

    private fun refreshToken() {
        //TODO 토큰 갱신하기 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#refresh-token
    }

    /** 카카오 로그아웃 + 기록 **/
    fun logout(phone: String) {
        try {
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Logger.t(TAG_LOGIN).e("로그아웃에 실패함 : $error")
                    rdbLog.sendLogToFail("카카오 로그아웃 실패", error.toString())
                } else {
                    Logger.t(TAG_LOGIN).d("정상적으로 로그아웃 성공")
                    rdbLog.sendLogOutWithPhone("로그아웃 성공", phone.replace("+82 ","0"), "카카오")
                    val intent = Intent(activity, SignInActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    /** 클라이언트와 완전히 연결 끊기 **/
    fun disconnectFromKakao() {
        // 연결 끊기
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Logger.t(TAG_LOGIN).e("연결 끊기 실패 : $error")
            } else {
                Logger.t(TAG_LOGIN).i("연결 끊기 성공. SDK에서 토큰 삭제 됨")
            }
        }
    }
}