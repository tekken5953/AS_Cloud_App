package app.airsignal.weather.util

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import app.airsignal.weather.util.`object`.GetAppInfo
import com.orhanobut.logger.Logger
import com.triggertrap.seekarc.SeekArc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("ClickableViewAccessibility")
class SunProgress(private val seekArc: SeekArc) {

    /** 일출/일몰 그래프 애니메이션 발동 **/
    fun animate(currentSun: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            seekArc.apply {
                this.sweepAngle = 180
                this.arcColor = Color.parseColor("#E1E1E1")
                val isNight = GetAppInfo.getIsNight(currentSun)
                this.isClockwise = !isNight
                this.startAngle = if (isNight) 180 else 90
                this.arcRotation = if (isNight) 90 else 180
                this.progressColor = Color.parseColor(if (isNight) "#7E5DFF" else "#FF8A48")
                val animatorSun =
                    ObjectAnimator.ofInt(
                        seekArc, "progress",
                        if (isNight) currentSun - 100 else currentSun
                    )
                animatorSun.duration = 800
                animatorSun.start()
            }
        }
    }

    /** 일출/일몰 그래프 터치 막기 **/
    fun disableTouch() {
        seekArc.setOnTouchListener { _, _ -> true } // 자외선 그래프 클릭 방지
    }
}