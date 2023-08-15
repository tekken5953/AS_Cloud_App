package com.example.airsignal_app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView


/**
 * @author : Lee Jae Young
 * @since : 2023-06-07 오후 2:20
 **/
class OutlineTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    override fun onDraw(canvas: Canvas) {
        val textPaint = paint
        val textColor = currentTextColor
        textPaint.style = Paint.Style.STROKE // 외곽선 스타일 설정
        textPaint.strokeWidth = 2f // 외곽선 두께 설정
        setTextColor(Color.WHITE) // 텍스트 색상 변경 (외곽선 색상)

        super.onDraw(canvas)

        setTextColor(textColor) // 원래 텍스트 색상으로 되돌리기
        textPaint.style = Paint.Style.FILL // 텍스트 스타일을 기본 스타일로 되돌리기
    }
}