package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile.privacyPolicyURI
import app.airsignal.weather.dao.IgnoredKeyFile.termsOfServiceURL
import app.airsignal.weather.databinding.ActivityWebUrlBinding
import app.airsignal.weather.util.`object`.SetSystemInfo

class WebURLActivity : BaseActivity<ActivityWebUrlBinding>() {
    override val resID: Int get() = R.layout.activity_web_url

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        SetSystemInfo.setStatusBar(this)

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

        binding.webUrlBackIv.setOnClickListener { finish() }

        // 웹뷰 세팅
        val webSettings = binding.webUrlWebView.settings
        webSettings.apply {
            javaScriptEnabled = true // 자바스크립트 허용
            builtInZoomControls = true // 줌 컨트롤러 생성
            setSupportZoom(true) // 핀치 줌 허용
            loadWithOverviewMode = true // 메타태그 허용
            useWideViewPort = true // 화면 맞추기
            domStorageEnabled = true // 로컬 저장소 허용
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // 브라우저 캐시 허용
        }

        binding.webUrlWebView.apply {
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }

        if (intent.extras!!.getBoolean("appBar")) {
            binding.webUrlLinear.visibility = View.VISIBLE
        } else {
            binding.webUrlLinear.visibility = View.GONE
        }

        val url: String
        when(intent.extras!!.getString("sort")) {
            "termsOfService" -> {
                binding.webUrlTitle.text = getString(R.string.term_of_services)
                url = termsOfServiceURL
            }
            "dataUsage" -> {
                binding.webUrlTitle.text = getString(R.string.data_usages)
                url = privacyPolicyURI
            }
            else -> {
                binding.webUrlTitle.text = ""
                url = "about:blank"
            }
        }

        binding.webUrlWebView.clearCache(true)
        binding.webUrlWebView.loadUrl(url) // 페이지 로딩
    }
}