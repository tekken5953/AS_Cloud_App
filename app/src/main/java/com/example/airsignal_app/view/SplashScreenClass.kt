package com.example.airsignal_app.view

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.animation.doOnEnd
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.ConvertDataType
import kotlin.concurrent.thread

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 5:27
 **/
class SplashScreenClass (
    private val mContext: Activity
) {
    private var isReady = false
    private val sp by lazy { SharedPreferenceManager(mContext) }

    fun setInitialSetting() : SplashScreenClass {
        // 설정된 테마 정보 불러오기
        when(sp.getString("theme")) {
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        // 설정된 언어정보 불러오기
        when(sp.getString("lang")) {
            "korean" -> {
                ConvertDataType.setLocaleToKorea(mContext)
            }
            "english" -> {
                ConvertDataType.setLocaleToEnglish(mContext)
            }
            else -> {
                ConvertDataType.setLocaleToSystem(mContext)
            }
        }
        return this
    }

    fun setContentView(contentView: View) {
        thread(start=true) {
            for (i in 1..1) {
                Thread.sleep(1000)
            }
            isReady = true
        }

        // Set up an OnPreDrawListener to the root view.
        contentView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check if the initial data is ready.
                    return if (isReady) {
                        // The content is ready; start drawing.
                        contentView.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready; suspend.
                        false
                    }
                }
            }
        )

        // Add a callback that's called when the splash screen is animating to
        // the app content.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mContext.splashScreen.setOnExitAnimationListener { splashScreenView ->
                // Create your custom animation.
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.height.toFloat()
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 600L

                // Call SplashScreenView.remove at the end of your custom animation.
                slideUp.doOnEnd { splashScreenView.remove() }

                // Run your animation.
                slideUp.start()
            }
        }
    }
}