package app.airsignal.weather.view.custom_view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.fragment.EyeDetailReportFragment
import app.airsignal.weather.databinding.CustomViewAirqBinding
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.`object`.DataTypeParser

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오후 1:46
 **/
class TestAirQView(context: Context, attrs: AttributeSet?)
    : RelativeLayout(context, attrs) {
    private var airBinding: CustomViewAirqBinding

    enum class AirQ { PM2_5,PM10,NO2,SO2,CO,O3 }

    init {
        val inflater = LayoutInflater.from(context)
        airBinding = CustomViewAirqBinding.inflate(inflater, this, true)

        attrs?.let { set ->
            val typedArray = context.obtainStyledAttributes(set, R.styleable.TestAirQView)

            val title = typedArray.getString(R.styleable.TestAirQView_airTitle)

            airBinding.apply {
                listItemNestedAirTitle.text = title
            }
            typedArray.recycle()
        }
    }

    // 데이터 적용
    fun fetchData(sort: AirQ, value: Double, grade: Int): TestAirQView {
        TimberUtil().d("testtest","fetch $sort $value $grade")
        airBinding.listItemNestedAirPb.progress = setProgress(sort, value, grade)
        airBinding.listItemNestedAirValue.text = value.toString()
        airBinding.listItemNestedAirValue.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.text = DataTypeParser.getDataText(context, grade)
        airBinding.listItemNestedAirPb.setBackgroundColor(getProgressBackgroundColor(grade))
        airBinding.listItemNestedAirPb.progressTintList = ColorStateList.valueOf(DataTypeParser.getDataColor(context,grade))
        return this
    }

    // 데이터 적용
    fun fetchData(sort: AirQ, value: Int, grade: Int): TestAirQView {
        TimberUtil().d("testtest","fetch $sort $value $grade")
        airBinding.listItemNestedAirPb.progress = setProgress(sort, value, grade)
        airBinding.listItemNestedAirValue.text = value.toString()
        airBinding.listItemNestedAirValue.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.setTextColor(DataTypeParser.getDataColor(context,grade))
        airBinding.listItemNestedAirGrade.text = DataTypeParser.getDataText(context, grade)
        airBinding.listItemNestedAirPb.setBackgroundColor(getProgressBackgroundColor(grade))
        airBinding.listItemNestedAirPb.progressTintList = ColorStateList.valueOf(DataTypeParser.getDataColor(context,grade))
        return this
    }

    private fun getProgressBackgroundColor(grade: Int): Int {
        return context.getColor(when(grade) {
            1 -> { R.color.ae_good_sub }
            2 -> { R.color.ae_normal_sub }
            3 -> { R.color.ae_bad_sub }
            4 -> { R.color.ae_very_bad_sub }
            else -> {R.color.ae_good_sub}
        })
    }

    private fun setProgress(sort: AirQ, value: Int, grade: Int): Int {
        val pair = getModerate(sort, grade)
        return  (value / (pair.second - pair.first) * 100).toInt()
    }

    private fun setProgress(sort: AirQ, value: Double, grade: Int): Int {
        val pair = getModerate(sort, grade)
        return  (value / (pair.second - pair.first) * 100).toInt()
    }

    private fun getModerate(sort: AirQ, grade: Int): Pair<Double, Double> {
        return when(sort) {
            AirQ.PM2_5 -> {
                when (grade) {
                    1 -> { Pair(0.0,15.0) }
                    2 -> { Pair(16.0,35.0) }
                    3 -> { Pair(36.0,75.0) }
                    4 -> { Pair(76.0,500.0) }
                    else -> { Pair(0.0, 0.0) }
                }
            }
            AirQ.CO -> {
                when (grade) {
                    1 -> { Pair(0.0,2.0) }
                    2 -> { Pair(2.1,9.0) }
                    3 -> { Pair(9.01,15.0) }
                    4 -> { Pair(15.01,500.0) }
                    else -> { Pair(0.0, 0.0) }
                }
            }
            AirQ.NO2 -> {
                when (grade) {
                    1 -> { Pair(0.0,0.03) }
                    2 -> { Pair(0.031,0.06) }
                    3 -> { Pair(0.061,0.2) }
                    4 -> { Pair(0.201,10.0) }
                    else -> { Pair(0.0, 0.0) }
                }
            }
            AirQ.PM10 -> {
                when (grade) {
                    1 -> { Pair(0.0,30.0) }
                    2 -> { Pair(31.0,80.0) }
                    3 -> { Pair(81.0,150.0) }
                    4 -> { Pair(151.0,500.0) }
                    else -> { Pair(0.0, 0.0) }
                }
            }
            AirQ.SO2 -> {
                when (grade) {
                    1 -> { Pair(0.0,0.02) }
                    2 -> { Pair(0.021,0.05) }
                    3 -> { Pair(0.051,0.15) }
                    4 -> { Pair(0.151,10.0) }
                    else -> { Pair(0.0, 0.0) }
                }
            }
            AirQ.O3 -> {
                when (grade) {
                    1 -> { Pair(0.0,0.03) }
                    2 -> { Pair(0.031,0.09) }
                    3 -> { Pair(0.091,0.15) }
                    4 -> { Pair(0.151,10.0) }
                    else -> { Pair(0.0, 0.0) }
                }
            }
        }
    }
}