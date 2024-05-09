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
import app.airsignal.weather.util.`object`.DataTypeParser

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오후 1:46
 **/
class ExternalAirView(context: Context, attrs: AttributeSet?)
    : RelativeLayout(context, attrs) {
    private var airBinding: CustomViewAirqBinding

    enum class AirQ(val title: String,val en: String, val sort: String) {
        PM2_5(title = "초미세먼지", en = "ultrafine dust", sort = "PM2.5"),
        PM10(title = "미세먼지", en = "fine dust",sort = "PM10"),
        NO2(title = "이산화질소", en = "nitrogen dioxide", sort = "NO2"),
        SO2(title = "아황산가스", en = "sulfur dioxide", sort = "SO2"),
        CO(title = "일산화탄소", en = "carbon monoxide", sort = "CO"),
        O3(title = "오존", en = "ozone", sort = "O3")
    }

    private data class Range(val min: Double, val max: Double)
    private data class Grade(val grade: Int, val percentage: Int)

    init {
        val inflater = LayoutInflater.from(context)
        airBinding = CustomViewAirqBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.ExternalAirView)
            val title = typedArray.getString(R.styleable.ExternalAirView_airTitle)
            val unit = typedArray.getString(R.styleable.ExternalAirView_airUnit)

            airBinding.apply {
                listItemNestedAirTitle.text = translateTitle(title ?: "")
                listItemNestedAirUnit.text = unit
            }

            typedArray.recycle()
        }
    }

    fun setOnClickListener(popupHelp: AirQView): ExternalAirView {
        airBinding.listItemNestedAirHelp.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (popupHelp.alpha == 0f) {
                    val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                    fadeIn.duration = 400
                    // 팝업 다이얼로그 생성
                    popupHelp.apply {
                        bringToFront()
                        when(airBinding.listItemNestedAirTitle.text.toString()) {
                            context.getString(R.string.pm2_5_full) -> {
                                popupHelp.fetchData(
                                modifyDataSort(context, context.getString(R.string.pm2_5_full)),
                                modifyDataGraph(context, context.getString(R.string.pm2_5_full))!!,
                                AirQ.PM2_5.sort, context.getString(R.string.pm2_5_full)) }
                            context.getString(R.string.pm10_full) -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, context.getString(R.string.pm10_full)),
                                    modifyDataGraph(context, context.getString(R.string.pm10_full))!!,
                                    AirQ.PM10.sort, context.getString(R.string.pm10_full)) }
                            context.getString(R.string.no2_full) -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, context.getString(R.string.no2_full)),
                                    modifyDataGraph(context, context.getString(R.string.no2_full))!!,
                                    AirQ.NO2.sort, context.getString(R.string.no2_full))}
                            context.getString(R.string.so2_full) -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, context.getString(R.string.so2_full)),
                                    modifyDataGraph(context, context.getString(R.string.so2_full))!!,
                                    AirQ.SO2.sort, context.getString(R.string.so2_full)) }
                            context.getString(R.string.o3_full) -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, context.getString(R.string.o3_full)),
                                    modifyDataGraph(context, context.getString(R.string.o3_full))!!,
                                    AirQ.O3.sort, context.getString(R.string.o3_full)) }
                            context.getString(R.string.co_full) -> {
                                popupHelp.fetchData(
                                    modifyDataSort(context, context.getString(R.string.co_full)),
                                    modifyDataGraph(context, context.getString(R.string.co_full))!!,
                                    AirQ.CO.sort, context.getString(R.string.co_full)) }
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
    fun fetchData(sort: AirQ, value: Double): ExternalAirView {
        val moderate = getModerate(sort, value)
        val grade = moderate.first
        val progress = moderate.second
        airBinding.listItemNestedAirPb.progress = if (grade == 4) 100 else progress
        airBinding.listItemNestedAirValue.text = if (value != -999.0) value.toString() else context.getString(R.string.error)
        airBinding.listItemNestedAirValue.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirUnit.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.text = if (value != -999.0) DataTypeParser.getDataText(context, grade) else context.getString(R.string.error)
        airBinding.listItemNestedAirPb.progressDrawable = getProgressDrawable(grade)
        return this
    }

    // 데이터 적용
    fun fetchData(sort: AirQ, value: Int): ExternalAirView {
        val moderate = getModerate(sort, value.toDouble())
        val grade = moderate.first
        val progress = moderate.second
        airBinding.listItemNestedAirPb.progress = if (grade == 4) 100 else progress
        airBinding.listItemNestedAirValue.text = if (value != -999) value.toString() else context.getString(R.string.error)
        airBinding.listItemNestedAirValue.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirUnit.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.text = if (value != -999) DataTypeParser.getDataText(context, grade) else context.getString(R.string.error)
        airBinding.listItemNestedAirPb.progressDrawable = getProgressDrawable(grade)
        return this
    }

    fun fetchWhite(isWhite: Boolean) {
        val color = if (isWhite) context.getColor(R.color.white) else context.getColor(R.color.main_black)
        airBinding.listItemNestedAirTitle.setTextColor(color)
        airBinding.listItemNestedAirHelp.imageTintList = ColorStateList.valueOf(color)
    }

    private fun translateTitle(title: String): String {
        return when(title) {
            AirQ.PM2_5.title -> {context.getString(R.string.pm2_5_full)}
            AirQ.PM10.title -> {context.getString(R.string.pm10_full)}
            AirQ.NO2.title -> {context.getString(R.string.no2_full)}
            AirQ.O3.title -> {context.getString(R.string.o3_full)}
            AirQ.SO2.title -> {context.getString(R.string.so2_full)}
            AirQ.CO.title -> {context.getString(R.string.co_full)}
            else -> {""}
        }
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
        fun calculateGrade(value: Double, ranges: List<Range>): Grade {
            ranges.forEachIndexed { index, range ->
                if (value in range.min..range.max) {
                    val percentage = ((value - range.min) / (range.max - range.min) * 100).toInt()
                    return Grade(index + 1, percentage)
                }
            }
            return Grade(0, 0)
        }

        return when (sort) {
            AirQ.PM2_5 -> calculateGrade(value, listOf(
                Range(0.0, 15.0),
                Range(16.0, 35.0),
                Range(36.0, 75.0),
                Range(76.0, 500.0)
            ))
            AirQ.CO -> calculateGrade(value, listOf(
                Range(0.0, 2.0),
                Range(2.1, 9.0),
                Range(9.01, 15.0),
                Range(15.01, 500.0)
            ))
            AirQ.NO2 -> calculateGrade(value, listOf(
                Range(0.0, 0.03),
                Range(0.031, 0.06),
                Range(0.061, 0.2),
                Range(0.201, 10.0)
            ))
            AirQ.PM10 -> calculateGrade(value, listOf(
                Range(0.0, 30.0),
                Range(31.0, 80.0),
                Range(81.0, 150.0),
                Range(151.0, 500.0)
            ))
            AirQ.SO2 -> calculateGrade(value, listOf(
                Range(0.0, 0.02),
                Range(0.021, 0.05),
                Range(0.051, 0.15),
                Range(0.151, 10.0)
            ))
            AirQ.O3 -> calculateGrade(value, listOf(
                Range(0.0, 0.03),
                Range(0.031, 0.09),
                Range(0.091, 0.15),
                Range(0.151, 10.0)
            ))
        }.let { Pair(it.grade, it.percentage) }
    }
}