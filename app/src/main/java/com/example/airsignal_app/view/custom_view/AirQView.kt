package com.example.airsignal_app.view.custom_view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewMainAirBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오후 1:46
 **/
class AirQView(context: Context, attrs: AttributeSet?)
    : RelativeLayout(context, attrs) {
    private var airBinding: CustomViewMainAirBinding

    init {
        val inflater = LayoutInflater.from(context)
        airBinding = CustomViewMainAirBinding.inflate(inflater, this, true)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.AirQView)
            typedArray.recycle()
        }

        airBinding.airQCancel.setOnClickListener {
            val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            this.apply {
                startAnimation(fadeOut)
                alpha = 0f
            }
        }
    }

    fun modifyDataSort(context: Context, krName: String): String {
        return when(krName) {
            "초미세먼지" -> context.getString(R.string.airq_question_pm2p5)
            "미세먼지" -> context.getString(R.string.airq_question_pm10)
            "오존" -> context.getString(R.string.airq_question_o3)
            "이산화질소" -> context.getString(R.string.airq_question_no2)
            "아황산가스" -> context.getString(R.string.airq_question_so2)
            "일산화탄소" -> context.getString(R.string.airq_question_co)
            else -> ""
        }
    }

    fun modifyDataGraph(context: Context, krName: String): Drawable? {
        return when(krName) {
            "초미세먼지" -> ResourcesCompat.getDrawable(context.resources,R.drawable.graph_pm25,null)
            "미세먼지" -> ResourcesCompat.getDrawable(context.resources,R.drawable.graph_pm10,null)
            "오존" -> ResourcesCompat.getDrawable(context.resources,R.drawable.graph_o3,null)
            "이산화질소" -> ResourcesCompat.getDrawable(context.resources,R.drawable.graph_no2,null)
            "아황산가스" -> ResourcesCompat.getDrawable(context.resources,R.drawable.graph_so2,null)
            "일산화탄소" -> ResourcesCompat.getDrawable(context.resources,R.drawable.graph_co,null)
            else -> null
        }
    }

    fun fetchData(explain: String, graph: Drawable, nameEN: String, nameKR: String): AirQView {
        airBinding.airQExplainText.text = explain
        airBinding.airQName.text = "${nameEN}(${nameKR})"
        airBinding.airQGraphIv.setImageDrawable(graph)
        return this
    }
}