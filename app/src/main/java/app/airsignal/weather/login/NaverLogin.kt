package app.airsignal.weather.login

import android.app.Activity
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.db.sp.SharedPreferenceManager
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.utils.plain.ToastUtils
import app.airsignal.weather.utils.view.RefreshUtils
import app.airsignal.weather.view.activity.LoginActivity
import com.airbnb.lottie.LottieAnimationView
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 * @author : Lee Jae Young
 * @since : 2023-03-09 오후 2:56
 **/

class NaverLogin(private val activity: Activity): KoinComponent {
    private val toast: ToastUtils by inject()
<<<<<<< HEAD
=======
    private val sp: SharedPreferenceManager by inject()
>>>>>>> f5127faf2733fe7a95cb90d2e31e3722846e9b16

    fun init(): NaverLogin {
        NaverIdLoginSDK.initialize(
            activity,
            IgnoredKeyFile.NAVER_DEFAULT_CLIENT_ID,
            IgnoredKeyFile.NAVER_DEFAULT_CLIENT_SECRETE,
            IgnoredKeyFile.NAVER_DEFAULT_CLIENT_NAME
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
        kotlin.runCatching {
            NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
        }.exceptionOrNull()?.stackTraceToString()
    }

    /** 로그아웃 + 기록 저장 */
    fun logout(pb: LottieAnimationView?) {
        NaverIdLoginSDK.logout()
        pb?.let {
            activity.runOnUiThread {
                RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = it)
            }
        }
    }

    /** 엑세스 토큰 불러오기
     *
     * @return String?
     * **/
    fun getAccessToken(): String? {
        kotlin.runCatching {
            return NaverIdLoginSDK.getAccessToken()
        }

        return null
    }

    /** 엑세스 토큰 리프래시 **/
    suspend fun refreshToken() {
        CoroutineScope(Dispatchers.Default).launch {
            val refreshResult = NidOAuthLogin().refreshToken()
            if (refreshResult) NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
            else ToastUtils(activity).showMessage("로그인에 실패했습니다")
        }
    }

    // 프로필 콜벡 메서드
    val profileCallback = object : NidProfileCallback<NidProfileResponse> {
        override fun onSuccess(result: NidProfileResponse) {
            result.profile?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    sp.setString(SpDao.LAST_LOGIN_PHONE, it.mobile.toString())
                        .setString(SpDao.USER_ID, it.name.toString())
                        .setString(SpDao.USER_PROFILE, it.profileImage ?: "")
                        .setString(SpDao.USER_EMAIL, it.email.toString())
                    SetAppInfo.setUserLoginPlatform(StaticDataObject.LOGIN_NAVER)

                    withContext(Dispatchers.Main) {
                        if (activity is LoginActivity) activity.finish()
                    }
                }
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
    fun isLogin() : Boolean = NidOAuthLogin().callProfileApi(profileCallback).isCompleted

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