package app.airsignal.weather.login

import android.app.Activity
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile.lastLoginPhone
import app.airsignal.weather.dao.IgnoredKeyFile.naverDefaultClientId
import app.airsignal.weather.dao.IgnoredKeyFile.naverDefaultClientName
import app.airsignal.weather.dao.IgnoredKeyFile.naverDefaultClientSecret
import app.airsignal.weather.dao.IgnoredKeyFile.userEmail
import app.airsignal.weather.dao.IgnoredKeyFile.userId
import app.airsignal.weather.dao.IgnoredKeyFile.userProfile
import app.airsignal.weather.dao.StaticDataObject.TAG_LOGIN
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.firebase.db.RDBLogcat.LOGIN_NAVER
import app.airsignal.weather.firebase.db.RDBLogcat.writeErrorNotANR
import app.airsignal.weather.firebase.db.RDBLogcat.writeLoginHistory
import app.airsignal.weather.firebase.db.RDBLogcat.writeLoginPref
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.`object`.GetAppInfo.getUserEmail
import app.airsignal.weather.view.util.ToastUtils
import com.airbnb.lottie.LottieAnimationView
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
    private val toast = ToastUtils(activity)

    init {
        NaverIdLoginSDK.initialize(
            activity,
            naverDefaultClientId,
            naverDefaultClientSecret,
            naverDefaultClientName
        )
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
        writeLoginHistory(
            isLogin = false,
            platform = LOGIN_NAVER,
            email = getUserEmail(activity),
            isAuto = null,
            isSuccess = true
        )
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
    suspend fun refreshToken() {
        NidOAuthLogin().refreshToken()
    }

    // 프로필 콜벡 메서드
    val profileCallback = object : NidProfileCallback<NidProfileResponse> {
        override fun onSuccess(result: NidProfileResponse) {
            result.profile?.let {
                SharedPreferenceManager(activity)
                    .setString(lastLoginPhone, it.mobile.toString())
                    .setString(userId, it.name.toString())
                    .setString(userProfile, it.profileImage!!)
                    .setString(userEmail, it.email.toString())

                writeLoginHistory(isLogin = true, platform = LOGIN_NAVER, email = it.email.toString(),
                    isAuto = false, isSuccess = true)

                writeLoginPref(activity,
                    platform = LOGIN_NAVER,
                    email = it.email.toString(),
                    phone = it.mobile.toString(),
                    name = it.name.toString(),
                    profile = it.profileImage.toString()
                )

                EnterPageUtil(activity).toMain(LOGIN_NAVER)
            }
        }

        override fun onFailure(httpStatus: Int, message: String) {
            toast.showMessage("프로필을 불러오는데 실패했습니다",1)
            writeLoginHistory(
                isLogin = true, platform = LOGIN_NAVER, email = getUserEmail(activity),
                isAuto = false, isSuccess = false
            )
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
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            writeErrorNotANR(activity,"naver login","error $errorCode $errorDescription")
            toast.showMessage("로그인이 필요합니다",1)
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    /** 로그인 페이지로 이동 **/
    private fun enterLoginPage() {
        EnterPageUtil(activity).toLogin()
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
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                Logger.t(TAG_LOGIN).e("errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Logger.t(TAG_LOGIN).e("errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
                activity.recreate()
            }

            override fun onError(errorCode: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                onFailure(errorCode, message)
                activity.recreate()
            }
        })
    }
}