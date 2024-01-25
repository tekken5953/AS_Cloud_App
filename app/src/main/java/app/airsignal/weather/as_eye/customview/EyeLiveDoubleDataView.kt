package app.airsignal.weather.as_eye.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewEyeDoubleBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오전 10:57
 **/
@SuppressLint("Recycle", "CustomViewStyleable")
class EyeLiveDoubleDataView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var systemBinding: CustomViewEyeDoubleBinding

    init {
        val inflater = LayoutInflater.from(context)
        systemBinding = CustomViewEyeDoubleBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.EyeLiveDoubleDataView)
            val krTitle = typedArray.getString(R.styleable.EyeLiveDoubleDataView_titleKrDouble)
            val enTitle = typedArray.getString(R.styleable.EyeLiveDoubleDataView_titleEnDouble)
            val unit = typedArray.getString(R.styleable.EyeLiveDoubleDataView_unitDouble)

            systemBinding.customEyeDoubleKrTitle.text = krTitle
            systemBinding.customEyeDoubleEnTitle.text = enTitle
            systemBinding.customEyeDoubleUnit.text = unit

            typedArray.recycle()
        }
    }

    fun fetchData(value: String, bg: Int, smile: Int): EyeLiveDoubleDataView {
        systemBinding.customEyeDoubleValue.text = value
        systemBinding.customEyeDoubleSideView.setBackgroundResource(bg)
        systemBinding.customEyeDoubleSmile.setBackgroundResource(smile)
        return this
    }
}