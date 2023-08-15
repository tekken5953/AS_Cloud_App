package com.example.airsignal_app.util

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrateUtil(private val context: Context) {
    private val vib by lazy {
        @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
    }

    /** 진동 발생 메서드 **/
    fun make(duration: Int) {
        vib.vibrate(
            VibrationEffect.createOneShot(
                duration.toLong(), VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    fun noti(array: LongArray) {
        vib.vibrate(
            VibrationEffect.createWaveform(array, -1)
        )
    }
}