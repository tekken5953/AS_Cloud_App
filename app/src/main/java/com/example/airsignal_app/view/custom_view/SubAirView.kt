package com.example.airsignal_app.view.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewSubAirBinding
import org.w3c.dom.Text

/**
 * @author : Lee Jae Young
 * @since : 2023-07-18 오전 9:11
 **/
class SubAirView(context: Context, attrs: AttributeSet?)
    : RelativeLayout(context, attrs) {
    private var subAirBinding: CustomViewSubAirBinding

    init {
        val inflater = LayoutInflater.from(context)
        subAirBinding = CustomViewSubAirBinding.inflate(inflater, this, true)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SubAirView)
            typedArray.recycle()

        }
    }

    // 타이틀 반환
    fun getTitle(): TextView {
        return subAirBinding.customSubAirTitle
    }

    // 값 데이터 반환
    fun getValue(): TextView {
        return subAirBinding.customSubAirValue
    }

    // 방위에 따른 아이콘 적용
    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun applyVector(vector: String?) {
        vector?.let { v ->
            subAirBinding.customSubAirValue.setPadding(0,0,15,0)
            subAirBinding.customSubAirValue.setCompoundDrawablesRelativeWithIntrinsicBounds(
                when(v) {
                    "북" -> { drawable(R.drawable.ico_wind_n) }
                    "북북동" -> { drawable(R.drawable.ico_wind_nne) }
                    "북동" -> { drawable(R.drawable.ico_wind_ne) }
                    "동북동" -> { drawable(R.drawable.ico_wind_ene) }
                    "동" -> { drawable(R.drawable.ico_wind_e) }
                    "동남동" -> { drawable(R.drawable.ico_wind_ese) }
                    "남동" -> { drawable(R.drawable.ico_wind_se) }
                    "남남동" -> { drawable(R.drawable.ico_wind_sse) }
                    "남" -> { drawable(R.drawable.ico_wind_s) }
                    "남남서" -> { drawable(R.drawable.ico_wind_ssw) }
                    "남서" -> { drawable(R.drawable.ico_wind_sw) }
                    "서남서" -> { drawable(R.drawable.ico_wind_wsw) }
                    "서" -> { drawable(R.drawable.ico_wind_w) }
                    "서북서" -> { drawable(R.drawable.ico_wind_wnw) }
                    "북서" -> { drawable(R.drawable.ico_wind_nw) }
                    "북북서" -> { drawable(R.drawable.ico_wind_nnw) }
                    else -> { null }
                },null,null,null
            )
        }
    }

    // 이미지 적용
    private fun drawable(int: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, int, null)
    }

    fun fetchData(value: String, img: Int,vector: String?): SubAirView {
        subAirBinding.customSubAirValue.text = value
        subAirBinding.customSubAirImg.setImageDrawable(
            ResourcesCompat.getDrawable(resources,img,null))
        applyVector(vector = vector)

        return this
    }
}