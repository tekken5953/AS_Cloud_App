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
                sweepAngle = 180
                arcColor = Color.parseColor("#E1E1E1")

                if (GetAppInfo.getIsNight(currentSun)) {
                    isClockwise = false
                    startAngle = 180
                    arcRotation = 90
                    progressColor = Color.parseColor("#7E5DFF")


                    val animatorSun =
                        ObjectAnimator.ofInt(seekArc, "progress", currentSun - 100)
                    animatorSun.duration = 800
                    animatorSun.start()
                } else {
                    isClockwise = true
                    startAngle = 90
                    arcRotation = 180
                    progressColor = Color.parseColor("#FF8A48")

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