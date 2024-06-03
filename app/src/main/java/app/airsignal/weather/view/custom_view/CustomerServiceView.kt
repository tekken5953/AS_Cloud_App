package app.airsignal.weather.view.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewCustomerItemBinding

class CustomerServiceView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var customerBinding: CustomViewCustomerItemBinding

    init {
        val inflater = LayoutInflater.from(context)
        customerBinding = CustomViewCustomerItemBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.CustomerServiceView)
            val title = typedArray.getString(R.styleable.CustomerServiceView_customerTitle)
            val value = typedArray.getString(R.styleable.CustomerServiceView_customerValue)
            typedArray.recycle()

            customerBinding.customCustomerTitle.text = title
            customerBinding.customCustomerValue.text = value
        }
    }

    fun fetchData(img: Int) =
        customerBinding.customCustomerImg.setImageDrawable(ResourcesCompat.getDrawable(resources, img, null))
}