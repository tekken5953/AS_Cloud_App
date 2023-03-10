package com.example.airsignal_app.login

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.airsignal_app.MainActivity
import com.example.airsignal_app.R
import com.example.airsignal_app.SignInActivity
import com.example.airsignal_app.firebase.RDBLogcat
import com.example.airsignal_app.util.LoggerUtil
import com.example.airsignal_app.util.SharedPreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.orhanobut.logger.Logger

/**
 * @user : USER
 * @autor : Lee Jae Young
 * @since : 2023-03-08 오후 3:47
 * @version : 1.0.0
 **/

class GoogleLogin(mActivity: Activity) {

    private val activity = mActivity
    private var client: GoogleSignInClient
    private var lastLogin: GoogleSignInAccount? = null
    private val rdbLog = RDBLogcat("Log")

    init {
        LoggerUtil().getInstance()
        client = GoogleSignIn.getClient(activity, getGoogleSignInOptions())
        lastLogin = GoogleSignIn.getLastSignedInAccount(activity)
    }

    // 로그인 진행 + 로그인 버튼 비활성화
    fun login(mBtn: SignInButton, result: ActivityResultLauncher<Intent>) {
        val signInIntent: Intent = client.signInIntent
        result.launch(signInIntent)
        mBtn.isEnabled = false
    }

    fun logout() {
        client.signOut()
            .addOnCompleteListener {
                Logger.t("TAG_LOGIN").d("정상적으로 로그아웃 성공")
                saveLogoutStatus()
                val intent = Intent(activity, SignInActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
            .addOnCanceledListener {
                Logger.t("TAG_LOGIN").e("로그아웃에 실패했습니다")
            }
    }

    fun checkSilenceLogin() {
        client.silentSignIn()
            .addOnCompleteListener {
                handleSignInResult(it)
                Logger.t("TAG_LOGIN").d("자동 로그인 됨")
                saveLoginStatus()
            }
            .addOnFailureListener {
                Logger.t("TAG_LOGIN").w("마지막 로그인 세션을 찾을 수 없습니다")
            }
    }

    private fun getGoogleSignInOptions(): GoogleSignInOptions {
        // 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정한다.
        // DEFAULT_SIGN_IN parameter 는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용된다.
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail() // email addresses 도 요청함
            .build()
    }

    private fun saveLoginStatus() {
        SharedPreferenceManager(activity).setString("last_login","구글")
        rdbLog.sendLogInWithPhone("로그인 성공", "010-4180-5953", "구글", "수동")
    }

    private fun saveLogoutStatus() {
        rdbLog.sendLogOutWithPhone("로그아웃 성공","010-4180-5953","구글")
    }

    // 로그인 이벤트 성공
    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account.email!!.lowercase()
            val displayName = account.displayName
            val id = account.id!!.lowercase()
            val photo: String = account?.photoUrl.toString()
            val token = account.idToken
            Logger.t("TAG_LOGIN").d(
                """
                gLogin
                Id : ${id}Account$account
                DisplayName : $displayName
                Token : $token
                Email : $email
                profile : $photo
                """.trimIndent()
            )
            saveLoginStatus()
            enterMainPage(activity, MainActivity())
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

    private fun enterMainPage(sendActivity: Activity, receiveActivity: Activity) {
        val intent = Intent(sendActivity, receiveActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}