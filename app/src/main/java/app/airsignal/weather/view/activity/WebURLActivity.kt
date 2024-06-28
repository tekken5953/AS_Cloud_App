package app.airsignal.weather.view.activity

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.databinding.ActivityWebUrlBinding
import app.airsignal.weather.utils.controller.ScreenController
import app.airsignal.weather.utils.view.WebViewSetting

class WebURLActivity : BaseActivity<ActivityWebUrlBinding>() {
    override val resID: Int get() = R.layout.activity_web_url

    private val blankURL = "about:blank"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        ScreenController(this).setStatusBar()

        val webView = binding.webUrlWebView

        window.statusBarColor = getColor(R.color.theme_view_color)

        ScreenController(this).changeSystemUiVisibility()

        binding.webUrlBackIv.setOnClickListener {
            if (webView.canGoBack()) webView.goBack() else finish()
        }

        binding.webUrlTop.setOnClickListener { webView.pageUp(true) }

        binding.webUrlWebView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.webUrlTop.visibility = if (scrollY == 0) View.GONE else View.VISIBLE
        }

        // 웹뷰 세팅
        WebViewSetting().apply(webView)

        webView.apply {
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }

        if (intent.extras?.getBoolean("appBar") == true)
            binding.webUrlLinear.visibility = View.VISIBLE
        else binding.webUrlLinear.visibility = View.GONE

        val (webUrlTitleText, url) = when (intent.extras?.getString("sort")) {
            "as-eye" -> "AS-EYE" to blankURL
            "termsOfService" -> getString(R.string.term_of_services) to IgnoredKeyFile.TERMS_OF_SERVICE_URL
            "dataUsage" -> getString(R.string.data_usages) to IgnoredKeyFile.PRIVACY_POLICY_URL
            "inAppLink" -> "공지사항" to intent.extras?.getString("redirect",null)
            else -> "" to blankURL
        }

        binding.webUrlTitle.text = webUrlTitleText

        url?.let {webView.loadUrl(it)} ?: run {webView.loadUrl(blankURL)} // 페이지 로딩
    }
}