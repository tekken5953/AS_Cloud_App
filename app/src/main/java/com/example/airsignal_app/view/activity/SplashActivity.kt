package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.ConvertDataType.setFullScreenMode
import com.example.airsignal_app.dao.ConvertDataType.setLocaleToEnglish
import com.example.airsignal_app.dao.ConvertDataType.setLocaleToKorea
import com.example.airsignal_app.dao.ConvertDataType.setLocaleToSystem
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.db.SharedPreferenceManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val sp by lazy { SharedPreferenceManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 설정된 테마 정보 불러오기
        when(sp.getString("theme")) {
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        // 설정된 언어정보 불러오기
        when(sp.getString("lang")) {
            "korean" -> {
                setLocaleToKorea(this)
            }
            "english" -> {
                setLocaleToEnglish(this)
            }
            else -> {
                setLocaleToSystem(this)
            }
        }

        setFullScreenMode(this) // 풀 스크린

        // 2초 뒤 이동
        Handler(Looper.getMainLooper()).postDelayed({
           EnterPage(this).toLogin()
        },1000)
    }
}