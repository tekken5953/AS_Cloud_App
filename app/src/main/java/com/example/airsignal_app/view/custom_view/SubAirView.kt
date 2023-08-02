package com.example.airsignal_app.view.custom_view

import android.content.Context
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

    fun fetchData(value: String, img: Int): SubAirView {
        subAirBinding.customSubAirValue.text = value
        subAirBinding.customSubAirImg.setImageDrawable(
            ResourcesCompat.getDrawable(resources,img,null))
        return this
    }
}