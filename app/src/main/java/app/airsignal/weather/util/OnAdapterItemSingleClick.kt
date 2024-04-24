package app.airsignal.weather.util

import android.os.SystemClock
import android.view.View

abstract class OnAdapterItemSingleClick : OnAdapterItemClick.OnAdapterItemClick {
    private var mLastClickTime: Long = 0

    abstract fun onSingleClick(v: View?, position: Int)

    override fun onItemClick(v: View, position: Int) {
        val currentClickTime = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        mLastClickTime = currentClickTime
        // 중복클릭 아닌 경우
        if (elapsedTime > MIN_CLICK_INTERVAL) { onSingleClick(v, position) }
//        else { ToastUtils(BaseApplication.appContext).showMessage("잠시 후에 시도해주세요") }
    }

    companion object {
        // 중복 클릭 방지 시간 설정 ( 해당 시간 이후에 다시 클릭 가능 )
        private const val MIN_CLICK_INTERVAL: Long = 1000
    }
}