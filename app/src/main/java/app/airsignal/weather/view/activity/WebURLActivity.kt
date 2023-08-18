package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import app.airsignal.weather.R
import app.airsignal.weather.databinding.ActivityWebUrlBinding

class WebURLActivity : BaseActivity<ActivityWebUrlBinding>() {
    override val resID: Int get() = R.layout.activity_web_url

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

        binding.webUrlBackIv.setOnClickListener {
            finish()
        }

        // 웹뷰 세팅
        val webSettings = binding.webUrlWebView.settings
        webSettings.apply {
            javaScriptEnabled = true // 자바스크립트 허용
            builtInZoomControls = true
            setSupportZoom(true) // 핀치 줌 허용
            loadWithOverviewMode = true // 메타태그 허용
            useWideViewPort = true // 화면 맞추기
            domStorageEnabled = true // 로컬 저장소 허용
            cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용
        }

        binding.webUrlWebView.apply {
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }

        val pdfUrl: String
        when(intent.extras!!.getString("sort")) {
            "termsOfService" -> {
                binding.webUrlTitle.text = getString(R.string.term_of_services)
                pdfUrl = "https://docs.google.com/document/d/e/2PACX-1vTmaf0Wg9zhNZfe-_S-4eWDj1XLwbYlUcoONys3MzzTEAx-_QLlJOuTGo7uQjl5FbyGlWPL6d9tp8JV/pub"
            }
            "dataUsage" -> {
                binding.webUrlTitle.text = getString(R.string.data_usages)
                pdfUrl = "file:///android_asset/airsignal_user_data_usage.html"
            }
            else -> {
                binding.webUrlTitle.text = ""
                pdfUrl = "about:blank"
            }

        }
        
        binding.webUrlWebView.loadUrl(pdfUrl) // 웹 페이지 로딩
    }
}