package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.AddressListAdapter
import com.example.airsignal_app.adapter.AirQualityAdapter
import com.example.airsignal_app.adapter.DailyWeatherAdapter
import com.example.airsignal_app.adapter.WeeklyWeatherAdapter
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.StaticDataObject.CHECK_GPS_BACKGROUND
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.dao.StaticDataObject.TAG_L
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.GpsRepository
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.gps.GpsWorker
import com.example.airsignal_app.util.*
import com.example.airsignal_app.util.ConvertDataType.convertDayOfWeekToKorean
import com.example.airsignal_app.util.ConvertDataType.getSkyImg
import com.example.airsignal_app.view.SearchDialog
import com.example.airsignal_app.view.SideMenuClass
import com.example.airsignal_app.vmodel.GetWeatherViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.header.BezierRadarHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var isBackPressed = false
    private val getDataViewModel by viewModel<GetWeatherViewModel>()
    private val dailyWeatherList = ArrayList<AdapterModel.DailyWeatherItem>()
    private val weeklyWeatherList = ArrayList<AdapterModel.WeeklyWeatherItem>()
    private val airQualityList = ArrayList<AdapterModel.AirQualityItem>()
    private val airQualityAdapter by lazy { AirQualityAdapter(this, airQualityList)}
    private val dailyWeatherAdapter by lazy { DailyWeatherAdapter(this, dailyWeatherList) }
    private val weeklyWeatherAdapter by lazy { WeeklyWeatherAdapter(this, weeklyWeatherList) }

    override fun onResume() {
        super.onResume()
        getDataSingleTime()
        Logger.t(TAG_L).d("onResume")
    }

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

        CompletableFuture.supplyAsync {
            GetLocation(this@MainActivity).getLocation()
        }.thenAccept {
            initializing()
        }

        // 사이드 메뉴 세팅
        SideMenuClass(this, binding.mainDrawerLayout, binding.mainNavView, binding.mainLayout)
            .setUpSideMenu(binding.mainSideMenuIv, binding.mainPb)

        binding.mainGpsTitleTv.setOnClickListener {
            val bottomSheet = SearchDialog(0,supportFragmentManager, BottomSheetDialogFragment().tag)
            bottomSheet.show(0)
        }

        val refreshLayout = findViewById<View>(R.id.mainSwipeLayout) as RefreshLayout
        refreshLayout.apply {
            setRefreshHeader(BezierRadarHeader(this@MainActivity))
//            setRefreshFooter(ClassicsFooter(this@MainActivity))
            setFinishOnTouchOutside(false)
            setOnRefreshListener {
                it.finishRefresh(2000)
                Handler(Looper.getMainLooper()).postDelayed ({
                    getDataSingleTime()
                },2000)
            }
        }
    }

    private fun getDataSingleTime() {
        val db = GpsRepository(this)
        println(db.findById(CURRENT_GPS_ID))
        if (SharedPreferenceManager(this).getString(lastAddress) == db.findById(CURRENT_GPS_ID).addr
            || SharedPreferenceManager(this).getString(lastAddress) == "") {
            getDataViewModel.loadDataResult(
                db.findById(CURRENT_GPS_ID).lat!!,
                db.findById(CURRENT_GPS_ID).lng!!,
                null
            )
            Logger.t(TAG_D)
                .d("${db.findById(CURRENT_GPS_ID).lat},${db.findById(CURRENT_GPS_ID).lng}")

            binding.mainGpsTitleTv.text = db.findById(CURRENT_GPS_ID).addr

        } else {
            getDataViewModel.loadDataResult(
                null,
                null,
                SharedPreferenceManager(this).getString(lastAddress)
            )
            Logger.t(TAG_D).d(SharedPreferenceManager(this).getString(lastAddress))
            binding.mainGpsTitleTv.text = SharedPreferenceManager(this).getString(lastAddress)
        }
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
        binding.mainAirQualityRv.adapter = airQualityAdapter
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
                SharedPreferenceManager(this).removeKey(lastAddress)
                EnterPage(this).fullyExit()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                isBackPressed = false
            }, 2000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    fun applyGetDataViewModel() : MainActivity {
        getDataViewModel.getDataResult().observe(this) {
            it?.let { result ->
                val realtime = result.realtime[0]
                val sun = result.sun
                val air = result.quality
                val week = result.week
                val tempDate = LocalDateTime.parse(week.tempDate)
//                val tempDate = week.tempDate
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
                airQualityList.clear()

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
                binding.mainPm10Grade.setGradeText((air.pm10Grade-1).toString())
                binding.mainPm2p5Grade.setGradeText((air.pm25Grade-1).toString())
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
//                        tempDate
                        "${tempDate.month.value}.${tempDate.dayOfMonth + i}" +
                                "(${convertDayOfWeekToKorean(this, tempDate.dayOfWeek.value + i)})",
                        getSkyImg(this, wfMin[i])!!,
                        getSkyImg(this, wfMax[i])!!,
                        "${taMin[i].roundToInt()}˚",
                        "${taMax[i].roundToInt()}˚"
                    )
                }

                addAirQualityItem("통합 대기\n환경수치",air.khaiValue.toString())
                addAirQualityItem("미세먼지",air.pm10Value.toInt().toString())
                addAirQualityItem("초미세먼지",air.pm25Value.toString())
                addAirQualityItem("일산화탄소",air.coValue.toString())
                addAirQualityItem("오존",air.o3Value.toString())
                addAirQualityItem("이산화황",air.so2Value.toString())

                weeklyWeatherAdapter.notifyDataSetChanged()
                dailyWeatherAdapter.notifyDataSetChanged()
                airQualityAdapter.notifyDataSetChanged()

                runOnUiThread {
                    binding.mainPbLayout.visibility = View.GONE
                }
            }
        }
        return this
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

    private fun addAirQualityItem(title: String, data: String) {
        val item = AdapterModel.AirQualityItem(title, data)
        this.airQualityList.add(item)
    }
}