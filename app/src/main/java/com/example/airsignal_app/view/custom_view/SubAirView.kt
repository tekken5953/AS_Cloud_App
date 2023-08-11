package com.example.airsignal_app.view.custom_view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewSubAirBinding

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
            val title = typedArray.getString(R.styleable.SubAirView_customTitle)
            val isLeftLine = typedArray.getBoolean(R.styleable.SubAirView_isLeftLine, false)
            typedArray.recycle()

            subAirBinding.customSubAirTitle.text = title

            if (isLeftLine) {
                subAirBinding.customSubAirLeftLine.visibility = View.VISIBLE
            } else {
                subAirBinding.customSubAirLeftLine.visibility = View.GONE
            }
        }
    }

    private fun applyVector(vector: String?) {
        vector?.let { v ->
            subAirBinding.customSubAirValue.setCompoundDrawablesRelativeWithIntrinsicBounds(
                when(v) {
                    "북" -> { drawable(R.drawable.ico_wind_n) }
                    "북북동" -> { drawable(R.drawable.ico_wind_nne) }
                    "북동" -> { drawable(R.drawable.ico_wind_ne) }
                    "동북동" -> { drawable(R.drawable.ico_wind_nne) }
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