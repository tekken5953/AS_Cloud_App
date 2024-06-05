package app.airsignal.weather.utils

import android.app.Activity
import android.view.View
import android.view.WindowManager

class ScreenController(private val activity: Activity) {
    // 화면 터치를 허용/거부 합니다
    fun blockTouch(b: Boolean) {
        if (b) activity.window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        else activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    // 몰입모드로 전환됩니다
    @Suppress("DEPRECATION")
    fun fullMode() {
        activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    // 몰입모드에서 시스템 네비게이션바만 보여줍니다
    @Suppress("DEPRECATION")
    fun basicMode() {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}