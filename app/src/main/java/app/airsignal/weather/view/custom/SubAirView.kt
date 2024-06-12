package app.airsignal.weather.view.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewSubAirBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-18 오전 9:11
 **/
class SubAirView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private var subAirBinding: CustomViewSubAirBinding

    init {
        val inflater = LayoutInflater.from(context)
        subAirBinding = CustomViewSubAirBinding.inflate(inflater, this, true)
    }

    // 타이틀 반환
    fun getTitle(): TextView = subAirBinding.customSubAirTitle
    // 값 데이터 반환
    fun getValue(): TextView = subAirBinding.customSubAirValue

    // 방위에 따른 아이콘 적용
    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun applyVector(vector: String?) {
        vector?.let { v ->
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(BELOW, R.id.customSubAirImg)
                addRule(CENTER_IN_PARENT)
            }
            subAirBinding.customSubAirValue.layoutParams = params
            subAirBinding.customSubAirValue.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawable(when(v) {
                    "북" -> R.drawable.ico_wind_n
                    "북북동" -> R.drawable.ico_wind_nne
                    "북동" -> R.drawable.ico_wind_ne
                    "동북동" -> R.drawable.ico_wind_ene
                    "동" -> R.drawable.ico_wind_e
                    "동남동" -> R.drawable.ico_wind_ese
                    "남동" -> R.drawable.ico_wind_se
                    "남남동" -> R.drawable.ico_wind_sse
                    "남" -> R.drawable.ico_wind_s
                    "남남서" -> R.drawable.ico_wind_ssw
                    "남서" -> R.drawable.ico_wind_sw
                    "서남서" -> R.drawable.ico_wind_wsw
                    "서" -> R.drawable.ico_wind_w
                    "서북서" -> R.drawable.ico_wind_wnw
                    "북서" -> R.drawable.ico_wind_nw
                    "북북서" -> R.drawable.ico_wind_nnw
                    else -> null
                }),null,null,null
            )
        }
    }

    // 이미지 적용
    private fun drawable(int: Int?): Drawable? =
        int?.let {ResourcesCompat.getDrawable(resources, int, null)}

    fun fetchData(value: String, img: Int, vector: String?): SubAirView {
        subAirBinding.customSubAirValue.text = value
        subAirBinding.customSubAirImg.setImageDrawable(ResourcesCompat.getDrawable(resources,img,null))
        if (vector != null) applyVector(vector = vector)

        return this
    }
}