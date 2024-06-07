package app.airsignal.weather.view.activity

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.databinding.ActivityWebUrlBinding
import app.airsignal.weather.utils.DataTypeParser
import app.airsignal.weather.view.dialog.WebViewSetting

class WebURLActivity : BaseActivity<ActivityWebUrlBinding>() {
    override val resID: Int get() = R.layout.activity_web_url

    private val blankURL = "about:blank"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        DataTypeParser.setStatusBar(this)

        val webView = binding.webUrlWebView

        window.statusBarColor = getColor(R.color.theme_view_color)

        @Suppress("DEPRECATION")
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

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

        if (intent.extras?.getBoolean("appBar") == true) binding.webUrlLinear.visibility = View.VISIBLE
        else binding.webUrlLinear.visibility = View.GONE

        val sort = intent.extras?.getString("sort")

        val (webUrlTitleText, url) = when (sort) {
            "as-eye" -> "AS-EYE" to blankURL
            "termsOfService" -> getString(R.string.term_of_services) to IgnoredKeyFile.termsOfServiceURL
            "dataUsage" -> getString(R.string.data_usages) to IgnoredKeyFile.privacyPolicyURI
            "inAppLink" -> "공지사항" to intent.extras?.getString("redirect",null)
            else -> "" to blankURL
        }

        binding.webUrlTitle.text = webUrlTitleText

        url?.let {webView.loadUrl(it)} ?: run {webView.loadUrl(blankURL)} // 페이지 로딩

    }
}