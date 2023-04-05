package com.example.airsignal_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-03-23 오후 4:00
 **/
class DailyWeatherAdapter(private val context: Context, list: ArrayList<AdapterModel.DailyWeatherItem>) :
    RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
    private val mList = list

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
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        private val time: TextView = itemView.findViewById(R.id.itemDailyTime)
        private val image: ImageView = itemView.findViewById(R.id.itemDailySky)
        private val value: TextView = itemView.findViewById(R.id.itemDailyValue)
        private val layout: RelativeLayout = itemView.findViewById(R.id.itemDailyLayout)

        fun bind(dao: AdapterModel.DailyWeatherItem) {
            time.text = dao.time
            image.setImageDrawable(dao.img)
            value.text = dao.value

            if (adapterPosition == 0) {
                layout.background = ResourcesCompat.getDrawable(context.resources, R.drawable.daily_selected_bg, null)
            }
        }
    }
}