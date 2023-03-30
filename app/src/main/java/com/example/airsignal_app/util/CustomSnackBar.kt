package com.example.airsignal_app.util

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
import com.google.android.material.snackbar.Snackbar

/**
 * @author : Lee Jae Young
 * @since : 2023-03-30 오후 1:22
 **/
class CustomSnackBar(view: View, private val message: String, private val drawable: Drawable) {
    //https://velog.io/@nagosooo/custom-Snackbar

    /**Constructor**/
    companion object {
        fun make(view: View, message: String, drawable: Drawable) =
            CustomSnackBar(view, message, drawable)
    }

    private val context = view.context
    private val snackBar = Snackbar.make(view, "", 2000)
    private val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout

    private val inflater = LayoutInflater.from(context)
    private val snackBarBinding =
        DataBindingUtil.inflate(inflater, R.layout.custom_view_snackbar, null, false)
                as CustomViewSnackbarBinding

    init {
        initView()
        initData()
    }

    /** 뷰 세팅 **/
    private fun initView() {
        with(snackBarLayout) {
            val layoutParams = layoutParams as FrameLayout.LayoutParams

            // 애니메이션 추가
            // 정석은 snackBar을 extend한 class 에서 contentViewCallback 을 커스텀 해주는거 같음
            // 그러나 나는 야매로 snackbarLayout에 animation을 줬다.
            val snackBarShowAnim = AnimationUtils.loadAnimation(context, R.anim.snackbar_fade_in)
            val snackBarHideAnim = AnimationUtils.loadAnimation(context, R.anim.snackbar_fade_out)
            this.startAnimation(snackBarShowAnim) // 👈 시작할때 애니메이션

            Handler(Looper.getMainLooper()).postDelayed({
                this.startAnimation(snackBarHideAnim)
            }, 2000L) // 핸들러로 메인 쓰레드 2초 잠재우고, 스낵바 hide 하는 애니메이션 실행

            layoutParams.gravity = Gravity.BOTTOM// gravity 설정
            removeAllViews()
            setPadding(0, 0, 0, 30) // padding 설정. 위에서 16만큼 떨어져있게.
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            addView(snackBarBinding.root, 0)
        }

    }

    /** 스낵바 텍스트, 이미지 할당**/
    private fun initData() {
        snackBarBinding.tvSample.text = message
        snackBarBinding.image.background = drawable
    }

    /** 진동, 스낵바 Show**/
    fun show() {
        MakeVibrator().init(context).make(100)
        snackBar.show()
    }
}