package app.airsignal.weather.login

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.ToastUtils
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

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

    /** 로그인 진행 + 로그인 버튼 비활성화 **/
    fun login(mBtn: AppCompatButton, result: ActivityResultLauncher<Intent>) {
        try {
            val signInIntent: Intent = client.signInIntent
            result.launch(signInIntent)
            mBtn.alpha = 0.7f
        } catch (e: Exception) {
            e.stackTraceToString()
        }
    }

    /** 토큰 유효성 검사 **/
    fun isValidToken() : Boolean { return lastLogin?.idToken != null }

    /** 로그아웃 진행 + 로그아웃 로그 저장 **/
    fun logout(pb: LottieAnimationView?) {
        client.signOut()
            .addOnCompleteListener {
                pb?.let {
                    RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = it)
                }
            }
            .addOnCanceledListener {
                ToastUtils(activity).showMessage("로그아웃에 실패했습니다",1)
            }
    }

    /** 자동 로그인 **/
    fun checkSilenceLogin() {
        client.silentSignIn()
            .addOnCompleteListener {
                handleSignInResult(it,isAuto = true)
            }
            .addOnFailureListener {
                ToastUtils(activity).showMessage("마지막 로그인 세션을 찾을 수 없습니다",1)
            }
    }

    /** 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정
     *
     * DEFAULT_SIGN_IN parameter 는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용**/
    private fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(IgnoredKeyFile.googleDefaultClientId) // 토큰 요청
            .requestEmail() // email addresses 도 요청함
            .build()
    }

    /** 사용자의 로그인 정보를 저장
     *
     * TODO 구글로그인은 아직 테스팅 단계라 임시로 파라미터를 설정**/
    private fun saveLoginStatus() {
        SetAppInfo.setUserLoginPlatform(activity, RDBLogcat.LOGIN_GOOGLE)
    }

    /** 로그인 이벤트 성공 **/
    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>, isAuto: Boolean) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account.email?.lowercase() ?: ""
            val displayName = account.displayName
            val id = account.id?.lowercase()
            val photo: String = account?.photoUrl.toString()
            val token = account.idToken

            SetAppInfo.setUserId(activity, displayName.toString())
            SetAppInfo.setUserProfile(activity, photo)
            SetAppInfo.setUserEmail(activity, email)

            saveLoginStatus()
            activity.finish()
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }
}