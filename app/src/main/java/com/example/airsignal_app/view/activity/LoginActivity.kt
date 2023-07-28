package com.example.airsignal_app.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.TAG_LOGIN
import com.example.airsignal_app.databinding.ActivityLoginBinding
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.login.PhoneLogin
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.view.ShowDialogClass
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.kakao.sdk.common.util.Utility.getKeyHash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity
    : BaseActivity<ActivityLoginBinding>() {
    override val resID: Int get() = R.layout.activity_login

    private val googleLogin by lazy { GoogleLogin(this) }   // 구글 로그인
    private val kakaoLogin by lazy { KakaoLogin(this) }     // 카카오 로그인
    private val naverLogin by lazy { NaverLogin(this) }     // 네이버 로그인

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.googleLoginButton.setOnClickListener {
            googleLogin.login(binding.googleLoginButton, startActivityResult)
        }

        binding.kakakoLoginButton.setOnClickListener {
            kakaoLogin.checkInstallKakaoTalk(binding.pbLayout)
        }

        binding.naverLoginButton.setOnClickListener {
            naverLogin.login()
        }

        println(getKeyHash(this))

        binding.phoneLoginButton.setOnClickListener {
            val viewEmailLogin: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_phone_input, null)
            val dialog = ShowDialogClass(this)
            dialog.show(viewEmailLogin, true)
            val inputEt: EditText = viewEmailLogin.findViewById(R.id.inputEt)
            val inputSendBtn: Button = viewEmailLogin.findViewById(R.id.inputSendBtn)
            val inputVerifyEt: EditText = viewEmailLogin.findViewById(R.id.inputNumberEt)
            val inputErrorText: TextView =
                viewEmailLogin.findViewById(R.id.inputResultText)

            inputSendBtn.setOnClickListener {
                if (inputSendBtn.isEnabled) {
                    if (inputSendBtn.text == "Send") {
                        inputVerifyEt.visibility = View.VISIBLE
                        inputSendBtn.text = "Verify"
                        PhoneLogin(this, inputSendBtn, inputErrorText)
                            .login(inputEt.text.toString().replaceFirst("0", "+82"))
                    } else if (inputSendBtn.text == "Verify") {
                        if (inputVerifyEt.text.toString() ==
                            SharedPreferenceManager(this).getString("verificationCode")
                        ) {
                            dialog.dismiss()
                            Thread.sleep(100)
                            binding.pbLayout.visibility = View.VISIBLE
                            Handler(Looper.getMainLooper()).postDelayed({
                                EnterPageUtil(this).toMain("email")
                                binding.pbLayout.visibility = View.GONE
                            }, 2000)
                        } else {
                            inputErrorText.visibility = View.VISIBLE
                            inputErrorText.text = "인증번호가 일치하지 않습니다"
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
            when (result.resultCode) {
                RESULT_OK -> {
                    val data = result.data
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    Log.d("testtest", "Google Login is OK : ${result.resultCode},${result.data}")

                    if (task.result.email == "tekken5953@naver.com") {
                        SubFCM().subAdminTopic()
                    }
                    googleLogin.handleSignInResult(task, isAuto = false)
                    EnterPageUtil(this).toMain("google")
                }
                RESULT_CANCELED -> {
                    Log.d("testtest", "Google Login is Canceled : ${result.resultCode}")
                    binding.googleLoginButton.isEnabled = true
                }
                RESULT_FIRST_USER -> {
                    Log.d("testtest", "Google Login is FIRST_USER : ${result.resultCode}")
                    binding.googleLoginButton.isEnabled = true
                }
                else -> {
                    Log.d("testtest", "Else : ${result.resultCode}")
                    binding.googleLoginButton.isEnabled = true
                }
            }
        }
}