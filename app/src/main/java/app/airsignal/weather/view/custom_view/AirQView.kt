package app.airsignal.weather.view.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewMainAirBinding

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

        // 취소 버튼 클릭 시 다이얼로그 사라짐
        airBinding.airQCancel.setOnClickListener {
            val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            this.apply {
                startAnimation(fadeOut)
                alpha = 0f
            }
        }
    }

    // 외부 공기질 목록에 따른 내용 반환
    fun modifyDataSort(context: Context, krName: String): String {
        return when(krName) {
            context.getString(R.string.pm2_5_full) -> context.getString(R.string.airq_question_pm2p5)
            context.getString(R.string.pm10_full) -> context.getString(R.string.airq_question_pm10)
            context.getString(R.string.o3_full) -> context.getString(R.string.airq_question_o3)
            context.getString(R.string.no2_full) -> context.getString(R.string.airq_question_no2)
            context.getString(R.string.so2_full) -> context.getString(R.string.airq_question_so2)
            context.getString(R.string.co_full) -> context.getString(R.string.airq_question_co)
            else -> ""
        }
    }

    // 외부 공기질 목록에 따른 그래프 반환
    fun modifyDataGraph(context: Context, krName: String): Drawable? {
        return when(krName) {
            context.getString(R.string.pm2_5_full) -> {
                ResourcesCompat.getDrawable(context.resources,R.drawable.graph_pm25,null)
            }
            context.getString(R.string.pm10_full) -> {
                ResourcesCompat.getDrawable(context.resources,R.drawable.graph_pm10,null)
            }
            context.getString(R.string.o3_full) -> {
                ResourcesCompat.getDrawable(context.resources,R.drawable.graph_03,null)
            }
            context.getString(R.string.no2_full) -> {
                ResourcesCompat.getDrawable(context.resources,R.drawable.graph_no2,null)
            }
            context.getString(R.string.so2_full) ->  {
                ResourcesCompat.getDrawable(context.resources,R.drawable.graph_so2,null)
            }
            context.getString(R.string.co_full)-> {
                ResourcesCompat.getDrawable(context.resources,R.drawable.graph_co,null)
            }
            else -> null
        }
    }

    // 데이터 적용
    @SuppressLint("SetTextI18n")
    fun fetchData(explain: String, graph: Drawable, nameEN: String, nameKR: String): AirQView {
        airBinding.airQExplainText.text = explain
        airBinding.airQName.text = "${nameEN}(${nameKR})"
        airBinding.airQGraphIv.setImageDrawable(graph)
        return this
    }
}