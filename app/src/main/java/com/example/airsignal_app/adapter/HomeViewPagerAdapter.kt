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
import androidx.viewpager2.widget.ViewPager2.ScrollState
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel
import kotlin.random.Random

/**
 * @author : Lee Jae Young
 * @since : 2023-03-23 오후 4:00
 **/
class HomeViewPagerAdapter(private val context : Context, list: ArrayList<AdapterModel.WeatherItem>) :
    RecyclerView.Adapter<HomeViewPagerAdapter.ViewHolder>() {
    private val mList = list
    private val dailyWeatherList = ArrayList<AdapterModel.DailyWeatherItem>()
    private val weeklyWeatherList = ArrayList<AdapterModel.WeeklyWeatherItem>()

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

        private val dailyWeather: RecyclerView = itemView.findViewById(R.id.viewPagerDailyWeatherRv)
        private val weeklyWeather: RecyclerView = itemView.findViewById(R.id.viewPagerWeeklyWeatherRv)

        private val dailyWeatherAdapter = DailyWeatherAdapter(context,
            this@HomeViewPagerAdapter.dailyWeatherList)

        private val weeklyWeatherAdapter = WeeklyWeatherAdapter(context,
        this@HomeViewPagerAdapter.weeklyWeatherList)

        fun bind(dao: AdapterModel.WeatherItem) {
            settingSpan(pm10, dao.pm10Grade)
            settingSpan(pm2p5, dao.pm2p5Grade)

            dailyWeather.adapter = dailyWeatherAdapter
            this@HomeViewPagerAdapter.dailyWeatherList.clear()

            weeklyWeather.adapter = weeklyWeatherAdapter
            this@HomeViewPagerAdapter.weeklyWeatherList.clear()


            val testDailyArray = intArrayOf(R.drawable.sunny_test,R.drawable.cloud_test,
                R.drawable.cloud2_test,R.drawable.rainy_test,R.drawable.snow_test)
            for(i: Int in 0..7) {
                addDailyWeatherItem("${i+12}시",
                    ResourcesCompat.getDrawable(context.resources, testDailyArray.random() ,null)!!,
                    "${i+18}˚")
                addWeeklyWeatherItem(context.getString(R.string.dat_of_sat),
                    ResourcesCompat.getDrawable(context.resources, testDailyArray.random() ,null)!!,
                    ResourcesCompat.getDrawable(context.resources, testDailyArray.random() ,null)!!,
                    "${Random.nextInt(0,30)}˚",
                    "${Random.nextInt(0,30)}˚")

                dailyWeatherAdapter.notifyItemInserted(i)
                weeklyWeatherAdapter.notifyItemInserted(i)
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
            0 -> context.getString(R.string.progress_good)
            1 -> context.getString(R.string.progress_normal)
            2 -> context.getString(R.string.progress_bad)
            3 -> context.getString(R.string.progress_verybad)
            else -> ""
        }
    }

    // 시간별 날씨 리사이클러뷰 아이템 추가
    private fun addDailyWeatherItem(time: String, img: Drawable, value: String) {
        val item = AdapterModel.DailyWeatherItem(time, img, value)

        this.dailyWeatherList.add(item)
    }

    // 시간별 날씨 리사이클러뷰 아이템 추가
    private fun addWeeklyWeatherItem(
        day: String, minImg: Drawable,
        maxImg: Drawable, minText: String, maxText: String) {
        val item = AdapterModel.WeeklyWeatherItem(day, minImg, maxImg,minText,maxText)

        this.weeklyWeatherList.add(item)
    }
}