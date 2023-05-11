package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.airsignal_app.R
import com.example.airsignal_app.util.ConvertDataType.setFullScreenMode
import com.google.firebase.database.FirebaseDatabase

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        FirebaseDatabase.getInstance()

        val loadingGif = findViewById<ImageView>(R.id.splashLoading)
        Glide.with(this).asGif().load(R.drawable.loading_gif).override(100,100).into(loadingGif)

        setFullScreenMode(this) // 풀 스크린

        // 2초 뒤 이동
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, RedirectPermissionActivity::class.java)
            startActivity(intent)
            finish()
        },1000)
    }
}