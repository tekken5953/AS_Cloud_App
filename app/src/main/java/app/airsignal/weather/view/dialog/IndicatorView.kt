package app.airsignal.weather.view.dialog

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R

class IndicatorView(private val context: Context, private val listSize: Int) {
    private lateinit var indicators: Array<ImageView>

    fun createIndicators(container: LinearLayout, viewPager: ViewPager2, color: ColorStateList) {
        indicators = Array(listSize) {
            val indicatorView = ImageView(context)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (listSize > 1) {
                        updateIndicators(position)
                        viewPager.requestLayout()
                    }
                }
            })

            indicatorView.imageTintList = color
            params.setMargins(10, 0, 10, 0)
            indicatorView.layoutParams = params
            indicatorView.setImageResource(R.drawable.indicator_empty) // 선택되지 않은 원 이미지
            indicatorView.scaleType = ImageView.ScaleType.FIT_XY
            container.addView(indicatorView)
            indicatorView
        }
        updateIndicators(viewPager.currentItem)
    }

    fun updateIndicators(position: Int) {
        for (i in indicators.indices) {
            val animator = ValueAnimator.ofInt(
                indicators[i].layoutParams.width,
                if (i == position) 120 else 35
            )
            animator.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                val params = indicators[i].layoutParams
                params.width = value
                indicators[i].layoutParams = params
            }

            animator.duration = 300
            animator.start()
        }
    }
}