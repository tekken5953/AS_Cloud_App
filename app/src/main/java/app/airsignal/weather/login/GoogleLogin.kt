package app.airsignal.weather.login

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.utils.plain.ToastUtils
import app.airsignal.weather.utils.view.RefreshUtils
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author : Lee Jae Young
 * @since : 2023-03-08 오후 3:47
 **/

class GoogleLogin(private val activity: Activity): KoinComponent {
    private val client: GoogleSignInClient get() = activity.let {GoogleSignIn.getClient(activity, getGoogleSignInOptions())}
    private val lastLogin: GoogleSignInAccount? get() = activity.let {GoogleSignIn.getLastSignedInAccount(activity)}

    private val toast: ToastUtils by inject()
    /** 로그인 진행 + 로그인 버튼 비활성화 **/
    fun login(mBtn: AppCompatButton, result: ActivityResultLauncher<Intent>) {
        kotlin.runCatching {
            val signInIntent: Intent = client.signInIntent
            result.launch(signInIntent)
            mBtn.alpha = 0.7f
        }.exceptionOrNull()?.stackTraceToString()
    }

    /** 토큰 유효성 검사 **/
    fun isValidToken(): Boolean = lastLogin?.idToken != null

    /** 로그아웃 진행 + 로그아웃 로그 저장 **/
    fun logout(pb: LottieAnimationView?) {
        client.signOut()
            .addOnCompleteListener {
                pb?.let {
                    activity.runOnUiThread {
                        RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = it)
                    }
                }
            }
            .addOnCanceledListener {
                toast.showMessage("로그아웃에 실패했습니다",1)
            }
    }

    /** 자동 로그인 **/
    fun checkSilenceLogin() {
        client.silentSignIn()
            .addOnCompleteListener { handleSignInResult(it) }
            .addOnFailureListener {
                toast.showMessage("마지막 로그인 세션을 찾을 수 없습니다",1)
            }
    }

    fun revokeAccess(pb: LottieAnimationView?) {
        val task = client.revokeAccess()
        task.addOnSuccessListener {
            pb?.let { RefreshUtils(activity).refreshActivityAfterSecond(sec = 1, pbLayout = it) }
        }
        task.addOnFailureListener {
            toast.showMessage("로그아웃에 실패했습니다")
        }
    }

    /** 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정
     *
     * DEFAULT_SIGN_IN parameter 는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용**/
    private fun getGoogleSignInOptions(): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(IgnoredKeyFile.GOOGLE_DEFAULT_CLIENT_ID) // 토큰 요청
            .requestEmail() // email addresses 도 요청함
            .build()

    /** 사용자의 로그인 정보를 저장
     *
     * TODO 구글로그인은 아직 테스팅 단계라 임시로 파라미터를 설정**/
    private fun saveLoginStatus() = SetAppInfo.setUserLoginPlatform(StaticDataObject.LOGIN_GOOGLE)

    /** 로그인 이벤트 성공 **/
    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        kotlin.runCatching {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account.email?.lowercase() ?: ""
            val displayName = account.displayName
            val photo: String = account?.photoUrl.toString()

            CoroutineScope(Dispatchers.IO).launch {
                SetAppInfo.setUserId(displayName.toString())
                SetAppInfo.setUserProfile(photo)
                SetAppInfo.setUserEmail(email)

                withContext(Dispatchers.Main) {
                    saveLoginStatus()
                    activity.finish()
                }
            }
        }.exceptionOrNull()?.stackTraceToString()
    }
}