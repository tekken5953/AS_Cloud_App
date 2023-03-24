package com.example.airsignal_app.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-03-23 오후 4:00
 **/
class HomeViewPagerAdapter(mContext: Context, list: ArrayList<AdapterModel.ViewPagerItem>) :
    RecyclerView.Adapter<HomeViewPagerAdapter.ViewHolder>() {
    private val mList = list
    private val context = mContext
    private val timeWeatherList = ArrayList<AdapterModel.TimeWeatherItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeViewPagerAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.viewpager_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        private val pm10: TextView = itemView.findViewById(R.id.viewPagerPm10Grade)
        private val pm2p5: TextView = itemView.findViewById(R.id.viewPagerPm2p5Grade)
        private val temp: TextView = itemView.findViewById(R.id.viewPagerLiveTempValue)
        private val humid: TextView = itemView.findViewById(R.id.viewPagerHumidValue)
        private val wind: TextView = itemView.findViewById(R.id.viewPagerWindValue)
        private val rainPer: TextView = itemView.findViewById(R.id.viewPagerRainPerValue)
        private val sunset: TextView = itemView.findViewById(R.id.viewPagerSunSetValue)
        private val sunrise: TextView = itemView.findViewById(R.id.viewPagerSunRiseValue)

        private val timeWeather: RecyclerView = itemView.findViewById(R.id.viewPagerTimeWeatherRv)
        private val timeWeatherAdapter = TimeWeatherAdapter(context, timeWeatherList)

        fun bind(dao: AdapterModel.ViewPagerItem) {
            settingSpan(pm10, dao.pm10Grade)
            settingSpan(pm2p5, dao.pm2p5Grade)

            timeWeather.adapter = timeWeatherAdapter
            timeWeatherList.clear()
            for(i: Int in 0..7) {
                addTimeWeatherItem("${i+12}시",
                    ResourcesCompat.getDrawable(context.resources,R.drawable.cloudy,null)!!,
                    "${i+18}˚")
                timeWeatherAdapter.notifyItemInserted(i)
            }
        }
    }

    // 미세먼지 & 초미세먼지 글자 색 설정
    private fun settingSpan(view: TextView, grade: Int) {
        val span = SpannableStringBuilder(getDataString(grade))
        span.setSpan(ForegroundColorSpan(getDataColor(grade)), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text = span
    }

    // 등급에 따른 색상 변환
    private fun getDataColor(grade: Int) : Int {
        return when (grade) {
            0 -> ResourcesCompat.getColor(context.resources, R.color.progressGood, null)
            1 -> ResourcesCompat.getColor(context.resources, R.color.progressNormal, null)
            2 -> ResourcesCompat.getColor(context.resources, R.color.progressBad, null)
            3 -> ResourcesCompat.getColor(context.resources, R.color.progressWorst, null)
            else -> ResourcesCompat.getColor(context.resources, R.color.progressError, null)
        }
    }

    // 등급에 따른 텍스트 변환
    private fun getDataString(grade: Int) : String {
        return when (grade) {
            0 -> "좋음"
            1 -> "보통"
            2 -> "나쁨"
            3 -> "매우나쁨"
            else -> ""
        }
    }

    // 시간별 날씨 리사이클러뷰 아이템 추가
    private fun addTimeWeatherItem(time: String, img: Drawable, value: String) {
        val item = AdapterModel.TimeWeatherItem(time, img, value)

        timeWeatherList.add(item)
    }
}