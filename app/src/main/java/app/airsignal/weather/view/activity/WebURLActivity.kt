package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile.privacyPolicyURI
import app.airsignal.weather.dao.IgnoredKeyFile.termsOfServiceURL
import app.airsignal.weather.databinding.ActivityWebUrlBinding
import app.airsignal.weather.util.`object`.DataTypeParser.setStatusBar
import app.airsignal.weather.view.dialog.WebViewSetting

class WebURLActivity : BaseActivity<ActivityWebUrlBinding>() {
    override val resID: Int get() = R.layout.activity_web_url

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        setStatusBar(this)

        val webView = binding.webUrlWebView

        window.statusBarColor = getColor(R.color.theme_view_color)

        @Suppress("DEPRECATION")
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        binding.webUrlBackIv.setOnClickListener {
            if (!webView.canGoBack()) finish() else webView.goBack()
        }

        binding.webUrlTop.setOnClickListener { webView.pageUp(true) }

        // 웹뷰 세팅
        WebViewSetting().apply(webView)

        binding.webUrlWebView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.webUrlTop.visibility = if (scrollY == 0) View.GONE else View.VISIBLE
        }

        webView.apply {
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }

        if (intent.extras!!.getBoolean("appBar")) binding.webUrlLinear.visibility = View.VISIBLE
        else binding.webUrlLinear.visibility = View.GONE

        val sort = intent.extras?.getString("sort")

        val (webUrlTitleText,url) = when (sort) {
            "as-eye" -> "AS-EYE" to "about:blank"
            "termsOfService" -> getString(R.string.term_of_services) to termsOfServiceURL
            "dataUsage" -> getString(R.string.data_usages) to privacyPolicyURI
            "inAppLink" -> "공지사항" to intent.extras!!.getString("redirect",null)
            else -> "" to "about:blank"
        }

        binding.webUrlTitle.text = webUrlTitleText

        webView.clearCache(true)
        webView.loadUrl(url) // 페이지 로딩
    }
}