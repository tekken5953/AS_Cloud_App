package com.example.airsignal_app.view

/**
 * @author : Lee Jae Young
 * @since : 2023-05-23 오전 10:41
 **/
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R

class SemiCircularProgressBar : View {
    private val circlePaint = Paint()
    private val progressPaint = Paint()
    private val rectF = RectF()

    var progress: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        circlePaint.color = context.resources.getColor(R.color.main_gray_color)
        progressPaint.color = context.resources.getColor(R.color.progress_color)
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() / 2
        val radius = (Math.min(width, height) - paddingLeft - paddingRight) / 2.toFloat()
        val startAngle = 180f
        val sweepAngle = progress.toFloat() / 100 * 180f

        // 배경 원형 그리기
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // 반원 모양의 프로그래스바 그리기
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        canvas.drawArc(rectF, startAngle, sweepAngle, true, progressPaint)
    }
}