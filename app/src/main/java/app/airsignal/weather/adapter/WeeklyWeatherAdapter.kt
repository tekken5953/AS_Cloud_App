package app.airsignal.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.util.`object`.GetAppInfo.getUserFontScale
import app.airsignal.weather.util.`object`.SetSystemInfo

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
        when(getUserFontScale(context)) {
            "small" -> {
                SetSystemInfo.setTextSizeSmall(view.context)
            }
            "big" -> {
                SetSystemInfo.setTextSizeLarge(view.context)
            }
            else -> {
                SetSystemInfo.setTextSizeDefault(view.context)
            }
        }
        Thread.sleep(100)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val day: TextView = itemView.findViewById(R.id.weeklyDayText)
        private val date: TextView = itemView.findViewById(R.id.weeklyDayDate)
        private val minImg: ImageView = itemView.findViewById(R.id.weeklyMinIv)
        private val maxImg: ImageView = itemView.findViewById(R.id.weeklyMaxIv)
        private val minText: TextView = itemView.findViewById(R.id.weeklyMinText)
        private val maxText: TextView = itemView.findViewById(R.id.weeklyMaxText)
        private val line: View = itemView.findViewById(R.id.weeklyBottomLine)

        fun bind(dao: AdapterModel.WeeklyWeatherItem) {
            day.text = dao.day
            date.text = dao.date
            minImg.setImageDrawable(dao.minImg)
            maxImg.setImageDrawable(dao.maxImg)
            minText.text = dao.minText
            maxText.text = dao.maxText

            if (adapterPosition == 0) {
                day.setTextColor(context.getColor(R.color.main_blue_color))
                date.setTextColor(context.getColor(R.color.main_blue_color))
            }

            if (adapterPosition == itemCount - 1) {
                line.visibility = View.GONE
            }
        }
    }
}