package com.example.airsignal_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.util.VibrateUtil
import com.orhanobut.logger.Logger
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-06-013 오후 2:35
 **/
class ReportViewPagerAdapter(
    private val context: Context,
    list: ArrayList<AdapterModel.ReportItem>,
    private val viewPager2: ViewPager2
) :
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

            if (mList.size == 0) {
                viewPager2.visibility = View.GONE
            } else {
                viewPager2.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        val ellipsisCount = textView.layout.getEllipsisCount(textView.lineCount - 1)
                        val isEllipsized = ellipsisCount > 0
                        if (textView.maxLines == 3) {
                            if (isEllipsized) {
                                mVib()
                                val lineCount = textView.lineCount + (ellipsisCount / textView.maxEms + 1)
                                textView.maxLines = lineCount
                                val lineHeight = textView.lineHeight
                                val desiredHeight = lineCount * lineHeight
                                textView.layoutParams.height = desiredHeight
                                viewPager2.layoutParams.height = desiredHeight
                                textView.requestLayout()
                            }
                        } else {
                            mVib()
                            val lineCount = 3
                            textView.maxLines = 3
                            val lineHeight = textView.lineHeight
                            val desiredHeight = lineCount * lineHeight
                            textView.layoutParams.height = desiredHeight
                            viewPager2.layoutParams.height = desiredHeight
                            textView.requestLayout()
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                } else {
                    Timber.tag("testtest").w("No Position")
                }
            }
        }
    }
}