package com.example.airsignal_app.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.window.SplashScreen
import android.window.SplashScreenView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.dao.StaticDataObject.TAG_LOGIN
import com.example.airsignal_app.databinding.ActivityLoginBinding
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.orhanobut.logger.Logger

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleLogin: GoogleLogin
    private lateinit var kakaoLogin: KakaoLogin
    private lateinit var naverLogin: NaverLogin

    override fun onStart() {
        super.onStart()
        silentLogin()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        googleLogin = GoogleLogin(this) // 구글 로그인
        kakaoLogin = KakaoLogin(this).initialize()   // 카카오 로그인
        naverLogin = NaverLogin(this).initialize()   // 네이버 로그인

        binding.googleLoginButton.setOnClickListener {
            googleLogin.login(binding.googleLoginButton, startActivityResult)
        }

        binding.kakakoLoginButton.setOnClickListener {
            kakaoLogin.checkInstallKakaoTalk(binding.pbLayout)
        }

        binding.naverLoginButton.setOnClickListener {
            naverLogin.login()
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

    // 플랫폼 별 자동로그인
    private fun silentLogin() {
        when(SharedPreferenceManager(this).getString(lastLoginPlatform)) {
            "google" -> {
                // 구글 자동 로그인
                googleLogin.checkSilenceLogin()
            }
            "kakao" -> {
                // 카카오 자동 로그인
                kakaoLogin.isValidToken(binding.pbLayout)
            }
            "naver" -> {
                // 네이버 자동 로그인
                naverLogin.login()
            }
        }
    }
}