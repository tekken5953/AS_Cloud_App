package com.example.airsignal_app.view.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewMainAirBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오후 1:46
 **/
class AirQView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var airBinding: CustomViewMainAirBinding

    init {
        val inflater = LayoutInflater.from(context)
        airBinding = CustomViewMainAirBinding.inflate(inflater, this, true)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.AirQView)
            val customSort = typedArray.getString(R.styleable.AirQView_customSort)
            val customUnit = typedArray.getString(R.styleable.AirQView_customUnit)
            typedArray.recycle()

            airBinding.customAirQTitle.text = customSort
            airBinding.customAirQUnit.text = customUnit
        }
    }

    fun fetchData(value: String?, color: Int): AirQView {
        airBinding.customAirQValue.text = value
        airBinding.customAirQValue.setTextColor(color)
        return this
    }
}