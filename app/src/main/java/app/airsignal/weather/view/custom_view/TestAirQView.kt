package app.airsignal.weather.view.custom_view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import app.airsignal.weather.databinding.CustomViewAirqBinding
import app.airsignal.weather.util.OnSingleClickListener
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.`object`.DataTypeParser

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오후 1:46
 **/
class TestAirQView(context: Context, attrs: AttributeSet?)
    : RelativeLayout(context, attrs) {
    private var airBinding: CustomViewAirqBinding

    enum class AirQ(val title: String,val en: String) {
        PM2_5(title = "초미세먼지", "PM2.5"),
        PM10(title = "미세먼지", "PM10"),
        NO2(title = "이산화질소", "NO2"),
        SO2(title = "아황산가스", "SO2"),
        CO(title = "일산화탄소", "CO"),
        O3(title = "오존", "O3")
    }

    init {
        val inflater = LayoutInflater.from(context)
        airBinding = CustomViewAirqBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.TestAirQView)

            val title = typedArray.getString(R.styleable.TestAirQView_airTitle)
            val unit = typedArray.getString(R.styleable.TestAirQView_airUnit)

            airBinding.apply {
                listItemNestedAirTitle.text = title
                listItemNestedAirUnit.text = unit
            }

            typedArray.recycle()
        }
    }

    fun setOnClickListener(popupHelp: AirQView): TestAirQView {
        airBinding.listItemNestedAirHelp.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (popupHelp.alpha == 0f) {
                    val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                    fadeIn.duration = 400
                    // 팝업 다이얼로그 생성
                    popupHelp.apply {
                        bringToFront()
                        when(airBinding.listItemNestedAirTitle.text.toString()) {
                            AirQ.PM2_5.title -> {
                                popupHelp.fetchData(
                                modifyDataSort(context, AirQ.PM2_5.title),
                                modifyDataGraph(context, AirQ.PM2_5.title)!!,
                                AirQ.PM2_5.en, AirQ.PM2_5.title) }
                            AirQ.PM10.title -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, AirQ.PM10.title),
                                    modifyDataGraph(context, AirQ.PM10.title)!!,
                                    AirQ.PM10.en, AirQ.PM10.title) }
                            AirQ.NO2.title -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, AirQ.NO2.title),
                                    modifyDataGraph(context, AirQ.NO2.title)!!,
                                    AirQ.NO2.en, AirQ.NO2.title)}
                            AirQ.SO2.title -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, AirQ.SO2.title),
                                    modifyDataGraph(context, AirQ.SO2.title)!!,
                                    AirQ.SO2.en, AirQ.SO2.title) }
                            AirQ.O3.title -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, AirQ.O3.title),
                                    modifyDataGraph(context, AirQ.O3.title)!!,
                                    AirQ.O3.en, AirQ.O3.title) }
                            AirQ.CO.title -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, AirQ.CO.title),
                                    modifyDataGraph(context, AirQ.CO.title)!!,
                                    AirQ.CO.en, AirQ.CO.title) }
                        }
                        startAnimation(fadeIn)
                        alpha = 1f
                    }
                } else {
                    val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
                    popupHelp.apply {
                        startAnimation(fadeOut)
                        alpha = 0f
                    }
                }
            }
        })

        return this
    }

    // 데이터 적용
    fun fetchData(sort: AirQ, value: Double): TestAirQView {
        val moderate = getModerate(sort, value)
        val grade = moderate.first
        val progress = moderate.second
        airBinding.listItemNestedAirPb.progress = progress
        airBinding.listItemNestedAirValue.text = value.toString()
        airBinding.listItemNestedAirValue.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirUnit.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.text = DataTypeParser.getDataText(context, grade)
        airBinding.listItemNestedAirPb.progressDrawable = getProgressDrawable(grade)
        return this
    }

    // 데이터 적용
    fun fetchData(sort: AirQ, value: Int): TestAirQView {
        val moderate = getModerate(sort, value.toDouble())
        val grade = moderate.first
        val progress = moderate.second
        airBinding.listItemNestedAirPb.progress = progress
        airBinding.listItemNestedAirValue.text = value.toString()
        airBinding.listItemNestedAirValue.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirUnit.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.text = DataTypeParser.getDataText(context, grade)
        airBinding.listItemNestedAirPb.progressDrawable = getProgressDrawable(grade)
        return this
    }

    fun fetchWhite(isWhite: Boolean) {
        val color = if (isWhite) context.getColor(R.color.white) else context.getColor(R.color.main_black)
        airBinding.listItemNestedAirTitle.setTextColor(color)
        airBinding.listItemNestedAirHelp.imageTintList = ColorStateList.valueOf(color)
    }

    private fun getProgressDrawable(grade: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources,when(grade) {
            1 -> { R.drawable.airq_good_pb }
            2 -> { R.drawable.airq_normal_pb }
            3 -> { R.drawable.airq_bad_pb }
            4 -> { R.drawable.airq_verybad_pb }
            else -> { R.drawable.airq_good_pb }
        }, null)
    }

    private fun getModerate(sort: AirQ, value: Double): Pair<Int, Int> {
        return when(sort) {
            AirQ.PM2_5 -> {
                when (value) {
                    in 0.0..15.0 -> { Pair(1, (value / (15.0 - 0.0) * 100).toInt()) }
                    in 16.0..35.0 -> { Pair(2, (value / (35.0 - 16.0) * 100).toInt()) }
                    in 36.0..75.0 -> { Pair(3, (value / (75.0 - 36.0) * 100).toInt()) }
                    in 76.0..500.0 -> { Pair(4, (value / (500.0 - 76.0) * 100).toInt()) }
                    else -> { Pair(0,0) }
                }
            }
            AirQ.CO -> {
                when (value) {
                    in 0.0..2.0 -> { Pair(1, (value / (2.0 - 0.0) * 100).toInt()) }
                    in 2.1..9.0 -> { Pair(2, (value / (9.0 - 2.1) * 100).toInt()) }
                    in 9.01..15.0 -> { Pair(3, (value / (15.0 - 9.01) * 100).toInt()) }
                    in 15.01..500.0 -> { Pair(4, (value / (500.0 - 15.01) * 100).toInt()) }
                    else -> { Pair(0,0) }
                }
            }
            AirQ.NO2 -> {
                when (value) {
                    in 0.0..0.03 -> { Pair(1, (value / (0.03 - 0.0) * 100).toInt()) }
                    in 0.031..0.06 -> { Pair(2, (value / (0.06 - 0.031) * 100).toInt()) }
                    in 0.061..0.2 -> { Pair(3, (value / (0.2 - 0.061) * 100).toInt()) }
                    in 0.201..10.0 -> { Pair(4, (value / (10.0 - 0.201) * 100).toInt()) }
                    else -> { Pair(0,0) }
                }
            }
            AirQ.PM10 -> {
                when (value) {
                    in 0.0..30.0 -> { Pair(1, (value / (30.0 - 0.0) * 100).toInt()) }
                    in 31.0..80.0 -> { Pair(2, (value / (80.0 - 31.0) * 100).toInt()) }
                    in 81.0..150.0 -> { Pair(3, (value / (150.0 - 81.0) * 100).toInt()) }
                    in 151.0..500.0 -> { Pair(4, (value / (500.0 - 151.0) * 100).toInt()) }
                    else -> { Pair(0,0) }
                }
            }
            AirQ.SO2 -> {
                when (value) {
                    in 0.0..0.02 -> { Pair(1, (value / (0.02 - 0.0) * 100).toInt()) }
                    in 0.021..0.05 -> { Pair(2, (value / (0.05 - 0.021) * 100).toInt()) }
                    in 0.051..0.15 -> { Pair(3, (value / (0.15 - 0.051) * 100).toInt()) }
                    in 0.151..10.0 -> { Pair(4, (value / (10.0 - 0.151) * 100).toInt()) }
                    else -> { Pair(0,0) }
                }
            }
            AirQ.O3 -> {
                when (value) {
                    in 0.0..0.03 -> { Pair(1, (value / (0.03 - 0.0) * 100).toInt()) }
                    in 0.031..0.09 -> { Pair(2, (value / (0.09 - 0.031) * 100).toInt()) }
                    in 0.091..0.15 -> { Pair(3, (value / (0.15 - 0.091) * 100).toInt()) }
                    in 0.151..10.0 -> { Pair(4, (value / (10.0 - 0.151) * 100).toInt()) }
                    else -> { Pair(0,0) }
                }
            }
        }
    }
}