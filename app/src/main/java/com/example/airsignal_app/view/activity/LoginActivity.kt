package com.example.airsignal_app.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.TAG_LOGIN
import com.example.airsignal_app.databinding.ActivityLoginBinding
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_GOOGLE
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.util.EnterPageUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import timber.log.Timber


class LoginActivity
    : BaseActivity<ActivityLoginBinding>() {
    override val resID: Int get() = R.layout.activity_login

    private val googleLogin by lazy { GoogleLogin(this) }   // 구글 로그인
    private val kakaoLogin by lazy { KakaoLogin(this) }     // 카카오 로그인
    private val naverLogin by lazy { NaverLogin(this) }     // 네이버 로그인

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

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
        binding.loginMainBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // 구글로그인 startActivityResult 변수
    private var startActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                // 로그인 성공 함
                RESULT_OK -> {
                    val data = result.data
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                    if (task.result.email == "tekken5953@naver.com") {
                        SubFCM().subAdminTopic()
                    }
                    googleLogin.handleSignInResult(task, isAuto = false)
                    EnterPageUtil(this).toMain(LOGIN_GOOGLE)
                }
                // 로그인 취소 됨
                RESULT_CANCELED -> {
                    Timber.tag(TAG_LOGIN).w("Cancel Google Login")
                    binding.googleLoginButton.alpha = 1f
                }
                else -> {
                    binding.googleLoginButton.alpha = 1f
                }
            }
        }
}