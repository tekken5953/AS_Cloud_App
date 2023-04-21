package com.example.airsignal_app.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.TAG_LOGIN
import com.example.airsignal_app.databinding.ActivityLoginBinding
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.login.PhoneLogin
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.view.ShowDialogClass
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.orhanobut.logger.Logger

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val googleLogin by lazy { GoogleLogin(this) } // 구글 로그인
    private val kakaoLogin by lazy { KakaoLogin(this) } // 카카오 로그인
    private val naverLogin by lazy { NaverLogin(this) } // 네이버 로그인

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

        binding.phoneLoginButton.setOnClickListener {
            val viewEmailLogin: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_phone_input, null)
            val dialog = ShowDialogClass().getInstance(this)
            dialog.show(viewEmailLogin, true)
            val phoneInputEt: EditText = viewEmailLogin.findViewById(R.id.phoneInputEt)
            val phoneInputSendBtn: Button = viewEmailLogin.findViewById(R.id.phoneInputSendBtn)
            val phoneInputVerifyEt: EditText = viewEmailLogin.findViewById(R.id.phoneInputVerifyEt)
            val phoneInputErrorText: TextView =
                viewEmailLogin.findViewById(R.id.phoneInputResultText)

            phoneInputSendBtn.setOnClickListener {
                if (phoneInputSendBtn.isEnabled) {
                    if (phoneInputSendBtn.text == "Send") {
                        phoneInputVerifyEt.visibility = View.VISIBLE
                        phoneInputSendBtn.text = "Verify"
                        PhoneLogin(this, phoneInputSendBtn, phoneInputErrorText)
                            .login(phoneInputEt.text.toString().replaceFirst("0", "+82"))
                    } else if (phoneInputSendBtn.text == "Verify") {
                        if (phoneInputVerifyEt.text.toString() ==
                            SharedPreferenceManager(this).getString("verificationCode")
                        ) {
                            dialog.dismiss()
                            Thread.sleep(100)
                            binding.pbLayout.visibility = View.VISIBLE
                            Handler(Looper.getMainLooper()).postDelayed({
                                EnterPage(this).toMain("email")
                                binding.pbLayout.visibility = View.GONE
                            }, 2000)
                        } else {
                            phoneInputErrorText.visibility = View.VISIBLE
                            phoneInputErrorText.text = "인증번호가 다릅니다"
                        }
                    }
                }
            }
        }

        binding.loginMainBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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
                googleLogin.handleSignInResult(task, "수동")
                EnterPage(this).toMain("google")
            } else {
                Logger.t(TAG_LOGIN).e("로그인 실패 $result")
                binding.googleLoginButton.isEnabled = true
            }
        }
}