package com.example.airsignal_app.view.custom_view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewMainAirBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오후 1:46
 **/
class AirQView(context: Context, attrs: AttributeSet?)
    : RelativeLayout(context, attrs) {
    private var airBinding: CustomViewMainAirBinding

    init {
        val inflater = LayoutInflater.from(context)
        airBinding = CustomViewMainAirBinding.inflate(inflater, this, true)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.AirQView)
            typedArray.recycle()
        }

        airBinding.airQCancel.setOnClickListener {
            val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            this.apply {
                startAnimation(fadeOut)
                alpha = 0f
            }
        }
    }

    fun fetchData(explain: String, graph: Drawable, nameEN: String, nameKR: String): AirQView {
        airBinding.airQExplainText.text = explain
        airBinding.airQName.text = nameEN
        airBinding.airQNameKR.text = nameKR
        airBinding.airQGraphIv.setImageDrawable(graph)
        return this
    }
}