package app.airsignal.weather.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.databinding.ActivityLoginBinding
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.login.GoogleLogin
import app.airsignal.weather.login.KakaoLogin
import app.airsignal.weather.login.NaverLogin
import app.airsignal.weather.utils.DataTypeParser
import app.airsignal.weather.utils.controller.ScreenController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import org.koin.android.ext.android.inject


class LoginActivity
    : BaseActivity<ActivityLoginBinding>() {
    override val resID: Int get() = R.layout.activity_login
    private val subFCM: SubFCM by inject()

    private val googleLogin by lazy { GoogleLogin(this) }   // 구글 로그인
    private val kakaoLogin by lazy { KakaoLogin(this) }     // 카카오 로그인
    private val naverLogin by lazy { NaverLogin(this).init() }     // 네이버 로그인

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        ScreenController(this).setStatusBar()

        // 구글 로그인 버튼 클릭
        binding.googleLoginButton.setOnClickListener {
            googleLogin.login(binding.googleLoginButton, startActivityResult)
        }

        // 카카오 로그인 버튼 클릭
        binding.kakakoLoginButton.setOnClickListener {
            kakaoLogin.checkInstallKakaoTalk(binding.kakakoLoginButton)
        }

        // 네이버 로그인 버튼 클릭
        binding.naverLoginButton.setOnClickListener {
            binding.naverLoginButton.alpha = 0.7f
            naverLogin.login(binding.naverLoginButton)
        }

        // 뒤로가기 버튼 클릭
        binding.loginMainBack.setOnClickListener { finish() }
    }

    // 구글로그인 startActivityResult 변수
    private var startActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                // 로그인 성공 함
                RESULT_OK -> {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    if (task.result.email == IgnoredKeyFile.notificationAdmin) subFCM.subAdminTopic()

                    googleLogin.handleSignInResult(task)
                }
                // 로그인 취소 됨
                RESULT_CANCELED -> binding.googleLoginButton.alpha = 1f
                else -> binding.googleLoginButton.alpha = 1f
            }
        }
}