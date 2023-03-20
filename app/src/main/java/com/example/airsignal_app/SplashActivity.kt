package com.example.airsignal_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.airsignal_app.util.ConvertDataType
import com.example.airsignal_app.util.SharedPreferenceManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val sp by lazy { SharedPreferenceManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
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

        when(sp.getString("lang")) {
            "korean" -> {
                ConvertDataType().setLocaleToKorea(this)
            }
            "english" -> {
                ConvertDataType().setLocaleToEnglish(this)
            }
            else -> {
                ConvertDataType().setLocaleToSystem(this)
            }
        }

        ConvertDataType().setFullScreenMode(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        },2000)
    }
}