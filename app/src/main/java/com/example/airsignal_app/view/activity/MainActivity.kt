package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.airsignal_app.R
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
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.gps.GpsWorker
import com.example.airsignal_app.login.SilentLoginClass
import com.example.airsignal_app.util.*
import com.example.airsignal_app.util.ConvertDataType.convertDayOfWeekToKorean
import com.example.airsignal_app.util.ConvertDataType.getDataColor
import com.example.airsignal_app.util.ConvertDataType.getRainType
import com.example.airsignal_app.util.ConvertDataType.getSkyImg
import com.example.airsignal_app.view.*
import com.example.airsignal_app.view.widget.WidgetProvider
import com.example.airsignal_app.vmodel.GetWeatherViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.w3c.dom.Text
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private var isBackPressed = false
    private val getDataViewModel by viewModel<GetWeatherViewModel>()
    private val dailyWeatherList = ArrayList<AdapterModel.DailyWeatherItem>()
    private val weeklyWeatherList = ArrayList<AdapterModel.WeeklyWeatherItem>()
    private val airQualityList = ArrayList<AdapterModel.AirQualityItem>()
    private val airQualityAdapter by lazy { AirQualityAdapter(this, airQualityList) }
    private val dailyWeatherAdapter by lazy { DailyWeatherAdapter(this, dailyWeatherList) }
    private val weeklyWeatherAdapter by lazy { WeeklyWeatherAdapter(this, weeklyWeatherList) }
    private val sp by lazy { SharedPreferenceManager(this) }
    private val db by lazy { GpsRepository(this).getInstance() }
    private val UPDATE_TIME = "com.example.airsignal_app.action.UPDATE_DATA"

    override fun onResume() {
        super.onResume()
        Logger.t(TAG_L).d("onResume")
        getDataSingleTime()
//        binding.mainBottomAdView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
//        binding.mainBottomAdView.destroy()
    }

    override fun onPause() {
        super.onPause()
//        binding.mainBottomAdView.pause()
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

        val anim = AnimationUtils.loadAnimation(this,R.anim.bottom_arrow_anim)
        binding.mainMotionSLideImg.startAnimation(anim)

        //findViewById or Binding for your SegmentedProgressBar
        binding.segmentProgress2p5Bar.apply {
            setContexts(
                barContexts = listOf(
                    SegmentedProgressBar.BarContext(
                        0.35f //percentage for segment
                    ),
                    SegmentedProgressBar.BarContext(
                        0.19f
                    ),
                    SegmentedProgressBar.BarContext(
                        0.16f
                    ),
                    SegmentedProgressBar.BarContext(
                        0.30f
                    )
                )
            )
        }
        //findViewById or Binding for your SegmentedProgressBar
        binding.segmentProgress10Bar.apply {
            setContexts(
                barContexts = listOf(
                    SegmentedProgressBar.BarContext(
                        0.35f //percentage for segment
                    ),
                    SegmentedProgressBar.BarContext(
                        0.19f
                    ),
                    SegmentedProgressBar.BarContext(
                        0.16f
                    ),
                    SegmentedProgressBar.BarContext(
                        0.30f
                    )
                )
            )
        }

//        // TEST NOTIFICATION
//        /////////////////////////////////////////////////////////////////
//        val intent = Intent(applicationContext, MainActivity::class.java)
//        val pmString = "미세먼지 나쁨"
//        pmString.toSpannable().setSpan(
//            ForegroundColorSpan(Color.RED),
//            5, pmString.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
//        )
//        val location = GpsRepository(this).getInstance().findById(CURRENT_GPS_ID).addr.toString()
//        location.toSpannable().setSpan(
//            android.text.style.AbsoluteSizeSpan(18),
//            0,
//            location.length,
//            Spannable.SPAN_INCLUSIVE_INCLUSIVE
//        )
//        val data = "최고: 24˚ 최저 : 10˚"
//        NotificationBuilder().sendNotification(
//            this, intent,
//            data, location, getCurrentTime()
//        )
//        ///////////////////////////////////////////////////////////////

            initializing()
//
//        onUpdateWidgetData()
//
            SilentLoginClass().login(this, binding.mainMotionLayout)

            binding.mainGpsTitleTv.setOnClickListener {
                val bottomSheet =
                    SearchDialog(this, 0, supportFragmentManager, BottomSheetDialogFragment().tag)
                bottomSheet.show(0)

//                val intent = Intent(this@MainActivity, SettingActivity::class.java)
//                startActivity(intent)
            }
//
            binding.mainGpsFix.setOnClickListener {
                showPB()
                GetLocation(this).getLocation()
                Handler(Looper.getMainLooper()).postDelayed({
                    loadCurrentAddr()
                    hidePB()
                }, 1500)
            }

        binding.mainSideMenuIv.setOnClickListener {
            val menu: View =
                LayoutInflater.from(this).inflate(R.layout.side_menu, null)
            val cancel = menu.findViewById<ImageView>(R.id.headerCancel)
            val profile = menu.findViewById<ImageView>(R.id.navHeaderProfileImg)
            val id = menu.findViewById<TextView>(R.id.navHeaderUserId)
            val weather = menu.findViewById<TextView>(R.id.navMenuWeather)
            val setting = menu.findViewById<TextView>(R.id.navMenuSetting)
            val headerTr = menu.findViewById<TableRow>(R.id.headerTr)

            val dialog = SideMenuBuilder().getInstance(this)
            dialog.apply {
                setBackPressed(cancel)
                setUserData(profile, id)
                show(menu, true)
            }

            headerTr.setOnClickListener {
                if (sp.getString(IgnoredKeyFile.lastLoginPlatform) == "")
                    EnterPage(this).toLogin()
            }
            weather.setOnClickListener {
                dialog.dismiss()
            }
            setting.setOnClickListener {
                val intent = Intent(this@MainActivity, SettingActivity::class.java)
                startActivity(intent)
            }
        }

//        val refreshLayout = findViewById<View>(R.id.mainSwipeLayout) as RefreshLayout
//        refreshLayout.apply {
//            setRefreshHeader(BezierRadarHeader(this@MainActivity), LayoutParams.MATCH_PARENT, 170)
//            setPrimaryColors(Color.TRANSPARENT)
////            setRefreshFooter(ClassicsFooter(this@MainActivity))
//            setFinishOnTouchOutside(false)
//            setOnRefreshListener {
//                Handler(Looper.getMainLooper()).postDelayed({
//                    getDataSingleTime()
//                    it.finishRefresh()
//                }, 2000)
//            }
//        }
    }

    private fun getDataSingleTime() {
        if (RequestPermissionsUtil(this).isLocationPermitted()) {
            val addrArray = resources.getStringArray(R.array.address)
            if (addrArray.contains(sp.getString(lastAddress))) {
                loadSavedAddr()
            } else {
                loadCurrentAddr()
            }
        }
    }

    private fun loadCurrentAddr(): GpsEntity {
        val dbCurrent = db.findById(CURRENT_GPS_ID)
        try {
            getDataViewModel.loadDataResult(
                dbCurrent.lat,
                dbCurrent.lng,
                null
            )
            val formatAddress = dbCurrent.addr!!.replace("null", "")
            sp.setString(lastAddress, formatAddress)
            Logger.t(TAG_D)
                .d("${dbCurrent.lat},${dbCurrent.lng}")
            binding.mainGpsTitleTv.text = db.findById(CURRENT_GPS_ID).addr
        } catch(e: Exception) {
            GetLocation(this).getLocation()
            Thread.sleep(1000)
            e.printStackTrace()
        }
        return dbCurrent
    }

    private fun loadSavedAddr() {
        getDataViewModel.loadDataResult(
            null,
            null,
            sp.getString(lastAddress)
        )
        Logger.t(TAG_D).d(sp.getString(lastAddress))
        binding.mainGpsTitleTv.text = sp.getString(lastAddress)
    }

    private fun showPB() {
        binding.mainMotionLayout.alpha = 0.7f
    }

    private fun hidePB() {
        binding.mainMotionLayout.alpha = 1f
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun initializing() {
//        addSkeletonItem()
        GetLocation(this).getLocation()

        // 워크 매니저 생성
        CoroutineScope(Dispatchers.Default).launch { createWorkManager() }

        binding.mainDailyWeatherRv.adapter = dailyWeatherAdapter
        binding.mainWeeklyWeatherRv.adapter = weeklyWeatherAdapter
//        binding.mainAirQualityRv.adapter = airQualityAdapter
//
//        AdViewClass(this).loadAdView(binding.mainBottomAdView)
    }
//
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
//        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
//            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
//        } else {
            if (!isBackPressed) {
                ToastUtils(this)
                    .showMessage(getString(R.string.back_press), 2)
                isBackPressed = true
            } else {
                sp.removeKey(lastAddress)
                EnterPage(this).fullyExit()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                isBackPressed = false
            }, 2000)
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    fun applyGetDataViewModel(): MainActivity {
        getDataViewModel.getDataResult().observe(this) {
            it?.let { result ->
                val realtime = result.realtime[0]
                val sun = result.sun
                val air = result.quality
                val week = result.week
                val today = result.today
                val yesterday = result.yesterday
                val dateNow: LocalDateTime = LocalDateTime.now()
                var tempDate: LocalDateTime
//                try {
//                    tempDate = LocalDateTime.parse(week.tempDate)
//                } catch (e: java.lang.NullPointerException) {
//                    tempDate = LocalDateTime.now()
//                    e.printStackTrace()
//                }
                val wfMin = listOf(
                    week.wf0Am,week.wf1Am, week.wf2Am, week.wf3Am,
                    week.wf4Am, week.wf5Am, week.wf6Am, week.wf7Am
                )
                val wfMax = listOf(
                    week.wf0Pm,week.wf1Pm, week.wf2Pm, week.wf3Pm,
                    week.wf4Pm, week.wf5Pm, week.wf6Pm, week.wf7Pm
                )
                val taMin = listOf(
                    week.taMin0, week.taMin1, week.taMin2, week.taMin3,
                    week.taMin4, week.taMin5, week.taMin6, week.taMin7
                )
                val taMax = listOf(
                    week.taMax0, week.taMax1, week.taMax2, week.taMax3, week.taMax4,
                    week.taMax5, week.taMax6, week.taMax7
                )

//                // 뷰페이저 아이템 추가
                dailyWeatherList.clear()
                weeklyWeatherList.clear()
//                airQualityList.clear()

                binding.mainLiveTempValue.text = realtime.temp.roundToInt().toString()
//                binding.mainSunRiseValue.text =
//                    sun.sunrise.substring(0, 2) + ":" + sun.sunrise.substring(2, sun.sunrise.length)
//                binding.mainSunSetValue.text =
//                    sun.sunset.substring(0, 2) + ":" + sun.sunset.substring(2, sun.sunset.length)
                binding.mainSkyImg.setImageDrawable(applySkyImg(realtime.rainType, realtime.sky))
                binding.mainSkyText.text = realtime.sky
//                binding.mainSensibleValue.text =
//                    "${getString(R.string.sens_temp)} : ${
//                        SensibleTempFormula().getSensibleTemp(
//                            realtime.temp,
//                            realtime.humid,
//                            realtime.windSpeed
//                        ).toInt()
//                    }˚"
//                binding.mainHumidValue.text = realtime.humid.roundToInt().toString() + "%"
//                binding.mainWindValue.text =
//                    realtime.windSpeed.roundToInt().toString() + "m/s, " + realtime.vector
//                binding.mainRainPerValue.text = realtime.rainP.roundToInt().toString() + "%"
                binding.mainPm10Grade.setGradeText((air.pm10Grade - 1).toString())
                binding.mainPm2p5Grade.setGradeText((air.pm25Grade - 1).toString())
                binding.mainMinMaxMin.text = "${filteringNullData(today.min)}˚"
                binding.mainMinMaxMax.text = "${filteringNullData(today.max)}˚"
                binding.nestedPm10Grade.setGradeText((air.pm10Grade - 1).toString())
                binding.nestedPm2p5Grade.setGradeText((air.pm25Grade - 1).toString())
                binding.nestedPm10Value.setTextColor(getDataColor(this,air.pm10Grade - 1))
                binding.nestedPm10Value.text = air.pm10Value.toInt().toString()
                binding.nestedPm2p5Value.setTextColor(getDataColor(this,air.pm25Grade - 1))
                binding.nestedPm2p5Value.text = air.pm25Value.toString()
                getCompareTemp(yesterday.temp, realtime.temp, binding.mainCompareTempIv, binding.mainCompareTempTv)

                for (i: Int in 0 until result.realtime.size) {
                    try {
                        if (i == result.realtime.lastIndex + 1) {
                            break
                        } else {
                            val dailyIndex = result.realtime[i]
                            val forecastToday = LocalDateTime.parse(dailyIndex.forecast)
                            addDailyWeatherItem(
                                "${forecastToday.hour}${getString(R.string.hour)}",
                                applySkyImg(dailyIndex.rainType, dailyIndex.sky),
                                "${dailyIndex.temp.roundToInt()}˚",
                                "${forecastToday.monthValue}.${forecastToday.dayOfMonth}"
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                for (i: Int in 0 until (8)) {
                    try {
                        addWeeklyWeatherItem(
                            "${dateNow.month.value}.${dateNow.dayOfMonth + i}" +
                                    "(${convertDayOfWeekToKorean(this, dateNow.dayOfWeek.value + i)})",
                            getSkyImg(this, wfMin[i])!!,
                            getSkyImg(this, wfMax[i])!!,
                            "${taMin[i].roundToInt()}˚",
                            "${taMax[i].roundToInt()}˚"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
//
//                addAirQualityItem(getString(R.string.cqi), air.khaiValue.toString())
//                addAirQualityItem(getString(R.string.pm_10), air.pm10Value.toInt().toString())
//                addAirQualityItem(getString(R.string.pm_2p5), air.pm25Value.toString())
//                addAirQualityItem(getString(R.string.co), air.coValue.toString())
//                addAirQualityItem(getString(R.string.o3), air.o3Value.toString())
//                addAirQualityItem(getString(R.string.so2), air.so2Value.toString())

                weeklyWeatherAdapter.notifyDataSetChanged()
                dailyWeatherAdapter.notifyDataSetChanged()
//                airQualityAdapter.notifyDataSetChanged()

                runOnUiThread {
                    hidePB()
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
        maxImg: Drawable, minText: String, maxText: String,
    ) {
        val item = AdapterModel.WeeklyWeatherItem(day, minImg, maxImg, minText, maxText)

        this.weeklyWeatherList.add(item)
    }

    // 로딩시 빈 아이템 보여줌
    private fun addSkeletonItem() {
        val itemDaily = AdapterModel.DailyWeatherItem("", null, "", "")
        val itemWeekly = AdapterModel.WeeklyWeatherItem("", null, null, "", "")

        for (i: Int in 0..7) {
            this.weeklyWeatherList.add(itemWeekly)
            this.dailyWeatherList.add(itemDaily)
        }
    }

    // 공기질 데이터 아이템 추가
    private fun addAirQualityItem(title: String, data: String) {
        val item = AdapterModel.AirQualityItem(title, data)
        this.airQualityList.add(item)
    }

    // 위젯 데이터 갱신
    private fun onUpdateWidgetData() {
        sendBroadcast(Intent(UPDATE_TIME).apply {
            component = ComponentName(this@MainActivity, WidgetProvider::class.java)
        })
    }

    // 강수형태가 없으면 하늘상태 있으면 강수형태 - 텍스트
    private fun applySkyText(rain: String?, sky: String?): String {
        return if (rain != "없음") {
            rain!!
        } else {
            sky!!
        }
    }

    // 강수형태가 없으면 하늘상태 있으면 강수형태 - 이미지
    private fun applySkyImg(rain: String?, sky: String?): Drawable {
        return if (rain != "없음") {
            getRainType(this, rain!!)!!
        } else {
            getSkyImg(this, sky!!)!!
        }
    }

    //어제와 기온 비교
    private fun getCompareTemp(yesterday: Double, today: Double, iv: ImageView, tv: TextView) {
        if (yesterday > today) {
            tv.visibility = View.VISIBLE
            iv.visibility = View.VISIBLE
            tv.setTextColor(ResourcesCompat.getColor(resources,R.color.red,null))
            iv.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.arrow_down,null))
            tv.text = "${(yesterday - today).roundToInt().absoluteValue}˚"
        } else if (today > yesterday) {
            tv.visibility = View.VISIBLE
            iv.visibility = View.VISIBLE
            iv.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.arrow_up,null))
            tv.text = "${(today-yesterday).roundToInt().absoluteValue}˚"
            tv.setTextColor(Color.parseColor("#61FF00"))
        } else {
            iv.visibility = View.GONE
            tv.visibility = View.GONE
        }
    }
}