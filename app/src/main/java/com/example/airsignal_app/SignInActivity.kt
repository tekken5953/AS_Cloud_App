package com.example.airsignal_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.databinding.ActivitySignInBinding
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.util.SharedPreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.kakao.sdk.common.util.Utility
import com.orhanobut.logger.Logger
import timber.log.Timber

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleLogin: GoogleLogin
    private lateinit var kakaoLogin: KakaoLogin
    private lateinit var naverLogin: NaverLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        googleLogin = GoogleLogin(this)
        kakaoLogin = KakaoLogin(this)
        naverLogin = NaverLogin(this)

        kakaoLogin.getInstance()
        naverLogin.initializing()

//        // 구글 자동 로그인
//        googleLogin.checkSilenceLogin()
//        // 카카오 자동 로그인
        kakaoLogin.isValidToken()

        binding.googleLoginButton.setOnClickListener {
            googleLogin.login(binding.googleLoginButton, startActivityResult)
        }

        binding.kakakoLoginButton.setOnClickListener {
            kakaoLogin.checkInstallKakaoTalk()
        }

        binding.naverLoginButton.setOnClickListener {
            naverLogin.login()
        }
    }

    private var startActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                googleLogin.handleSignInResult(task)
            } else {
                Logger.t("TAG_LOGIN").e("로그인 실패 $result")
                binding.googleLoginButton.isEnabled = true
            }
        }


}