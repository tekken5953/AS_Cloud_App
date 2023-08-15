package com.example.airsignal_app.view.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewCustomerItemBinding

@SuppressLint("Recycle", "CustomViewStyleable")
class CustomerServiceView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var customerBinding: CustomViewCustomerItemBinding

    init {
        val inflater = LayoutInflater.from(context)
        customerBinding = CustomViewCustomerItemBinding.inflate(inflater, this, true)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomerServiceView)
            val title = typedArray.getString(R.styleable.CustomerServiceView_customerTitle)
            val value = typedArray.getString(R.styleable.CustomerServiceView_customerValue)
            typedArray.recycle()

            customerBinding.customCustomerTitle.text = title
            customerBinding.customCustomerValue.text = value
        }
    }

    fun fetchData(img: Int): CustomerServiceView {
        customerBinding.customCustomerImg.setImageDrawable(
            ResourcesCompat.getDrawable(resources, img, null)
        )
        return this
    }
}