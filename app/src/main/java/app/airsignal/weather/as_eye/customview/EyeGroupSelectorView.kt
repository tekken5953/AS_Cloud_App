package app.airsignal.weather.as_eye.customview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewEyeDoubleBinding
import app.airsignal.weather.databinding.CustomViewEyeGroupSelectorBinding


@SuppressLint("Recycle", "CustomViewStyleable")
class EyeGroupSelectorView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var selectorBinding: CustomViewEyeGroupSelectorBinding

    init {
        val inflater = LayoutInflater.from(context)
        selectorBinding = CustomViewEyeGroupSelectorBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.EyeGroupSelectorView)
            val title = typedArray.getString(R.styleable.EyeGroupSelectorView_selectorTitle)

            selectorBinding.customGroupSelectTv.text = title
            selectorBinding.customGroupSelectIv.setImageDrawable(getImage(title ?: ""))

            typedArray.recycle()
        }
    }

    private fun getImage(title: String): Drawable? {
        return when(title) {
            "그룹 추가" -> ResourcesCompat.getDrawable(resources, R.drawable.add, null)
            "그룹 삭제" -> ResourcesCompat.getDrawable(resources, R.drawable.delete, null)
            "그룹명 변경" -> ResourcesCompat.getDrawable(resources, R.drawable.edit, null)
            else -> null
        }
    }

    fun fetchColor(isEnable: Boolean) {
        this.isEnabled = isEnable
        if (isEnable) {
            selectorBinding.customGroupSelectTv.setTextColor(context.getColor(R.color.main_black))
            selectorBinding.customGroupSelectIv.imageTintList = ColorStateList.valueOf(context.getColor(R.color.main_black))
        } else {
            selectorBinding.customGroupSelectTv.setTextColor(context.getColor(R.color.eye_btn_disable_color))
            selectorBinding.customGroupSelectIv.imageTintList = ColorStateList.valueOf(context.getColor(R.color.eye_btn_disable_color))
        }
    }
}