package app.airsignal.weather.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class VibrateUtil(private val context: Context) {
    private val vib by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /** 기본 진동 발생 메서드 **/
    fun make(duration: Long) {
        CoroutineScope(Dispatchers.Default).launch {
            vib.vibrate(
                VibrationEffect.createOneShot(
                    duration, VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    /** 알림 진동 발생 **/
    fun noti(array: LongArray) {
        CoroutineScope(Dispatchers.Default).launch {
            vib.vibrate(VibrationEffect.createWaveform(array, -1))
        }
    }
}