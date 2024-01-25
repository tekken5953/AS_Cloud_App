package app.airsignal.weather.view.dialog

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView

class WebViewSetting {
    @SuppressLint("SetJavaScriptEnabled")
    fun apply(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true // 자바스크립트 허용
            builtInZoomControls = false // 줌 컨트롤러 생성
            setSupportZoom(false) // 핀치 줌 허용
            loadWithOverviewMode = true // 메타태그 허용
            useWideViewPort = true // 화면 맞추기
            domStorageEnabled = true // 로컬 저장소 허용
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // 브라우저 캐시 허용
        }
    }
}