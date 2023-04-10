package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.DailyWeatherAdapter
import com.example.airsignal_app.adapter.WeeklyWeatherAdapter
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.StaticDataObject.CHECK_GPS_BACKGROUND
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.gps.GpsWorker
import com.example.airsignal_app.util.*
import com.example.airsignal_app.util.ConvertDataType.convertDayOfWeekToKorean
import com.example.airsignal_app.util.ConvertDataType.getSkyImg
import com.example.airsignal_app.util.ConvertDataType.settingSpan
import com.example.airsignal_app.view.SideMenuClass
import com.example.airsignal_app.vmodel.GetWeatherViewModel
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val sp by lazy { SharedPreferenceManager(this) }
    private var isBackPressed = false
    private var lastRefresh: Long = 0L
    private val getDataViewModel by viewModel<GetWeatherViewModel>()
    private val dailyWeatherList = ArrayList<AdapterModel.DailyWeatherItem>()
    private val weeklyWeatherList = ArrayList<AdapterModel.WeeklyWeatherItem>()
    private val dailyWeatherAdapter by lazy { DailyWeatherAdapter(this, dailyWeatherList) }
    private val weeklyWeatherAdapter by lazy { WeeklyWeatherAdapter(this, weeklyWeatherList) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(
            this@MainActivity,
            R.layout.activity_main
        )
            .apply {
                lifecycleOwner = this@MainActivity
                dataVM = getDataViewModel
                // 뷰모델 생성
                applyGetDataViewModel()
            }

        initializing()

        // 사이드 메뉴 세팅
        SideMenuClass(this, binding.mainDrawerLayout, binding.mainNavView, binding.mainLayout)
            .setUpSideMenu(binding.mainSideMenuIv, binding.mainPb)

        binding.mainGpsTitleTv.setOnClickListener {
            @SuppressLint("InflateParams")
            val searchLayout: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_search_address, null)
            ShowDialogClass()
                .getInstance(this)
                .setBackPressed(searchLayout.findViewById(R.id.searchBack))
                .show(searchLayout, true)

            val searchListView: ListView = searchLayout.findViewById(R.id.searchAddressListView)
            val searchItem = ArrayList<String>()
            val allTextArray = resources.getStringArray(R.array.address)
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, searchItem)
            val searchView: androidx.appcompat.widget.SearchView =
                searchLayout.findViewById(R.id.searchAddressView)
            searchListView.adapter = adapter
            searchView.requestFocus()

            // 서치 뷰 텍스트 변환 콜벡
            searchView.setOnQueryTextListener(object : OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.isNotEmpty()) {
                        searchItem.clear()
                        allTextArray.forEach { allList ->
                            if (allList.contains(newText)) {
                                searchItem.add(allList)
                            }
                        }

                    } else {
                        searchItem.clear()
                    }
                    adapter.notifyDataSetChanged()
                    return true
                }
            })

            // 검색주소 리스트
            searchListView.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    Logger.t("searchView").d("$position : ${searchItem[position]}")
                    ToastUtils(this).customDurationMessage(
                        "$position : ${searchItem[position]}",
                        500
                    )
                }
        }

        // 위로 스와이프 시 화면 갱신 -> GPS 새로 갱신 + 사이드 메뉴 갱신 + 데이터 갱신
        binding.mainSwipeLayout.setOnRefreshListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastRefresh > 1000 * 10) {
                Handler(Looper.getMainLooper()).postDelayed({
                    lastRefresh = currentTime
                    RefreshUtils(this).refreshActivity()
                    if (binding.mainSwipeLayout.isRefreshing) {
                        binding.mainSwipeLayout.isRefreshing = false
                    }
                }, 1500)
            } else {
                Toast.makeText(this, "마지막 갱신 후 10초가 지나야 합니다.", Toast.LENGTH_SHORT).show()
                if (binding.mainSwipeLayout.isRefreshing) {
                    binding.mainSwipeLayout.isRefreshing = false
                }
            }
        }
    }

    private fun getDataSingleTime() {
        CoroutineScope(Dispatchers.IO).launch {
            GetLocation(this@MainActivity).getLocation()
            delay(100)
            getDataViewModel.loadDataResult(
                sp.getString("lat").toDouble(),
                sp.getString("lng").toDouble()
            )
        }
        binding.mainGpsTitleTv.text = sp.getString(lastAddress)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun initializing() {
        addSkeletonItem()

        // 위치 권한 요청
        if (!RequestPermissionsUtil(this).isLocationPermitted()) {
            RequestPermissionsUtil(this).requestLocation()
        }

        // 워크 매니저 생성
        CoroutineScope(Dispatchers.Default).launch { createWorkManager() }

        binding.mainDailyWeatherRv.adapter = dailyWeatherAdapter
        binding.mainWeeklyWeatherRv.adapter = weeklyWeatherAdapter

        getDataSingleTime()
    }

    // 백그라운드에서 GPS 를 불러오기 위한 WorkManager
    private fun createWorkManager() {
        val workManager = WorkManager.getInstance(this)
        val workRequest =
            PeriodicWorkRequest.Builder(GpsWorker::class.java, 15, TimeUnit.MINUTES).build()
        workManager.enqueueUniquePeriodicWork(
            CHECK_GPS_BACKGROUND,
            ExistingPeriodicWorkPolicy.KEEP, workRequest
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (!isBackPressed) {
                ToastUtils(this).customDurationMessage("버튼을 한번 더 누르면 앱이 종료됩니다", 2)
                isBackPressed = true
            } else {
                EnterPage(this).fullyExit()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                isBackPressed = false
            }, 2000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun applyGetDataViewModel() {
        getDataViewModel.getDataResult().observe(this) { it ->
            it?.let { result ->
                val realtime = result.realtime[0]
                val sun = result.sun
                val air = result.quality
                val week = result.week
                val tempDate = LocalDateTime.parse(week.tempDate)
                val wfMin = listOf(
                    week.wf1Am, week.wf2Am, week.wf3Am,
                    week.wf4Am, week.wf5Am, week.wf6Am, week.wf7Am
                )
                val wfMax = listOf(
                    week.wf1Pm, week.wf2Pm, week.wf3Pm,
                    week.wf4Pm, week.wf5Pm, week.wf6Pm, week.wf7Pm
                )
                val taMin = listOf(
                    week.taMin1, week.taMin2, week.taMin3,
                    week.taMin4, week.taMin5, week.taMin6, week.taMin7
                )
                val taMax = listOf(
                    week.taMax1, week.taMax2, week.taMax3, week.taMax4,
                    week.taMax5, week.taMax6, week.taMax7
                )

                // 뷰페이저 아이템 추가
                dailyWeatherList.clear()
                weeklyWeatherList.clear()

                binding.mainGpsTitleTv.text = SharedPreferenceManager(this).getString(lastAddress)
                binding.mainLiveTempValue.text = realtime.temp.roundToInt().toString() + "˚"
                binding.mainSunRiseValue.text =
                    sun.sunrise.substring(0, 2)+ ":" + sun.sunrise.substring(2, sun.sunrise.length)
                binding.mainSunSetValue.text =
                    sun.sunset.substring(0, 2) + ":" + sun.sunset.substring(2, sun.sunset.length)
                binding.mainSkyImg.setImageDrawable(getSkyImg(this, realtime.sky))
                binding.mainSkyValue.text = realtime.sky
                binding.mainHumidValue.text = realtime.humid.roundToInt().toString() + "%"
                binding.mainWindValue.text = realtime.windSpeed.roundToInt().toString() + "m/s, " + realtime.vector
                binding.mainRainPerValue.text = realtime.rainP.roundToInt().toString() + "%"
                settingSpan(this, binding.mainPm10Grade, air.pm10Grade)
                settingSpan(this, binding.mainPm2p5Grade, air.pm25Grade)
                binding.mainMinTemp.text = "${filteringNullData(week.taMin0)}˚"
                binding.mainMaxTemp.text = "${filteringNullData(week.taMax0)}˚"

                for (i: Int in 0 until (10)) {
                    val today = result.realtime[i]
                    val forecastToday = LocalDateTime.parse(today.forecast)
                    addDailyWeatherItem(
                        forecastToday.hour.toString() + "시",
                        getSkyImg(this, today.sky)!!,
                        "${today.temp.roundToInt()}˚",
                        "${forecastToday.monthValue}.${forecastToday.dayOfMonth}"
                    )
                }

                for (i: Int in 0 until (7)) {
                    addWeeklyWeatherItem(
                        "${tempDate.month.value}.${tempDate.dayOfMonth + i}" +
                                "(${convertDayOfWeekToKorean(this,tempDate.dayOfWeek.value + i)})",
                        getSkyImg(this, wfMin[i])!!,
                        getSkyImg(this, wfMax[i])!!,
                        "${taMin[i].roundToInt()}˚",
                        "${taMax[i].roundToInt()}˚"
                    )
                }

                weeklyWeatherAdapter.notifyDataSetChanged()
                dailyWeatherAdapter.notifyDataSetChanged()

                runOnUiThread {
                    binding.mainPbLayout.visibility = View.GONE
                }
            }
        }
    }

    // 필드값이 없을 때 -100 출력 됨
    private fun filteringNullData(data: Double): String {
        return if (data != -100.0) data.roundToInt().toString() else ""
    }

    // 시간별 날씨 리사이클러뷰 아이템 추가
    private fun addDailyWeatherItem(time: String, img: Drawable, value: String, date: String) {
        val item = AdapterModel.DailyWeatherItem(time, img, value, date)

        this.dailyWeatherList.add(item)
    }

    // 시간별 날씨 리사이클러뷰 아이템 추가
    private fun addWeeklyWeatherItem(
        day: String, minImg: Drawable,
        maxImg: Drawable, minText: String, maxText: String
    ) {
        val item = AdapterModel.WeeklyWeatherItem(day, minImg, maxImg, minText, maxText)

        this.weeklyWeatherList.add(item)
    }

    private fun addSkeletonItem() {
        val itemDaily = AdapterModel.DailyWeatherItem("",null,"","")
        val itemWeekly = AdapterModel.WeeklyWeatherItem("", null, null, "", "")

        for (i: Int in 0..7) {
            this.weeklyWeatherList.add(itemWeekly)
            this.dailyWeatherList.add(itemDaily)
        }
    }
}