package com.example.airsignal_app.util

import android.annotation.TargetApi
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class MakeVibrator {
    private lateinit var vib: Vibrator

    /** 진동 클래스 초기설정 - 메서드 체이닝 형태임**/
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

    /** 진동 발생 메서드 **/
    @TargetApi(Build.VERSION_CODES.O)
    fun make(time: Long) {
        vib.vibrate(
            VibrationEffect.createOneShot(
                time,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}