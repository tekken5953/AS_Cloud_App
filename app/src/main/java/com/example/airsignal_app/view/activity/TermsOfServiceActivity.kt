package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityTermsOfServiceBinding

class TermsOfServiceActivity : BaseActivity<ActivityTermsOfServiceBinding>() {
    override val resID: Int get() = R.layout.activity_terms_of_service

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        window.statusBarColor = getColor(R.color.theme_view_color)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        binding.termsServiceBackIv.setOnClickListener {
            finish()
        }

        val webSettings = binding.termsServiceWebView.settings
        webSettings.apply {
            javaScriptEnabled = true // 자바스크립트 허용
            builtInZoomControls = true
            setSupportZoom(true) // 핀치 줌 허용
            loadWithOverviewMode = true // 메타태그 허용
            useWideViewPort = true // 화면 맞추기
            domStorageEnabled = true // 로컬 저장소 허용
            cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용
        }

        binding.termsServiceWebView.apply {
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }

        val pdfUrl = "https://docs.google.com/document/d/e/2PACX-1vTmaf0Wg9zhNZfe-_S-4eWDj1XLwbYlUcoONys3MzzTEAx-_QLlJOuTGo7uQjl5FbyGlWPL6d9tp8JV/pub"
//        val url = "http://docs.google.com/gview?embedded=true&url=$pdfUrl"
        binding.termsServiceWebView.loadUrl(pdfUrl)
    }
}