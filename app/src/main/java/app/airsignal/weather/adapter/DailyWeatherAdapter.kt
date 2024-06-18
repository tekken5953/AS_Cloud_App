package app.airsignal.weather.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.utils.controller.ItemDiffCallback
import app.airsignal.weather.dao.AdapterModel
import com.bumptech.glide.Glide
import java.time.LocalDateTime

/**
 * @author : Lee Jae Young
 * @since : 2023-03-23 오후 4:00
 **/
class DailyWeatherAdapter(
    private val context: Context,
    list: ArrayList<AdapterModel.DailyWeatherItem>, ) :
    RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
    private var mList = list
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

    fun submitList(newItems: ArrayList<AdapterModel.DailyWeatherItem>) {
        val diffResult = DiffUtil.calculateDiff(ItemDiffCallback(mList, newItems))
        mList = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(true)
        holder.bind(mList[position]).apply {
            if (position == 0 || LocalDateTime.parse(mList[position - 1].date).toLocalDate()
                    .compareTo(LocalDateTime.parse(mList[position].date).toLocalDate()) != 0)
                if (!dateSection.contains(position)) dateSection.add(position)

            holder.rain.visibility = if (mList[position].isRain) View.VISIBLE else View.INVISIBLE
        }
    }

    fun getDateSectionList(): ArrayList<Int> = dateSection

    fun getIsWhite(): Boolean = isWhite

    fun setIsWhite(b: Boolean) { isWhite = b }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val time: TextView = itemView.findViewById(R.id.itemDailyTime)
        private val image: ImageView = itemView.findViewById(R.id.itemDailySky)
        private val value: TextView = itemView.findViewById(R.id.itemDailyValue)
        val rain: TextView = itemView.findViewById(R.id.itemDailyRain)

        @SuppressLint("SetTextI18n")
        fun bind(dao: AdapterModel.DailyWeatherItem) {
            time.text = dao.time
            value.text = dao.value
            rain.text = "${dao.rainP?.toInt()}%"
            Glide.with(context).load(dao.img).into(image)

            val applyColor = context.getColor(if (isWhite) R.color.white else R.color.main_black)
            time.setTextColor(applyColor)
            value.setTextColor(applyColor)
        }
    }
}