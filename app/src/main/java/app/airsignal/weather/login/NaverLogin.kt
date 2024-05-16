package app.airsignal.weather.login

import android.app.Activity
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.db.sp.SharedPreferenceManager
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.ToastUtils
import com.airbnb.lottie.LottieAnimationView
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse


/**
 * @author : Lee Jae Young
 * @since : 2023-03-09 오후 2:56
 **/

class NaverLogin(private val activity: Activity) {
    private val toast by lazy {ToastUtils(activity)}

    fun init(): NaverLogin {
        NaverIdLoginSDK.initialize(
            activity,
            IgnoredKeyFile.naverDefaultClientId,
            IgnoredKeyFile.naverDefaultClientSecret,
            IgnoredKeyFile.naverDefaultClientName
        )

        return this
    }

    /** 로그인
     *
     * TODO 로그인 기록 저장
     * **/
    fun login(naverLoginButton: AppCompatButton) {
        naverLoginButton.alpha = 1f
        NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
    }

    fun silentLogin() {
        NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
    }

    /** 로그아웃 + 기록 저장 */
    fun logout() {
        NaverIdLoginSDK.logout()
        RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = null)
    }

    /** 엑세스 토큰 불러오기
     *
     * @return String?
     * **/
    fun getAccessToken(): String? {
        return NaverIdLoginSDK.getAccessToken()
    }

    /** 엑세스 토큰 리프래시 **/
    suspend fun refreshToken() { NidOAuthLogin().refreshToken() }

    // 프로필 콜벡 메서드
    val profileCallback = object : NidProfileCallback<NidProfileResponse> {
        override fun onSuccess(result: NidProfileResponse) {
            result.profile?.let {
                SharedPreferenceManager(activity)
                    .setString(IgnoredKeyFile.lastLoginPhone, it.mobile.toString())
                    .setString(IgnoredKeyFile.userId, it.name.toString())
                    .setString(IgnoredKeyFile.userProfile, it.profileImage ?: "")
                    .setString(IgnoredKeyFile.userEmail, it.email.toString())

                SetAppInfo.setUserLoginPlatform(activity, RDBLogcat.LOGIN_NAVER)
                activity.finish()
            }
        }

        override fun onFailure(httpStatus: Int, message: String) {
            toast.showMessage("프로필을 불러오는데 실패했습니다",1)
        }

        override fun onError(errorCode: Int, message: String) { onFailure(errorCode, message) }
    }

    /** 로그인 콜벡 메서드 **/
    private val oauthLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
            //로그인 유저 정보 가져오기
            NidOAuthLogin().callProfileApi(profileCallback)
        }

        override fun onFailure(httpStatus: Int, message: String) {
            toast.showMessage(activity.getString(R.string.require_login),1)
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    /** 로그인 세션 유지 확인 **/
    fun isLogin() : Boolean {
        return NidOAuthLogin().callProfileApi(profileCallback).isCompleted
    }

    /** 네이버 클라이언트와 연동 해제 **/
    fun disconnectFromNaver(pb: LottieAnimationView?) {
        NidOAuthLogin().callDeleteTokenApi(activity, object : OAuthLoginCallback {
            override fun onSuccess() {
                //서버에서 토큰 삭제에 성공한 상태입니다.
                toast.showMessage(activity.getString(R.string.naver_disconnect),1)
                pb?.let {
                    RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = it)
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                println(NaverIdLoginSDK.getLastErrorDescription())
                activity.recreate()
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
                activity.recreate()
            }
        })
    }
}