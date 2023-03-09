package com.example.airsignal_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleLogin: GoogleLogin
    private lateinit var kakakoLogin: KakaoLogin
    private lateinit var naverLogin: NaverLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        googleLogin = GoogleLogin(this)
        kakakoLogin = KakaoLogin(this)
        naverLogin = NaverLogin(this)

        binding.signOutGoogleButton.setOnClickListener {
            googleLogin.logout()
        }

        binding.signOutKakaoButton.setOnClickListener {
            kakakoLogin.signOut()
        }

        binding.signOutNaverButton.setOnClickListener {
            naverLogin.logout()
        }
    }
}