package app.chart

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.animation.AlphaAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import app.airsignal.weather.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener


class LineGraphClass(private val context: Context,private val isGradient: Boolean, private val selected: TextView):
    OnChartValueSelectedListener {
    private lateinit var mChart: LineChart
    private lateinit var lineData: LineData

    fun getInstance(chartView: LineChart): LineGraphClass {
        mChart = chartView
        lineData = LineData()
        return this
    }

    fun setChart(): LineGraphClass {
        try {
            mChart.setBackgroundColor(Color.parseColor("#40FFFFFF")) // 배경 색
            mChart.setTouchEnabled(false)
            mChart.isClickable = false
            mChart.legend.isEnabled = false
            mChart.description.isEnabled = false // description 표시
            mChart.axisRight.isEnabled = false
            mChart.axisLeft.isEnabled = true
            mChart.isDragEnabled = true
            mChart.setOnChartValueSelectedListener(this@LineGraphClass)
            mChart.setScaleEnabled(false)
            mChart.setPinchZoom(false) // pinch zoom
            mChart.minOffset = 5f
            mChart.setVisibleXRangeMaximum(6f)
            mChart.isDoubleTapToZoomEnabled = false
            setYAxis()
            setXAxis()
        } catch (e: Exception) {
            e.stackTraceToString()
            Toast.makeText(context, "차트 세팅 에러", Toast.LENGTH_SHORT).show()
        }

        return this
    }

    private fun setXAxis() {
        // X축
        val xAxis = mChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM // X축을 그래프 아래로 위치하기
        xAxis.textSize = 10f // 레이블 텍스트 사이즈
        xAxis.textColor = Color.GRAY // 레이블 텍스트 색
        xAxis.axisLineColor = Color.GRAY // 축 색
        xAxis.setDrawAxisLine(false) // 그래프 뒷 배경의 그리드 표시
        xAxis.setDrawGridLines(false) // 그래프 뒷 배경의 그리드 표시
        xAxis.spaceMax = 0.5f // 레이블 간격
        xAxis.isGranularityEnabled = true // 축 레이블 표시 간격
        xAxis.granularity = 1f // 축 레이블 표시 간격
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.axisMaximum = 6f
    }

    private fun setYAxis() {
        // Y축
        val yAxis = mChart.axisLeft
        yAxis.textSize = 10f
        yAxis.textColor = Color.GRAY
        yAxis.axisLineColor = Color.GRAY
        yAxis.setDrawAxisLine(false)
        yAxis.setDrawGridLines(false)
    }

    fun addDataSet(sort: String, entry: ArrayList<Entry>): LineGraphClass {
        try {
            val applyColor = if (sort == "미세먼지") Color.parseColor("#FF0000")
            else Color.parseColor("#cc0004ff")

            val dataSet = LineDataSet(entry, sort) // DataSet 생성
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 선 그리는 방식
            dataSet.color = applyColor // 선 색
            dataSet.valueTextColor = applyColor // 데이터 수치 텍스트 색
            dataSet.valueTextSize = 12f // 데이터 수치 텍스트 사이즈
            dataSet.lineWidth = 2f // 선 굵기
            dataSet.setDrawCircleHole(false)
            dataSet.setDrawCircles(true)
            dataSet.circleRadius = 2.5F
            dataSet.valueFormatter = DataSetValueFormat()
            dataSet.setCircleColor(applyColor)
            if (isGradient) {
                dataSet.setDrawFilled(true)
                dataSet.fillAlpha = 70
                dataSet.fillDrawable = if (sort == "미세먼지") ContextCompat.getDrawable(
                    context,
                    R.drawable.graph_fill_red
                )
                else ContextCompat.getDrawable(context, R.drawable.graph_fill_blue)
            }
            lineData.addDataSet(dataSet)
        } catch (e: Exception) {
            e.stackTraceToString()
        }

        return this
    }

    fun createGraph() {
        mChart.data = lineData // 데이터 적용
        val fadeIn = AlphaAnimation(0f,1f).apply {
            duration = 400
            repeatCount = 0
        }
        mChart.startAnimation(fadeIn)
        mChart.animateX(400)
        mChart.invalidate()
        mChart.moveViewToX(mChart.lineData.entryCount.toFloat()) // 가장 최근에 추가한 데이터의 위치로 이동처리
        mChart.data.notifyDataChanged()
        mChart.notifyDataSetChanged()
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        selected.text = e!!.y.toInt().toString()
        Log.d("testtest","value selected ${e!!.y}")
    }

    override fun onNothingSelected() {
        selected.text = "Nothing Selected"
        Log.w("testtest","nothing selected")
    }

    inner class DataSetValueFormat : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }
}