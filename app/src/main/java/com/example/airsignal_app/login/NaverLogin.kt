package com.example.airsignal_app.login

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPhone
import com.example.airsignal_app.dao.IgnoredKeyFile.naverDefaultClientId
import com.example.airsignal_app.dao.IgnoredKeyFile.naverDefaultClientName
import com.example.airsignal_app.dao.IgnoredKeyFile.naverDefaultClientSecret
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.IgnoredKeyFile.userId
import com.example.airsignal_app.dao.IgnoredKeyFile.userProfile
import com.example.airsignal_app.dao.StaticDataObject.TAG_LOGIN
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.firebase.db.RDBLogcat.sendLogInWithEmail
import com.example.airsignal_app.firebase.db.RDBLogcat.sendLogOutWithEmail
import com.example.airsignal_app.firebase.db.RDBLogcat.sendLogToFail
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.util.RefreshUtils
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.orhanobut.logger.Logger


/**
 * @author : Lee Jae Young
 * @since : 2023-03-09 오후 2:56
 **/

class NaverLogin(private val activity: Activity) {
    private val sp by lazy { SharedPreferenceManager(activity) }

    fun initialize() : NaverLogin {
        //TODO 정식버전이 되면 동적할당
        NaverIdLoginSDK.initialize(
            activity,
            naverDefaultClientId,
            naverDefaultClientSecret,
            naverDefaultClientName
        )
        return this
    }

    /** 로그인
     *
     * TODO 로그인 기록 저장
     * **/
    fun login() {
        NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
    }

    fun silentLogin() {
        NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
    }

    /** 로그아웃 + 기록 저장 */
    @RequiresApi(Build.VERSION_CODES.O)
    fun logout(email: String) {
        NaverIdLoginSDK.logout()
        Logger.t(TAG_LOGIN).d("네이버 아이디 로그아웃 성공")
        sendLogOutWithEmail(email, "로그아웃 성공", "네이버")
        RefreshUtils(activity).refreshActivityAfterSecond(sec = 1)
    }

    /** 엑세스 토큰 불러오기
     *
     * @return String?
     * **/
    fun getAccessToken(): String? {
        return NaverIdLoginSDK.getAccessToken()
    }

    /** 엑세스 토큰 리프래시 **/
    suspend fun refreshToken() {
        NidOAuthLogin().refreshToken()
    }

    // 프로필 콜벡 메서드
    val profileCallback = object : NidProfileCallback<NidProfileResponse> {
        override fun onSuccess(result: NidProfileResponse) {
            result.profile?.let {
                Logger.t(TAG_LOGIN).d("네이버 로그인 성공")

                 sp .setString(lastLoginPhone, it.mobile.toString())
                    .setString(userId, it.name.toString())
                    .setString(userProfile, it.profileImage!!)
                    .setString(userEmail, it.email.toString())

                sendLogInWithEmail("로그인 성공", it.email.toString(), "네이버", "수동")
                EnterPage(activity).toMain("naver")
            }
        }

        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            Logger.t(TAG_LOGIN).e(
                "errorCode: $errorCode\n" +
                        "errorDescription: $errorDescription"
            )
            sendLogToFail(
                sp.getString(userEmail),
                "네이버 로그인 실패",
                "$errorCode - $errorDescription")
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    // 로그인 콜벡 메서드
    private val oauthLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
            //로그인 유저 정보 가져오기
            NidOAuthLogin().callProfileApi(profileCallback)
        }

        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            Logger.t(TAG_LOGIN).e(
                "errorCode: $errorCode\n" +
                        "errorDescription: $errorDescription"
            )
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    // 로그인 페이지로 이동
    private fun enterLoginPage() {
        EnterPage(activity).toLogin()
    }

    fun isLogin() : Boolean {
       return NidOAuthLogin().callProfileApi(profileCallback).isCompleted
    }

    /** 네이버 클라이언트와 연동 해제 **/
    fun disconnectFromNaver() {
        NidOAuthLogin().callDeleteTokenApi(activity, object : OAuthLoginCallback {
            override fun onSuccess() {
                //서버에서 토큰 삭제에 성공한 상태입니다.
                Logger.t(TAG_LOGIN).d("네이버 로그인 서비스와의 연동을 해제하였습니다다")
                enterLoginPage()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                Logger.t(TAG_LOGIN).e("errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Logger.t(TAG_LOGIN).e("errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }

            override fun onError(errorCode: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                onFailure(errorCode, message)
            }
        })
    }
}