package app.airsignal.weather.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.animation.AnimationUtils
import android.widget.TextView
import app.airsignal.weather.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class CustomMarkerView(context: Context?, layoutResource: Int) :
    MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.markerTextView)

    // 마커가 표시될 때 호출되는 메서드
    override fun refreshContent(e: Entry, highlight: Highlight?) {
        // 마커의 내용을 설정
        tvContent.text = e.y.roundToInt().toString()
        super.refreshContent(e, highlight)
    }

    // 마커가 그려질 때 호출되는 메서드
    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        // 마커의 위치 조정
        var mPosX = posX
        var mPosY = posY
        mPosX -= (width / 2).toFloat()
        mPosY -= (height * 1.5).toFloat()
        super.draw(canvas, mPosX, mPosY)
    }
}