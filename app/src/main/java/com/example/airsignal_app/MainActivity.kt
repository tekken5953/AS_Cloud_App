package com.example.airsignal_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.util.SharedPreferenceManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleLogin: GoogleLogin
    private lateinit var kakakoLogin: KakaoLogin
    private lateinit var naverLogin: NaverLogin
    private var phoneNumber: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val lastLogin = SharedPreferenceManager(this).getString("last_login")

        @SuppressLint("SetTextI18n")
        binding.currentSort.text = "현재 로그인 : $lastLogin"

        googleLogin = GoogleLogin(this)
        kakakoLogin = KakaoLogin(this)
        naverLogin = NaverLogin(this)

        phoneNumber = SharedPreferenceManager(this).getString("phone_number")

        binding.signOutGoogleButton.setOnClickListener {
            googleLogin.logout()
        }

        binding.signOutKakaoButton.setOnClickListener {
            kakakoLogin.getInstance()
            kakakoLogin.logout(phoneNumber)
        }

        binding.signOutNaverButton.setOnClickListener {
            naverLogin.initializing()
            naverLogin.logout(phoneNumber)
        }
    }
}