package app.airsignal.weather.util

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import app.airsignal.weather.util.`object`.GetAppInfo
import com.triggertrap.seekarc.SeekArc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SunProgress(private val seekArc: SeekArc) {

    /** 일출/일몰 그래프 애니메이션 발동 **/
    fun animate(currentSun: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            seekArc.apply {
                this.sweepAngle = 180
                this.arcColor = Color.parseColor("#E1E1E1")

                if (GetAppInfo.getIsNight(currentSun)) {
                    this.isClockwise = false
                    this.startAngle = 180
                    this.arcRotation = 90
                    this.progressColor = Color.parseColor("#7E5DFF")


                    val animatorSun =
                        ObjectAnimator.ofInt(seekArc, "progress", currentSun - 100)
                    animatorSun.duration = 800
                    animatorSun.start()
                } else {
                    this.isClockwise = true
                    this.startAngle = 90
                    this. arcRotation = 180
                    this. progressColor = Color.parseColor("#FF8A48")

                    val animatorSun =
                        ObjectAnimator.ofInt(seekArc, "progress", currentSun)
                    animatorSun.duration = 800
                    animatorSun.start()
                }
            }
        }
    }

    /** 일출/일몰 그래프 터치 막기 **/
    @SuppressLint("ClickableViewAccessibility")
    fun disableTouch() {
        seekArc.setOnTouchListener { _, _ -> true } // 자외선 그래프 클릭 방지
    }
}