package app.airsignal.weather.view.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewSettingSystemBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오전 10:57
 **/
class SettingSystemView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var systemBinding: CustomViewSettingSystemBinding

    init {
        val inflater = LayoutInflater.from(context)
        systemBinding = CustomViewSettingSystemBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.SettingSystemView)
            val customText = typedArray.getString(R.styleable.SettingSystemView_left)
            typedArray.recycle()

            systemBinding.customSettingLeft.text = customText
        }
    }

    fun fetchData(right: String): SettingSystemView {
        systemBinding.customSettingSystemRight.text = right
        return this
    }
}