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
 * @since : 2023-03-23 오후 4:00
 **/
class DailyWeatherAdapter(
    list: ArrayList<AdapterModel.DailyWeatherItem>
) :
    RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
    private val mList = list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DailyWeatherAdapter.ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_daily_weather, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
        if (position > 0 && mList[position - 1].date == mList[position].date) {
            holder.date.visibility = View.INVISIBLE
        } else {
            holder.date.visibility = View.VISIBLE
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val time: TextView = itemView.findViewById(R.id.itemDailyTime)
        private val image: ImageView = itemView.findViewById(R.id.itemDailySky)
        private val value: TextView = itemView.findViewById(R.id.itemDailyValue)
        val date: TextView = itemView.findViewById(R.id.itemDailyDate)

        fun bind(dao: AdapterModel.DailyWeatherItem) {
            time.text = dao.time
            image.setImageDrawable(dao.img)
            value.text = dao.value
            date.text = dao.date
        }
    }
}