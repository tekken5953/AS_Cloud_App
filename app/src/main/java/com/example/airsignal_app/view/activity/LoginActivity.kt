package com.example.airsignal_app.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.TAG_LOGIN
import com.example.airsignal_app.databinding.ActivityLoginBinding
import com.example.airsignal_app.login.EmailLogin
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.orhanobut.logger.Logger

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val googleLogin by lazy { GoogleLogin(this) } // 구글 로그인
    private val kakaoLogin by lazy { KakaoLogin(this).initialize() } // 카카오 로그인
    private val naverLogin by lazy { NaverLogin(this).initialize() } // 네이버 로그인

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.googleLoginButton.setOnClickListener {
            googleLogin.login(binding.googleLoginButton, startActivityResult)
        }

        binding.kakakoLoginButton.setOnClickListener {
            kakaoLogin.checkInstallKakaoTalk(binding.pbLayout)
        }

        binding.naverLoginButton.setOnClickListener {
            naverLogin.login()
        }

        binding.loginMainBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.emailLoginButton.setOnClickListener {
            val email = "play@airsignal.kr"
            val password = "1234567"
            EmailLogin(this).initialize().loginEmail(email, password)
        }
    }

    // 구글로그인 startActivityResult 변수
    private var startActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                googleLogin.handleSignInResult(task)
            } else {
                Logger.t(TAG_LOGIN).e("로그인 실패 $result")
                binding.googleLoginButton.isEnabled = true
            }
        }
}