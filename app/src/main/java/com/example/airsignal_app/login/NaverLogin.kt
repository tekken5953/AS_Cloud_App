package com.example.airsignal_app.login

import android.app.Activity
import com.example.airsignal_app.R
import com.example.airsignal_app.firebase.RDBLogcat
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.util.SharedPreferenceManager
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.orhanobut.logger.Logger

/**
 * @user : USER
 * @autor : Lee Jae Young
 * @since : 2023-03-09 오후 2:56
 * @version : 1.0.0
 **/
class NaverLogin(mActivity: Activity) {
    private val activity = mActivity
    private val naverClientId = activity.getString(R.string.social_login_info_naver_client_id)
    private val naverClientSecret =
        activity.getString(R.string.social_login_info_naver_client_secret)
    private val naverClientName = activity.getString(R.string.social_login_info_naver_client_name)
    private val rdbLog = RDBLogcat("Log")

    fun initializing() {
        NaverIdLoginSDK.initialize(activity, naverClientId, naverClientSecret, naverClientName)
    }

    fun login() {
        NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
    }

    fun logout(phone: String) {
        NaverIdLoginSDK.logout()
//        enterLoginPage()
        Logger.t("TAG_LOGIN").d("네이버 아이디 로그아웃 성공")
        rdbLog.sendLogOutWithPhone("로그아웃 성공",phone, "네이버")
    }

    // 엑세스 토큰 불러오기
    fun getAccessToken(): String? {
        return NaverIdLoginSDK.getAccessToken()
    }

    // 엑세스 토큰 리프래시
    suspend fun refreshToken() {
        NidOAuthLogin().refreshToken()
    }

    val profileCallback = object : NidProfileCallback<NidProfileResponse> {
        override fun onSuccess(result: NidProfileResponse) {
            val userId = result.profile?.id
            val phone = result.profile?.mobile.toString()
            SharedPreferenceManager(activity).setString("phone_number",phone)
            Logger.t("TAG_LOGIN").d("네이버 로그인 성공")
            Logger.t("TAG_LOGIN").d(
                "user id : $userId\n" +
                        "mobile : $phone\n" +
                        "token Type : ${NaverIdLoginSDK.getTokenType()}\n" +
                        "access : $NaverIdLoginSDK.getAccessToken()\n" +
                        "refresh : ${NaverIdLoginSDK.getRefreshToken()}\n" +
                        "expired at : ${NaverIdLoginSDK.getExpiresAt()}\n" +
                        "state : ${NaverIdLoginSDK.getState()}"
            )

            rdbLog.sendLogInWithPhone("로그인 성공",phone,"네이버","수동")
            enterMainPage()
        }

        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            Logger.t("TAG_LOGIN").e(
                "errorCode: $errorCode\n" +
                        "errorDescription: $errorDescription"
            )
            rdbLog.sendLogToFail("네이버 로그인 실패", "$errorCode - $errorDescription")
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    private val oauthLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
            //로그인 유저 정보 가져오기
            NidOAuthLogin().callProfileApi(profileCallback)
        }

        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            Logger.t("TAG_LOGIN").e(
                "errorCode: $errorCode\n" +
                        "errorDescription: $errorDescription"
            )
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    private fun enterMainPage() {
       EnterPage(activity).toMain("네이버")
    }

    private fun enterLoginPage() {
       EnterPage(activity).toLogin()
    }

    private fun disconnectFromNaver() {
        NidOAuthLogin().callDeleteTokenApi(activity, object : OAuthLoginCallback {
            override fun onSuccess() {
                //서버에서 토큰 삭제에 성공한 상태입니다.
                Logger.t("TAG_LOGIN").d("네이버 로그인 서비스와의 연동을 해제하였습니다다")
            }

            override fun onFailure(httpStatus: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                Logger.t("TAG_LOGIN").e("errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Logger.t("TAG_LOGIN").e("errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }

            override fun onError(errorCode: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                onFailure(errorCode, message)
            }
        })
    }
}