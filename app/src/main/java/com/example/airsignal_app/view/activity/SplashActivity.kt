package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.dao.IgnoredKeyFile.userLocation
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.GpsRepository
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.util.ConvertDataType.setFullScreenMode
import com.example.airsignal_app.util.ConvertDataType.setLocaleToEnglish
import com.example.airsignal_app.util.ConvertDataType.setLocaleToKorea
import com.example.airsignal_app.util.ConvertDataType.setLocaleToSystem
import com.example.airsignal_app.util.SensibleTempFormula
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        FirebaseDatabase.getInstance()

        val loadingGif = findViewById<ImageView>(R.id.splashLoading)
        Glide.with(this).asGif().load(R.drawable.loading_gif).override(100,100).into(loadingGif)

        val sp = SharedPreferenceManager(this)
        GetLocation(this@SplashActivity).getLocation()

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
        when(sp.getString(userLocation)) {
            getString(R.string.korean) -> {
                setLocaleToKorea(this)
            }
            getString(R.string.english) -> {
                setLocaleToEnglish(this)
            }
            else -> {
                setLocaleToSystem(this)
            }
        }

        setFullScreenMode(this) // 풀 스크린

        // 2초 뒤 이동
        Handler(Looper.getMainLooper()).postDelayed({
            if (sp.getString(lastLoginPlatform) != "") {
                EnterPage(this).toMain(sp.getString(lastLoginPlatform))
            } else {
                EnterPage(this).toMain(null)
            }
        },2000)
    }
}