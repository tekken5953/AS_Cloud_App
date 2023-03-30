package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.databinding.ActivityTestPageBinding
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.db.SharedPreferenceManager


class TestPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestPageBinding
    private val googleLogin: GoogleLogin by lazy { GoogleLogin(this) }
    private val kakakoLogin: KakaoLogin by lazy { KakaoLogin(this) }
    private val naverLogin: NaverLogin by lazy { NaverLogin(this) }
    private var email: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test_page)

        val lastLogin = SharedPreferenceManager(this).getString(lastLoginPlatform)

        @SuppressLint("SetTextI18n")
        binding.currentSort.text = "현재 로그인 : $lastLogin"

        email = SharedPreferenceManager(this).getString(userEmail)

        binding.signOutGoogleButton.setOnClickListener {
            googleLogin.logout()
        }

        binding.signOutKakaoButton.setOnClickListener {
            kakakoLogin.initialize()
            kakakoLogin.logout(email)
        }

        binding.signOutNaverButton.setOnClickListener {
            naverLogin.initialize()
            naverLogin.logout(email)
        }

    }
}