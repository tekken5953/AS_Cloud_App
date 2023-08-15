package com.example.airsignal_app.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.airsignal_app.R
import com.example.airsignal_app.util.VibrateUtil
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.view.activity.WarningDetailActivity

/**
 * @author : Lee Jae Young
 * @since : 2023-06-013 오후 2:35
 **/
class WarningViewPagerAdapter(
    private val context: Activity,
    list: ArrayList<String>,
    private val viewPager2: ViewPager2
) :
    RecyclerView.Adapter<WarningViewPagerAdapter.ViewHolder>() {
    private val mList = list
    private var textColor: Int = Color.WHITE

    private lateinit var onClickListener: OnItemClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WarningViewPagerAdapter.ViewHolder {
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

    fun changeTextColor(color: Int) {
        textColor = color
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(R.id.vpWarningText)

        fun bind(dao: String) {
            textView.text = dao
            textView.setTextColor(textColor)

            if (mList.size == 0) {
                viewPager2.visibility = View.GONE
            } else {
                viewPager2.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        val intent = Intent(context, WarningDetailActivity::class.java)
                        intent.putExtra("warning", mList)
                        intent.putExtra("address", GetAppInfo.getNotificationAddress(context))
                        context.startActivity(intent)
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}