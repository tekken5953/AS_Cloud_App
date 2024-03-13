package app.airsignal.weather.as_eye.customview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
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
            val isToggle = typedArray.getBoolean(R.styleable.EyeSettingView_isToggle, false)
            typedArray.recycle()

            systemBinding.customEyeSettingTitle.text = leftText

            if (isArrow) systemBinding.customEyeSettingArrow.visibility = View.VISIBLE else View.GONE
            if (isToggle) systemBinding.customEyeSettingSwitch.visibility = View.VISIBLE else View.GONE
        }
    }

    fun fetchData(value: String): EyeSettingView {
        systemBinding.customEyeSettingValue.text = value
        return this
    }

    fun fetchToggle(b: Boolean): SwitchCompat {
        systemBinding.customEyeSettingSwitch.isChecked = b
        return systemBinding.customEyeSettingSwitch
    }

    fun fetchEnable(b: Boolean): EyeSettingView {
        systemBinding.customEyeSettingTitle.setTextColor(context.getColor(if (b) R.color.main_black else R.color.eye_btn_disable_color))
        systemBinding.customEyeSettingValue.setTextColor(context.getColor(if (b) R.color.main_black else R.color.eye_btn_disable_color))
        systemBinding.customEyeSettingArrow.imageTintList =
            ColorStateList.valueOf(context.getColor(if (b) R.color.main_black else R.color.eye_btn_disable_color))

        return this
    }

    fun fetchTitleColor(color: Int): EyeSettingView {
        systemBinding.customEyeSettingTitle.setTextColor(context.getColor(color))
        return this
    }
}