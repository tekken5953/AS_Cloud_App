package app.airsignal.weather.view.aseye.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewEyeSettingBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오전 10:57
 **/
@SuppressLint("Recycle", "CustomViewStyleable")
class EyeSettingView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var systemBinding: CustomViewEyeSettingBinding

    init {
        val inflater = LayoutInflater.from(context)
        systemBinding = CustomViewEyeSettingBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.EyeSettingView)
            val leftText = typedArray.getString(R.styleable.EyeSettingView_settingLeft)
            val isArrow = typedArray.getBoolean(R.styleable.EyeSettingView_isArrow,false)
            typedArray.recycle()

            systemBinding.customEyeSettingTitle.text = leftText

            if (isArrow) systemBinding.customEyeSettingArrow.visibility = View.VISIBLE else View.GONE
        }
    }

    fun fetchData(value: String): EyeSettingView {
        systemBinding.customEyeSettingValue.text = value
        return this
    }
}