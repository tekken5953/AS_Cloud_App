package com.example.airsignal_app.util

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.triggertrap.seekarc.SeekArc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SunProgress(private val seekArc: SeekArc) {

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

    @SuppressLint("ClickableViewAccessibility")
    fun disableTouch() {
        seekArc.setOnTouchListener { _, _ -> true } // 자외선 그래프 클릭 방지
    }
}