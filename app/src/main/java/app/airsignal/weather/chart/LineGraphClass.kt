package app.airsignal.weather.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.animation.AlphaAnimation
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat

class LineGraphClass(private val context: Context, private val isGradient: Boolean) {
    private lateinit var mChart: LineChart
    private lateinit var lineData: LineData

    private var charMadeTime = 0L

    fun getInstance(chartView: LineChart): LineGraphClass {
        mChart = chartView
        lineData = LineData()
        setYAxis()
        setXAxis()
        chartView.setNoDataText("데이터를 불러오는 중입니다")
        chartView.setNoDataTextColor(Color.WHITE)
        return this
    }

    fun setChart(): LineGraphClass {
        try {
            mChart.apply {
//                setBackgroundResource(R.drawable.pm_graph_bg)
                setBackgroundColor(Color.TRANSPARENT) // 배경 색
                legend.isEnabled = false
                description.isEnabled = false // description 표시
                setTouchEnabled(true) // 그래프 터치
                isClickable = false
                axisRight.isEnabled = false
                axisLeft.isEnabled = true
                isDragEnabled = true
                zoom(1.3f, 0f, 1.3f, 0f)
                isHighlightPerTapEnabled = false
                minOffset = 35f
                setScaleEnabled(false)
                setPinchZoom(false) // pinch zoom
//                mChart.setVisibleXRangeMaximum(5f)
                isDoubleTapToZoomEnabled = false
                isLongClickable = true
                isAutoScaleMinMaxEnabled = false
            }
        } catch (e: Exception) {
            e.stackTraceToString()
        }

        return this
    }

    private fun setXAxis() {
        mChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM // X축을 그래프 아래로 위치하기
            textSize = 12f // 레이블 텍스트 사이즈
            textColor = Color.WHITE // 레이블 텍스트 색
            axisLineColor = Color.WHITE // 축 색
            setDrawAxisLine(false) // 그래프 뒷 배경의 그리드 표시
            setDrawGridLines(false) // 그래프 뒷 배경의 그리드 표시
            spaceMax = 1f // 레이블 간격
            isGranularityEnabled = true // 축 레이블 표시 간격
            granularity = 1f // 축 레이블 표시 간격
            axisMaximum = 5f
            setDrawLabels(true)
            setAvoidFirstLastClipping(false)
            valueFormatter = XAxisValueFormat()
        }
    }

    private fun setYAxis() {
        mChart.axisLeft.apply {
            textSize = 12f
            textColor = Color.WHITE
            axisLineColor = Color.WHITE
            setDrawAxisLine(false)
            setDrawGridLines(true)
            spaceMax = 5f
            labelCount = 5
            enableGridDashedLine(25f,15f,0f)
            gridColor = Color.parseColor("#80FFFFFF")
        }
    }

    private fun getBlue(): Int {
        return ResourcesCompat.getColor(context.resources,R.color.graph_blue,null)
    }

    fun addDataSet(sort: String, entry: ArrayList<Entry>): LineGraphClass {
        try {
            val applyColor = if (sort == "미세먼지") getBlue()
            else Color.parseColor("#cc0004ff")

            val dataSet = LineDataSet(null, sort) // DataSet 생성
            dataSet.apply {
                label = sort
                mode = LineDataSet.Mode.LINEAR // 선 그리는 방식
                color = Color.TRANSPARENT // 선 색
                valueTextColor = Color.WHITE // 데이터 수치 텍스트 색
                valueTextSize = 14f // 데이터 수치 텍스트 사이즈
                lineWidth = 2f // 선 굵기
                setDrawCircleHole(false)
                setDrawCircles(true)
                valueFormatter = DataSetValueFormat()
                circleRadius = 6F
                setCircleColor(Color.WHITE)
                if (isGradient) {
                    this.setDrawFilled(true)
                    this.fillDrawable = if (sort == "미세먼지") ContextCompat.getDrawable(
                        context,
                        R.drawable.graph_fill_red
                    )
                    else ContextCompat.getDrawable(context, R.drawable.graph_fill_red)
                }
            }
            lineData.addDataSet(dataSet)
            entry.forEach {
                lineData.addEntry(it, if (sort == "미세먼지") 0 else 1)
                lineData.notifyDataChanged()
                mChart.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.stackTraceToString()
        }

        return this
    }

    fun createGraph() {
        mChart.data = lineData // 데이터 적용
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 400
            repeatCount = 0
        }
        mChart.startAnimation(fadeIn)
        mChart.animateX(400)
        mChart.invalidate()
        mChart.moveViewToX(mChart.lineData.entryCount.toFloat()) // 가장 최근에 추가한 데이터의 위치로 이동처리
    }

    class DataSetValueFormat : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }

    //X축 엔트리 포멧
    inner class XAxisValueFormat : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return chartTimeDivider(value)
        }
    }

    // 현재 시간기준으로 그래프의 X축 라벨을 포맷합니다
    private fun chartTimeDivider(value: Float): String {
        try {
            @SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat("H시")
            charMadeTime = System.currentTimeMillis()
            val lArray: Long = charMadeTime + (value.toInt()) * 60 * 60 * 1000
            return simpleDateFormat.format(lArray)
        } catch (e: IndexOutOfBoundsException) {
            e.stackTraceToString()
        } catch (e: NullPointerException) {
            e.stackTraceToString()
        } catch (e: NegativeArraySizeException) {
            e.stackTraceToString()
        }
        return ""
    }

    fun clear(): LineGraphClass {
        lineData.clearValues()
        return this
    }

}