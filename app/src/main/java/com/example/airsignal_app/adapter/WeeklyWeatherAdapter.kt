package com.example.airsignal_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-03-27 오전 9:37
 **/
class WeeklyWeatherAdapter(mContext: Context, list: ArrayList<AdapterModel.WeeklyWeatherItem>) :
    RecyclerView.Adapter<WeeklyWeatherAdapter.ViewHolder>() {
        private val mList = list
        private val context = mContext

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): WeeklyWeatherAdapter.ViewHolder {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val view: View = inflater.inflate(R.layout.list_item_weekly_weather, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = mList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(mList[position])
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
        {
            private val day: TextView = itemView.findViewById(R.id.weeklyDayText)
            private val minImg: ImageView = itemView.findViewById(R.id.weeklyMinIv)
            private val maxImg: ImageView = itemView.findViewById(R.id.weeklyMaxIv)
            private val minText: TextView = itemView.findViewById(R.id.weeklyMinText)
            private val maxText: TextView = itemView.findViewById(R.id.weeklyMaxText)

            fun bind(dao: AdapterModel.WeeklyWeatherItem) {
                day.text = dao.day
                minImg.setImageDrawable(dao.minImg)
                maxImg.setImageDrawable(dao.maxImg)
                minText.text = dao.minText
                maxText.text = dao.maxText
            }
        }
}