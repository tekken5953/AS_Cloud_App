package app.airsignal.weather.view.aseye.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewEyeSingleBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오전 10:57
 **/
@SuppressLint("Recycle", "CustomViewStyleable")
class EyeLiveSingleDataView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var systemBinding: CustomViewEyeSingleBinding

    init {
        val inflater = LayoutInflater.from(context)
        systemBinding = CustomViewEyeSingleBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.EyeLiveSingleDataView)
            val title = typedArray.getString(R.styleable.EyeLiveSingleDataView_title)
            val unit = typedArray.getString(R.styleable.EyeLiveSingleDataView_unit)

            systemBinding.customEyeSingleTitle.text = title
            systemBinding.customEyeSingleUnit.text = unit

            typedArray.recycle()
        }
    }

    fun fetchData(value: String): EyeLiveSingleDataView {
        systemBinding.customEyeSingleValue.text = value
        return this
    }
}