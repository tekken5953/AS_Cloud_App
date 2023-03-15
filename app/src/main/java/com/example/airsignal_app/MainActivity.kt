package com.example.airsignal_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.IgnoredKeyFile.lastLoginPhone
import com.example.airsignal_app.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.util.SharedPreferenceManager


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val googleLogin: GoogleLogin by lazy { GoogleLogin(this) }
    private val kakakoLogin: KakaoLogin by lazy { KakaoLogin(this) }
    private val naverLogin: NaverLogin by lazy { NaverLogin(this) }
    private var phoneNumber: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val lastLogin = SharedPreferenceManager(this).getString(lastLoginPlatform)

        @SuppressLint("SetTextI18n")
        binding.currentSort.text = "현재 로그인 : $lastLogin"

        phoneNumber = SharedPreferenceManager(this).getString(lastLoginPhone)

        binding.signOutGoogleButton.setOnClickListener {
            googleLogin.logout()
        }

        binding.signOutKakaoButton.setOnClickListener {
            kakakoLogin.initialize()
            kakakoLogin.logout(phoneNumber)
        }

        binding.signOutNaverButton.setOnClickListener {
            naverLogin.initialize()
            naverLogin.logout(phoneNumber)
        }

        binding.mainDialogBtn.setOnClickListener {
            val bottomSheet = AddDeviceDialog()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }
}