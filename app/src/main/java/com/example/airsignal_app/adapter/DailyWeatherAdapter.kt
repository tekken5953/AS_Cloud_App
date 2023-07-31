package com.example.airsignal_app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.DataTypeParser.getDailyItemDate
import timber.log.Timber
import java.time.LocalDateTime

/**
 * @author : Lee Jae Young
 * @since : 2023-03-23 오후 4:00
 **/
class DailyWeatherAdapter(
    private val context: Context,
    list: ArrayList<AdapterModel.DailyWeatherItem>
) :
    RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
    private val mList = list
    private val dateSection = ArrayList<Int>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DailyWeatherAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_daily_weather, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position]).apply {
            if (position == 0 ||
                LocalDateTime.parse(mList[position - 1].date).toLocalDate()
                    .compareTo(LocalDateTime.parse(mList[position].date).toLocalDate())
                != 0) {
                holder.date.visibility = View.VISIBLE
                if (!dateSection.contains(position)) {
                    dateSection.add(position)
                }
            } else {
                holder.date.visibility = View.INVISIBLE
            }

            if (mList[position].isRain) {
                holder.rain.visibility = View.VISIBLE
            } else {
                holder.rain.visibility = View.INVISIBLE
            }
        }
    }

    fun getDateSectionList(): ArrayList<Int> {
        return dateSection
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val time: TextView = itemView.findViewById(R.id.itemDailyTime)
        private val image: ImageView = itemView.findViewById(R.id.itemDailySky)
        private val value: TextView = itemView.findViewById(R.id.itemDailyValue)
        val date: TextView = itemView.findViewById(R.id.itemDailyDate)
        val rain: TextView = itemView.findViewById(R.id.itemDailyRain)

        @SuppressLint("SetTextI18n")
        fun bind(dao: AdapterModel.DailyWeatherItem) {
            time.text = dao.time
            image.setImageDrawable(dao.img)
            value.text = dao.value
            date.text = getDailyItemDate(context, LocalDateTime.parse(dao.date))
            rain.text = "${dao.rainP?.toInt().toString()}%"

            if (adapterPosition == 0) {
                date.setBackgroundResource(R.drawable.daily_date_bg_s)
            } else {
                date.setBackgroundResource(R.drawable.daily_date_bg_ns)
            }
        }
    }
}