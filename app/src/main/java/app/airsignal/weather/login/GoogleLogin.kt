package app.airsignal.weather.login

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.dao.IgnoredKeyFile.googleDefaultClientId
import app.airsignal.weather.dao.StaticDataObject.TAG_LOGIN
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.dao.RDBLogcat.LOGIN_FAILED
import app.airsignal.weather.dao.RDBLogcat.LOGIN_GOOGLE
import app.airsignal.weather.dao.RDBLogcat.writeLoginHistory
import app.airsignal.weather.dao.RDBLogcat.writeLoginPref
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.core_databse.db.sp.SetAppInfo.setUserEmail
import app.airsignal.core_databse.db.sp.SetAppInfo.setUserId
import app.airsignal.core_databse.db.sp.SetAppInfo.setUserLoginPlatform
import app.airsignal.core_databse.db.sp.SetAppInfo.setUserProfile
import app.airsignal.weather.view.util.ToastUtils
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.orhanobut.logger.Logger

/**
 * @author : Lee Jae Young
 * @since : 2023-03-08 오후 3:47
 **/

class GoogleLogin(private val activity: Activity) {
    private var client: GoogleSignInClient
    private var lastLogin: GoogleSignInAccount? = null

    init {
        client = GoogleSignIn.getClient(activity, getGoogleSignInOptions())
        lastLogin = GoogleSignIn.getLastSignedInAccount(activity)
    }

    private fun getAccessToken(): String? {
        return try {
            lastLogin?.idToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** 로그인 진행 + 로그인 버튼 비활성화 **/
    fun login(mBtn: AppCompatButton, result: ActivityResultLauncher<Intent>) {
        try {
            val signInIntent: Intent = client.signInIntent
            result.launch(signInIntent)
            mBtn.alpha = 0.7f
        } catch (e: Exception) {
            Logger.t(TAG_LOGIN).e(e.stackTraceToString())
            RDBLogcat.writeErrorNotANR(activity, LOGIN_FAILED, e.localizedMessage!!)
        }
    }

    /** 토큰 유효성 검사 **/
    fun isValidToken() : Boolean {
        return lastLogin?.idToken != null
    }

    /** 로그아웃 진행 + 로그아웃 로그 저장 **/
    fun logout(pb: LottieAnimationView?) {
        client.signOut()
            .addOnCompleteListener {
                saveLogoutStatus()
                pb?.let {
                    RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = it)
                }
            }
            .addOnCanceledListener {
                ToastUtils(activity)
                    .showMessage("로그아웃에 실패했습니다",1)
            }
    }

    /** 자동 로그인 **/
    fun checkSilenceLogin() {
        client.silentSignIn()
            .addOnCompleteListener {
                handleSignInResult(it,isAuto = true)
            }
            .addOnFailureListener {
                ToastUtils(activity)
                    .showMessage("마지막 로그인 세션을 찾을 수 없습니다",1)
            }
    }

    /** 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정
     *
     * DEFAULT_SIGN_IN parameter 는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용**/
    private fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleDefaultClientId) // 토큰 요청
            .requestEmail() // email addresses 도 요청함
            .build()
    }

    /** 사용자의 로그인 정보를 저장
     *
     * TODO 구글로그인은 아직 테스팅 단계라 임시로 파라미터를 설정**/
    private fun saveLoginStatus(email: String, name: String?, profile: String?, isAuto: Boolean) {
        setUserLoginPlatform(activity, "google")
        writeLoginHistory(isLogin = true , platform = LOGIN_GOOGLE, email = email, isAuto = isAuto, isSuccess = true)
        writeLoginPref(activity, platform =  LOGIN_GOOGLE, email = email, phone = null, name = name, profile = profile)
    }

    /** 사용자 로그아웃 정보를 저장
     *
     * TODO 임시로 번호를 지정해 놓음**/
    private fun saveLogoutStatus() {
        writeLoginHistory(isLogin = false, platform = LOGIN_GOOGLE, email = lastLogin?.email!!, isAuto = null, isSuccess = true)
    }

    /** 로그인 이벤트 성공 **/
    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>, isAuto: Boolean) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account.email!!.lowercase()
            val displayName = account.displayName
            val id = account.id!!.lowercase()
            val photo: String = account?.photoUrl.toString()
            val token = account.idToken
            Logger.t(TAG_LOGIN).d(
                """
                gLogin
                Id : ${id}Account$account
                DisplayName : $displayName
                Token : $token
                Email : $email
                profile : $photo
                """.trimIndent()
            )

            setUserId(activity, displayName.toString())
            setUserProfile(activity, photo)
            setUserEmail(activity, email)

            saveLoginStatus(email, displayName, photo, isAuto)
        } catch (e: ApiException) {
            Logger.t(TAG_LOGIN).e(e.stackTraceToString())
            e.printStackTrace()
        }
    }
}