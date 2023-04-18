package com.example.airsignal_app.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.util.ConvertDataType
import java.time.LocalDateTime
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-03-27 오전 9:37
 **/
class WeeklyWeatherAdapter(
    private val context: Context,
    list: ArrayList<AdapterModel.WeeklyWeatherItem>
) :
    RecyclerView.Adapter<WeeklyWeatherAdapter.ViewHolder>() {
    private val mList = list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WeeklyWeatherAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_weekly_weather, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val day: TextView = itemView.findViewById(R.id.weeklyDayText)
        private val minImg: ImageView = itemView.findViewById(R.id.weeklyMinIv)
        private val maxImg: ImageView = itemView.findViewById(R.id.weeklyMaxIv)
        private val minText: TextView = itemView.findViewById(R.id.weeklyMinText)
        private val maxText: TextView = itemView.findViewById(R.id.weeklyMaxText)
        private val line: View = itemView.findViewById(R.id.weeklyBottomLine)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(dao: AdapterModel.WeeklyWeatherItem) {
            day.text = dao.day
            minImg.setImageDrawable(dao.minImg)
            maxImg.setImageDrawable(dao.maxImg)
            minText.text = dao.minText
            minText.setTextColor(context.getColor(R.color.main_blue_color))
            maxText.text = dao.maxText
            maxText.setTextColor(context.getColor(R.color.red))

            val currentDate = LocalDateTime.now()
            if (mList[adapterPosition].day == "${currentDate.month.value}.${currentDate.dayOfMonth}" +
                "(${
                    ConvertDataType.convertDayOfWeekToKorean(
                        context,
                        currentDate.dayOfWeek.value
                    )
                })"
            ) {
                day.setTextColor(context.getColor(R.color.main_blue_color))
            }

            if (adapterPosition == itemCount - 1) {
                line.visibility = View.GONE
            }
        }
    }
}