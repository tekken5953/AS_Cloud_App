package app.airsignal.weather.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.util.`object`.DataTypeParser.getDailyItemDate
import java.time.LocalDateTime

/**
 * @author : Lee Jae Young
 * @since : 2023-03-23 오후 4:00
 **/
class DailyWeatherAdapter(
    private val context: Context,
    list: ArrayList<AdapterModel.DailyWeatherItem>,
) :
    RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
    private val mList = list
    private val dateSection = ArrayList<Int>()
    private var isWhite: Boolean = false

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
                if (!dateSection.contains(position)) {
                    dateSection.add(position)
                }
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

    fun getIsWhite(): Boolean {
        return isWhite
    }

    fun setIsWhite(b: Boolean) {
        isWhite = b
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val time: TextView = itemView.findViewById(R.id.itemDailyTime)
        private val image: ImageView = itemView.findViewById(R.id.itemDailySky)
        private val value: TextView = itemView.findViewById(R.id.itemDailyValue)
        val rain: TextView = itemView.findViewById(R.id.itemDailyRain)

        @SuppressLint("SetTextI18n")
        fun bind(dao: AdapterModel.DailyWeatherItem) {
            time.text = dao.time
            image.setImageDrawable(dao.img)
            value.text = dao.value
            rain.text = "${dao.rainP?.toInt().toString()}%"

            if (isWhite) {
                time.setTextColor(context.getColor(R.color.white))
                value.setTextColor(context.getColor(R.color.white))
            } else {
                time.setTextColor(context.getColor(R.color.main_black))
                value.setTextColor(context.getColor(R.color.main_black))
            }
        }
    }
}