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
class TimeWeatherAdapter(mContext: Context, list: ArrayList<AdapterModel.TimeWeatherItem>) :
    RecyclerView.Adapter<TimeWeatherAdapter.ViewHolder>() {
    private val mList = list
    private val context = mContext

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeWeatherAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_time_weather, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        private val time: TextView = itemView.findViewById(R.id.itemTimeWeatherTime)
        private val image: ImageView = itemView.findViewById(R.id.itemTimeWeatherSky)
        private val value: TextView = itemView.findViewById(R.id.itemTimeWeatherValue)

        fun bind(dao: AdapterModel.TimeWeatherItem) {
            time.text = dao.time
            image.setImageDrawable(dao.img)
            value.text = dao.value
        }
    }
}