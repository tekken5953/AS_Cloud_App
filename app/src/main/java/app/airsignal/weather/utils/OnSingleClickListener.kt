package app.airsignal.weather.utils

import android.os.SystemClock
import android.view.View

abstract class OnSingleClickListener : View.OnClickListener {
    private var mLastClickTime: Long = 0
    private val minClickInterval: Long = 1000

    abstract fun onSingleClick(v: View?)

    override fun onClick(v: View) {
        val currentClickTime = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        mLastClickTime = currentClickTime
        // 중복클릭 아닌 경우
        if (elapsedTime > minClickInterval) onSingleClick(v)
//        else { ToastUtils(BaseApplication.appContext).showMessage("잠시 후에 시도해주세요") }
    }

    companion object {
        // 중복 클릭 방지 시간 설정 ( 해당 시간 이후에 다시 클릭 가능 )

    }
}