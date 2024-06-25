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
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.SetSystemInfo
import com.bumptech.glide.Glide

/**
 * @author : Lee Jae Young
 * @since : 2023-03-27 오전 9:37
 **/
class WeeklyWeatherAdapter(
    private val context: Context,
    list: ArrayList<AdapterModel.WeeklyWeatherItem>) :
    RecyclerView.Adapter<WeeklyWeatherAdapter.ViewHolder>() {
    private val mList = list
    private var isWhite = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WeeklyWeatherAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_weekly_weather, parent, false)
        when(GetAppInfo.getUserFontScale()) {
            "small" -> SetSystemInfo.setTextSizeSmall(view.context)
            "big" -> SetSystemInfo.setTextSizeLarge(view.context)
            else -> SetSystemInfo.setTextSizeDefault(view.context)
        }
        Thread.sleep(100)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    fun setIsWhite(b: Boolean) { isWhite = b }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val day: TextView = itemView.findViewById(R.id.weeklyDayText)
        private val date: TextView = itemView.findViewById(R.id.weeklyDayDate)
        private val minImg: ImageView = itemView.findViewById(R.id.weeklyMinIv)
        private val maxImg: ImageView = itemView.findViewById(R.id.weeklyMaxIv)
        private val minText: TextView = itemView.findViewById(R.id.weeklyMinText)
        private val maxText: TextView = itemView.findViewById(R.id.weeklyMaxText)
        private val section: TextView = itemView.findViewById(R.id.weeklyMinMaxSection)
        private val minRain: TextView = itemView.findViewById(R.id.weeklyMinRain)
        private val maxRain: TextView = itemView.findViewById(R.id.weeklyMaxRain)

        @SuppressLint("SetTextI18n")
        fun bind(dao: AdapterModel.WeeklyWeatherItem) {
            day.text = dao.day
            date.text = dao.date
            Glide.with(context).load(dao.minImg).into(minImg)
            Glide.with(context).load(dao.maxImg).into(maxImg)
            minText.text = dao.minText
            maxText.text = dao.maxText

            val applyColor = context.getColor(if (isWhite) R.color.white else R.color.main_black)
            val applySubColor = context.getColor(if (isWhite) R.color.sub_white else R.color.sub_black)

            minRain.text = "${dao.minRain}%"
            maxRain.text = "${dao.maxRain}%"

            maxRain.setTextColor(applySubColor)
            minRain.setTextColor(applySubColor)
            maxRain.compoundDrawablesRelative[0]?.mutate()?.setTint(applySubColor)
            minRain.compoundDrawablesRelative[0]?.mutate()?.setTint(applySubColor)

            day.setTextColor(applyColor)
            date.setTextColor(applySubColor)
            minText.setTextColor(applyColor)
            maxText.setTextColor(applyColor)
            section.setTextColor(applyColor)

             if (bindingAdapterPosition == 0) {
                day.setTextColor(context.getColor(R.color.main_blue_color))
                date.setTextColor(context.getColor(R.color.main_blue_color))
            }
        }
    }
}