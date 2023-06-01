package com.example.airsignal_app.view

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewSnackbarBinding
import com.example.airsignal_app.util.MakeVibrator
import com.google.android.material.snackbar.Snackbar

/**
 * @author : Lee Jae Young
 * @since : 2023-03-30 오후 1:22
 **/
class SnackBarUtils(view: View, private val message: String, private val drawable: Drawable) {

    /**Constructor**/
    companion object {
        @JvmStatic
        fun make(view: View, message: String, drawable: Drawable) =
            SnackBarUtils(view, message, drawable)
    }

    private val context = view.context
    private val snackBar = Snackbar.make(view, "", 2000)
    private val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout
    private val inflater = LayoutInflater.from(context)
    private val snackBarBinding =
        DataBindingUtil.inflate(inflater, R.layout.custom_view_snackbar, null, false)
                as CustomViewSnackbarBinding

    /** 뷰 세팅 **/
    private fun initView() : SnackBarUtils {
        with(snackBarLayout) {
            val layoutParams = layoutParams as FrameLayout.LayoutParams
            val snackBarShowAnim = AnimationUtils.loadAnimation(context, R.anim.snackbar_fade_in)
            val snackBarHideAnim = AnimationUtils.loadAnimation(context, R.anim.snackbar_fade_out)
            this.startAnimation(snackBarShowAnim) // 시작할때 애니메이션

            Handler(Looper.getMainLooper()).postDelayed({
                this.startAnimation(snackBarHideAnim)
            }, 2000L) // 핸들러로 메인 쓰레드 2초 잠재우고, 스낵바 hide 하는 애니메이션 실행

            layoutParams.gravity = Gravity.BOTTOM // gravity
            removeAllViews()
            setPadding(0, 0, 0, 30) // set padding
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            addView(snackBarBinding.root, 0)
        }
        return this
    }

    /** 스낵바 텍스트, 이미지 할당**/
    private fun initData() {
        snackBarBinding.tvSample.text = message
        snackBarBinding.image.background = drawable
    }

    /** 진동, 스낵바 Show**/
    fun show() {
        initView().initData()
        vibrate()
        snackBar.show()
    }

    /** 진동 발생 **/
    private fun vibrate() {
        MakeVibrator().init(context).make(100)
    }
}