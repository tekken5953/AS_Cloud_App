package app.airsignal.weather.util

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
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
                this.arcColor = Color.parseColor("#40FF8A48")
                this.startAngle = 90
                this.arcRotation = 180
                this.progressColor = Color.parseColor("#FF8A48")
            }
            val animatorSun = ObjectAnimator.ofInt(seekArc, "progress", currentSun)
            animatorSun.duration = 800
            animatorSun.start()
        }
    }

    /** 일출/일몰 그래프 터치 막기 **/
    fun disableTouch() = seekArc.setOnTouchListener { _, _ -> true }
}