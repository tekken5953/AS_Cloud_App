package com.example.airsignal_app.util

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

class MakeVibrator {
    private lateinit var vib: Vibrator

    fun init(context: Context) : MakeVibrator {
        vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        return this
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun make(time: Long) {
        vib.vibrate(
            VibrationEffect.createOneShot(
                time,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}