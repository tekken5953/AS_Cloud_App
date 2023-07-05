package com.example.airsignal_app.view.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewSegmentPbSectionBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-07-04 오후 1:47
 **/
@SuppressLint("Recycle", "CustomViewStyleable")
class SegmentSectionView(context: Context, attrs: AttributeSet?)
    : LinearLayout(context, attrs) {
    private var segmentBinding: CustomViewSegmentPbSectionBinding

    init {
        val inflater = LayoutInflater.from(context)
        segmentBinding = CustomViewSegmentPbSectionBinding.inflate(inflater, this, true)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SegmentSectionView)
            val customSection1 = typedArray.getString(R.styleable.SegmentSectionView_section1)
            val customSection2 = typedArray.getString(R.styleable.SegmentSectionView_section2)
            val customSection3 = typedArray.getString(R.styleable.SegmentSectionView_section3)
            val weight0 = typedArray.getFloat(R.styleable.SegmentSectionView_weight0, 0f)
            val weight1 = typedArray.getFloat(R.styleable.SegmentSectionView_weight1, 0f)
            val weight2 = typedArray.getFloat(R.styleable.SegmentSectionView_weight2, 0f)
            val weight3 = typedArray.getFloat(R.styleable.SegmentSectionView_weight3, 0f)
            typedArray.recycle()

            segmentBinding.customSegment1.text = customSection1
            segmentBinding.customSegment2.text = customSection2
            segmentBinding.customSegment3.text = customSection3

            setWeight(segmentBinding.customSegment0, weight0)
            setWeight(segmentBinding.customSegment1, weight1)
            setWeight(segmentBinding.customSegment2, weight2)
            setWeight(segmentBinding.customSegment3, weight3)
        }
    }

    private fun setWeight(textView: TextView, f: Float) {
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layoutParams.weight = f
        textView.layoutParams = layoutParams
    }
}