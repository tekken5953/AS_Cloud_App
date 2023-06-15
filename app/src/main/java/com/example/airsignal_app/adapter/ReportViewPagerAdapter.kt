package com.example.airsignal_app.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.util.VibrateUtil

/**
 * @author : Lee Jae Young
 * @since : 2023-06-013 오후 2:35
 **/
class ReportViewPagerAdapter(private val context: Context, list: ArrayList<AdapterModel.ReportItem>, private val viewPager2: ViewPager2) :
    RecyclerView.Adapter<ReportViewPagerAdapter.ViewHolder>() {
    private val mList = list

    private lateinit var onClickListener: OnItemClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportViewPagerAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.view_pager_item_main_report, parent, false)
        return ViewHolder(view)
    }

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    fun mVib() {
        VibrateUtil(context).make(20)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(R.id.listItemReportText)

        fun bind(dao: AdapterModel.ReportItem) {
            textView.text = dao.text

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        val count = textView.layout.lineCount
                        val lastLineIndex: Int = count - 1
                        val ellipsisCount: Int = textView.layout.getEllipsisCount(lastLineIndex)
                        val isEllipsized = ellipsisCount > 0
//                    onClickListener.onItemClick(it, position)
                        if (textView.maxLines == 2) {
                            if (isEllipsized) {
                                mVib()
                                val lineCount = 4
                                textView.maxLines = lineCount
                                val lineHeight = textView.lineHeight
                                val desiredHeight = lineCount * lineHeight
                                textView.layoutParams.height = desiredHeight
                                viewPager2.layoutParams.height = desiredHeight
                                textView.requestLayout()
                            }
                        } else {
                            mVib()
                            val lineCount = 2
                            textView.maxLines = 2
                            val lineHeight = textView.lineHeight
                            val desiredHeight = lineCount * lineHeight
                            textView.layoutParams.height = desiredHeight
                            viewPager2.layoutParams.height = desiredHeight
                            textView.requestLayout()
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}