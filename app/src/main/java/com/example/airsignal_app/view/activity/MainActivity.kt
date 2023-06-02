package com.example.airsignal_app.view.activity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.work.*
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.DailyWeatherAdapter
import com.example.airsignal_app.adapter.UVLegendAdapter
import com.example.airsignal_app.adapter.UVResponseAdapter
import com.example.airsignal_app.adapter.WeeklyWeatherAdapter
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.dao.StaticDataObject.TAG_L
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.login.SilentLoginClass
import com.example.airsignal_app.util.*
import com.example.airsignal_app.util.ConvertDataType.convertDayOfWeekToKorean
import com.example.airsignal_app.util.ConvertDataType.convertTimeToMinutes
import com.example.airsignal_app.util.ConvertDataType.getCurrentTime
import com.example.airsignal_app.util.ConvertDataType.getDataColor
import com.example.airsignal_app.util.ConvertDataType.getRainType
import com.example.airsignal_app.util.ConvertDataType.getSkyImg
import com.example.airsignal_app.util.ConvertDataType.millsToString
import com.example.airsignal_app.util.ConvertDataType.pixelToDp
import com.example.airsignal_app.view.*
import com.example.airsignal_app.view.widget.WidgetProvider
import com.example.airsignal_app.vmodel.GetWeatherViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private var isBackPressed = false
    private val getDataViewModel by viewModel<GetWeatherViewModel>()
    private val dailyWeatherList = ArrayList<AdapterModel.DailyWeatherItem>()
    private val weeklyWeatherList = ArrayList<AdapterModel.WeeklyWeatherItem>()
    private val dailyWeatherAdapter by lazy { DailyWeatherAdapter(this, dailyWeatherList) }
    private val weeklyWeatherAdapter by lazy { WeeklyWeatherAdapter(this, weeklyWeatherList) }
    private val sp by lazy { SharedPreferenceManager(this) }
    private val UPDATE_TIME = "com.example.airsignal_app.action.UPDATE_DATA"
    private var isInit = true
    private val SHOWING_LOADING_FLOAT = 0.5f
    private val NOT_SHOWING_LOADING_FLOAT = 1f
    private val locationClass by lazy { GetLocation(this) }
    private val uvLegendList = ArrayList<AdapterModel.UVLegendItem>()
    private val uvLegendAdapter = UVLegendAdapter(this, uvLegendList)
    private val uvResponseList = ArrayList<AdapterModel.UVResponseItem>()
    private val uvResponseAdapter = UVResponseAdapter(this,uvResponseList)

    override fun onResume() {
        super.onResume()
        Logger.t(TAG_L).d("onResume")
        showPB()
        getDataSingleTime()
        if (isInit)
            isInit = false
        Thread.sleep(100)
//        binding.mainBottomAdView.resume()
    }

//    override fun onDestroy() {
//        super.onDestroy()
////        binding.mainBottomAdView.destroy()
//    }
//
//    override fun onPause() {
//        super.onPause()
////        binding.mainBottomAdView.pause()
//    }

    @SuppressLint("ClickableViewAccessibility")
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

        // 하단 스크롤시 네비게이션 바 색상 하얀색으로 변경
        binding.nestedScrollview.setOnScrollChangeListener { view, _, _, _, _ ->
            if (view.scrollY == 0) {
                window.navigationBarColor = getColor(android.R.color.transparent)
            } else {
                window.navigationBarColor = getColor(R.color.white)
            }
        }

        val bottomArrowAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_arrow_anim)
        binding.mainMotionSLideImg.startAnimation(bottomArrowAnim)

        //findViewById or Binding for your SegmentedProgressBar
        val array2P5 = floatArrayOf(0.12f,0.24f,0.48f,0.16f)
        val array10 = floatArrayOf(0.15f,0.25f,0.35f,0.25f)
        drawingPmGraph(binding.segmentProgress2p5Bar,array2P5)
        drawingPmGraph(binding.segmentProgress10Bar,array10)

        addUvLegendItem(0,"0 - 2", getColor(R.color.uv_low),"낮음")
        addUvLegendItem(1,"3 - 5", getColor(R.color.uv_normal),"보통")
        addUvLegendItem(2,"6 - 7", getColor(R.color.uv_high),"높음")
        addUvLegendItem(3,"8 - 10", getColor(R.color.uv_very_high),"매우\n높음")
        addUvLegendItem(4,"11 - ", getColor(R.color.uv_caution),"위험")

        binding.seekArc.setOnTouchListener { _, _ -> true } // 자외선 그래프 클릭 방지


        // 자외선 지수 접고 펴기 화살표
        binding.mainUvCollapseArrow.apply {
            this.setOnClickListener {
                if (binding.mainUvCollapsedLayout.visibility == VISIBLE) {

                    binding.mainUvCollapseArrow.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.btn_down,
                            null
                        )
                    )
                    binding.mainUvCollapsedLayout.apply {
                        this.visibility = GONE
                        this.clearFocus()
                    }
                } else {
                    binding.mainUvCollapseArrow.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.btn_up,
                            null
                        )
                    )
                    binding.mainUvCollapsedLayout.apply {
                        this.visibility = VISIBLE
                        this.requestFocus()
                    }
                }
            }
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

//        onUpdateWidgetData()

        initializing()

        // 플러스 모양 추가시 주소등록 다이얼로그
        binding.mainAddAddress.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                val bottomSheet =
                    SearchDialog(
                        this@MainActivity,
                        0,
                        supportFragmentManager,
                        BottomSheetDialogFragment().tag
                    )
                bottomSheet.show(0)
            }
        })

        // 현재 주소로 갱신
        binding.mainGpsFix.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (RequestPermissionsUtil(this@MainActivity).isLocationPermitted()) {
                    showPB()
                    getCurrentLocation()
                } else {
                    RequestPermissionsUtil(this@MainActivity).requestLocation()
                }
            }
        })

        // 사이드 메뉴 세팅
        binding.mainSideMenuIv.setOnClickListener(object : OnSingleClickListener() {
            @SuppressLint("InflateParams")
            override fun onSingleClick(v: View?) {
                val menu: View =
                    LayoutInflater.from(this@MainActivity).inflate(R.layout.side_menu, null)
                val cancel = menu.findViewById<ImageView>(R.id.headerCancel)
                val profile = menu.findViewById<ImageView>(R.id.navHeaderProfileImg)
                val id = menu.findViewById<TextView>(R.id.navHeaderUserId)
                val weather = menu.findViewById<TextView>(R.id.navMenuWeather)
                val setting = menu.findViewById<TextView>(R.id.navMenuSetting)
                val headerTr = menu.findViewById<TableRow>(R.id.headerTr)

                val dialog = SideMenuBuilder(this@MainActivity)
                dialog.apply {
                    setBackPressed(cancel)
                    setUserData(profile, id)
                    show(menu, true)
                }

                headerTr.setOnClickListener {
                    if (sp.getString(IgnoredKeyFile.lastLoginPlatform) == "")
                        EnterPage(this@MainActivity).toLogin()
                }
                weather.setOnClickListener {
                    dialog.dismiss()
                }
                setting.setOnClickListener {
                    CompletableFuture.supplyAsync {
                        dialog.dismiss()
                    }.thenAccept {
                        val intent = Intent(this@MainActivity, SettingActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        })
    }

    // 날씨 데이터 API 호출
    private fun getDataSingleTime() {
        if (RequestPermissionsUtil(this).isLocationPermitted()) {
            val addrArray = resources.getStringArray(R.array.address)
            if (addrArray.contains(sp.getString(lastAddress))) {
                loadSavedAddr()
                // TimeOut
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isProgressed()) {
                        hidePB()
                        Toast.makeText(this, "통신에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                }, 1000 * 5)
            } else {
                getCurrentLocation()
                // TimeOut
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isProgressed()) {
                        hidePB()
                        Toast.makeText(this, "통신에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                }, 1000 * 5)
            }
        }
    }

    // 저장된 주소로 데이터 호출
    private fun loadSavedAddr() {
        getDataViewModel.loadDataResult(
            null,
            null,
            sp.getString(lastAddress)
        )

        binding.mainGpsTitleTv.text = sp.getString(lastAddress)
        binding.mainTopBarGpsTitle.text = sp.getString(lastAddress)
    }

    private fun showPB() {
        if (binding.mainMotionLayout.alpha == NOT_SHOWING_LOADING_FLOAT) {
            binding.mainMotionLayout.alpha = SHOWING_LOADING_FLOAT
            binding.mainMotionLayout.isEnabled = false
        }
    }

    private fun hidePB() {
        if (binding.mainMotionLayout.alpha == SHOWING_LOADING_FLOAT) {
            binding.mainMotionLayout.alpha = NOT_SHOWING_LOADING_FLOAT
            binding.mainMotionLayout.isEnabled = true
        }
    }

    private fun isProgressed(): Boolean {
        return binding.mainMotionLayout.alpha == SHOWING_LOADING_FLOAT
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initializing() {
        // 자동 로그인
        SilentLoginClass().login(this@MainActivity, binding.mainMotionLayout)

        binding.mainDailyWeatherRv.adapter = dailyWeatherAdapter
        binding.mainWeeklyWeatherRv.adapter = weeklyWeatherAdapter
        binding.mainUVLegendRv.adapter = uvLegendAdapter
        binding.mainUvCollapseRv.adapter = uvResponseAdapter

        binding.mainUVLegendRv.isClickable = false
        binding.mainUvCollapseRv.isClickable = false

        createWorkManager()        // 워크 매니저 생성
//        AdViewClass(this).loadAdView(binding.mainBottomAdView)
    }

    private fun createWorkManager() {
        GetLocation(this).getGpsInBackground()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
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
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    fun applyGetDataViewModel(): MainActivity {
        getDataViewModel.getDataResult().observe(this) {
            it?.let { result ->
                val realtime = result.realtime[0]
                val sun = result.sun
                val sunTomorrow = result.sun_tomorrow
                val air = result.quality
                val week = result.week
                val today = result.today
                val uv = result.uv
                val yesterday = result.yesterday
                val dateNow: LocalDateTime = LocalDateTime.now()
                val current = result.current

                applyWindowBackground(applySkyText(realtime.rainType, realtime.sky))

                val wfMin = listOf(
                    week.wf0Am, week.wf1Am, week.wf2Am, week.wf3Am,
                    week.wf4Am, week.wf5Am, week.wf6Am, week.wf7Am
                )
                val wfMax = listOf(
                    week.wf0Pm, week.wf1Pm, week.wf2Pm, week.wf3Pm,
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

                dailyWeatherList.clear()
                weeklyWeatherList.clear()

                binding.mainLiveTempValue.text = current.temperature.absoluteValue.toString()

                applyUvResponseItem(uv.flag)      // 자외선 단계별 대응요령 추가

                if (current.temperature < 0) {
                    binding.mainLiveTempMinus.visibility = VISIBLE
                } else {
                    binding.mainLiveTempMinus.visibility = GONE
                }
                binding.mainSkyImg.setImageDrawable(applySkyImg(realtime.rainType, realtime.sky))
                binding.mainSkyText.text = applySkyText(realtime.rainType, realtime.sky)
//                binding.mainSensibleValue.text =
//                    "${getString(R.string.sens_temp)} : ${
//                        SensibleTempFormula().getSensibleTemp(
//                            realtime.temp,
//                            realtime.humid,
//                            realtime.windSpeed
//                        ).toInt()
//                    }˚"
//                spanUnit(binding.subWindValue, current.windSpeed.toString() + " ㎧")
//                spanUnit(binding.subRainPerValue, realtime.rainP.roundToInt().toString() + " %")
//                spanUnit(binding.subHumidValue, current.humidity.roundToInt().toString() + " %")
//
//                binding.subWindValue.setCompoundDrawablesWithIntrinsicBounds(
//                    ResourcesCompat.getDrawable(resources, R.drawable.gps, null), null, null, null
//                )
//                binding.mainPm10Grade.getPM10GradeFromValue(air.pm10Value.toInt())
//                binding.mainPm2p5Grade.getPM25GradeFromValue(air.pm25Value)
                binding.mainMinMaxValue.text =
                    "${filteringNullData(today.min)}˚/${filteringNullData(today.max)}˚"
                binding.nestedPm10Grade.getPM10GradeFromValue(air.pm10Value.toInt())
                binding.nestedPm2p5Grade.getPM25GradeFromValue(air.pm25Value)
                binding.nestedPm10Value.setIndexTextAsInt(air.pm10Value.toFloat())
                binding.nestedPm2p5Value.setIndexTextAsInt(air.pm25Value.toFloat())

                binding.mainAirCOValue.apply {
                    text = air.coValue.toString()
                    setTextColor(getDataColor(this@MainActivity, air.coGrade - 1))
                }
                binding.mainAirNO2Value.apply {
                    text = air.no2Value.toString()
                    setTextColor(getDataColor(this@MainActivity, air.no2Grade - 1))
                }
                binding.mainAirO3Value.apply {
                    text = air.o3Value.toString()
                    setTextColor(getDataColor(this@MainActivity, air.o3Grade - 1))
                }
                binding.mainAirSO2Value.apply {
                    text = air.so2Value.toString()
                    setTextColor(getDataColor(this@MainActivity, air.so2Grade - 1))
                }

                binding.mainUvGrade.text = uv.flag
                binding.mainUvValue.text = uv.value.toString()
                when(uv.flag) {
                    "낮음" -> binding.mainUvGrade.setTextColor(getColor(R.color.uv_low))
                    "보통" -> binding.mainUvGrade.setTextColor(getColor(R.color.uv_normal))
                    "높음" -> binding.mainUvGrade.setTextColor(getColor(R.color.uv_high))
                    "매우높음" -> binding.mainUvGrade.setTextColor(getColor(R.color.uv_very_high))
                    "위험" -> binding.mainUvGrade.setTextColor(getColor(R.color.uv_caution))
                }


                val sbRise = StringBuffer().append(sun.sunrise).insert(2, ":")
                val sbSet = StringBuffer().append(sun.sunset).insert(2, ":")
                val sbRiseTom = StringBuffer().append(sunTomorrow.sunrise).insert(2, ":")
                val sbSetTom = StringBuffer().append(sunTomorrow.sunset).insert(2, ":")
                binding.mainSunRiseTime.text = sbRise
                binding.mainSunSetTime.text = sbSet
                binding.mainSunRiseTom.text = sbRiseTom
                binding.mainSunSetTom.text = sbSetTom

                getCompareTemp(
                    yesterday.temp,
                    current.temperature,
                    binding.mainCompareTempTv
                )

                val sunsetTime = convertTimeToMinutes(sun.sunset)
                val sunriseTime = convertTimeToMinutes(sun.sunrise)
                val entireSun = sunsetTime - sunriseTime
                val currentTime = millsToString(getCurrentTime(), "HHmm")
                val currentSun =
                    100 * (convertTimeToMinutes(currentTime) - convertTimeToMinutes(sun.sunrise)) / entireSun

                if (currentSun in 0..100) {
                    binding.seekArc.progress = currentSun
                } else {
                    binding.seekArc.progress = 100
                }


                val widthDp = pixelToDp(this, binding.segmentProgress10Bar.width)
                if (air.pm25Value > 125) {
                    binding.segmentProgress2p5Arrow.setPadding(widthDp, 0, 0, 0)
                } else {
                    binding.segmentProgress2p5Arrow.setPadding(
                        air.pm25Value * (widthDp) / 125,
                        0,
                        0,
                        0
                    )
                }
                if (air.pm10Value > 200) {
                    binding.segmentProgress10Arrow.setPadding(200 * widthDp / 200, 0, 0, 0)
                } else {
                    binding.segmentProgress10Arrow.setPadding(
                        air.pm10Value.toInt() * widthDp / 200,
                        0,
                        0,
                        0
                    )
                }

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
                                convertDateAppendZero(forecastToday)
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                for (i: Int in 0 until (7)) {
                    try {
                        val formedDate = dateNow.plusDays(i.toLong())
                        val date: String = when (i) {
                            0 -> { "오늘" }
                            1 -> { "내일" }
                            else -> {
                                "${convertDayOfWeekToKorean(
                                        this,
                                        dateNow.dayOfWeek.value + i)}요일"
                            }
                        }
                        addWeeklyWeatherItem(
                            date,
                            convertDateAppendZero(formedDate),
                            getSkyImg(this, wfMin[i])!!,
                            getSkyImg(this, wfMax[i])!!,
                            "${taMin[i].roundToInt()}˚",
                            "${taMax[i].roundToInt()}˚"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                weeklyWeatherAdapter.notifyDataSetChanged()
                dailyWeatherAdapter.notifyDataSetChanged()
                changeTextColorStyle(applySkyText(realtime.rainType, realtime.sky))
            }
            runOnUiThread {
                hidePB()
            }
        }
        return this
    }

    // 하늘상태에 따라 윈도우 배경 변경
    private fun applyWindowBackground(sky: String?) {
        if (sky == "맑음") {
            window.setBackgroundDrawableResource(R.drawable.main_bg_clear)
        } else {
            window.setBackgroundDrawableResource(R.drawable.main_bg_night)
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
        day: String, date: String, minImg: Drawable,
        maxImg: Drawable, minText: String, maxText: String,
    ) {
        val item = AdapterModel.WeeklyWeatherItem(day, date, minImg, maxImg, minText, maxText)

        this.weeklyWeatherList.add(item)
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

    // 날짜가 한자리일 때 앞에 0 붙이기
    private fun convertDateAppendZero(dateTime: LocalDateTime): String {
        return if (dateTime.monthValue / 10 == 0) {
            if (dateTime.dayOfMonth / 10 == 0) {
                "0${dateTime.monthValue}.0${dateTime.dayOfMonth}"
            } else {
                "0${dateTime.monthValue}.${dateTime.dayOfMonth}"
            }
        } else {
            if (dateTime.dayOfMonth / 10 == 0) {
                "${dateTime.monthValue}.0${dateTime.dayOfMonth}"
            } else {
                "${dateTime.monthValue}.${dateTime.dayOfMonth}"
            }
        }
    }

    // 마지막 기호 크기 줄이기
    private fun spanUnit(tv: TextView, s: String) {
        val span = SpannableStringBuilder(s)
        span.setSpan(
            AbsoluteSizeSpan(35),
            s.length - 1,
            s.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv.text = span
    }

    //어제와 기온 비교
    private fun getCompareTemp(yesterday: Double, today: Double, tv: TextView) {
        if (yesterday != -100.0 && today != -100.0) {
            if (yesterday > today) {
                tv.visibility = VISIBLE
                tv.text =
                    "어제보다 ${((yesterday - today).absoluteValue * 10).roundToInt() / 10.0}˚ 낮아요"
            } else if (today > yesterday) {
                tv.visibility = VISIBLE
                tv.text =
                    "어제보다 ${((today - yesterday).absoluteValue * 10).roundToInt() / 10.0} ˚ 높아요"
            } else {
                tv.visibility = VISIBLE
                tv.text = "어제와 기온이 비슷해요"
            }
        } else {
            tv.visibility = GONE
        }
    }

    // API 호출 TimeOut Exception Class
    class CustomTimeOutException : SocketTimeoutException() {
        override fun getLocalizedMessage(): String? {
            MainActivity().hidePB()
            return super.getLocalizedMessage()
        }
    }

    // 현재 위치정보를 받아오고 데이터 갱신
    @SuppressLint("MissingPermission", "SuspiciousIndentation")
    private fun getCurrentLocation() {
        val fusedGPSLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (locationClass.isGPSConnected()) {
            fusedGPSLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    location?.let { gps ->
                        Log.d(TAG_D, "${location.latitude},${location.longitude}")
                        locationClass.getAddress(gps.latitude, gps.longitude)
                            .let { address ->
                                if (address != "Null Address") {
                                    updateCurrentAddress(
                                        gps.latitude, gps.longitude,
                                        address.replaceFirst(" ", "")
                                    )
                                    getDataViewModel.loadDataResult(
                                        gps.latitude,
                                        gps.longitude,
                                        null
                                    )

                                    locationClass.writeRdbLog(
                                        gps.latitude,
                                        gps.longitude,
                                        locationClass.formattingFullAddress(address)
                                    )
                                    binding.mainGpsTitleTv.text =
                                        locationClass.formattingFullAddress(address)
                                            .replaceFirst(" ", "")
                                    binding.mainTopBarGpsTitle.text =
                                        locationClass.formattingFullAddress(address)
                                            .replaceFirst(" ", "")
                                } else {
                                    Toast.makeText(this, "위치를 불러올 수 없습니다", Toast.LENGTH_SHORT)
                                        .show()
                                    showPB()
                                    Thread.sleep(2000)
                                    getCurrentLocation()
                                }
                            }
                    }
                }

                .addOnFailureListener {
                    RDBLogcat.writeLogCause(
                        sp.getString(userEmail),
                        "GPS 위치정보 갱신실패",
                        it.localizedMessage!!
                    )
                }
        } else if (!locationClass.isGPSConnected() && locationClass.isNetWorkConnected()) {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location?.let { loc ->
                GetLocation(this@MainActivity).getAddress(loc.latitude, loc.longitude)
                    .let { addr ->
                        updateCurrentAddress(
                            loc.latitude,
                            loc.longitude,
                            addr.replaceFirst(" ", "")
                        )
                        getDataViewModel.loadDataResult(loc.latitude, loc.longitude, null)
                        locationClass.writeRdbLog(loc.latitude, loc.longitude, addr)
                        binding.mainGpsTitleTv.text = locationClass.formattingFullAddress(addr)
                            .replaceFirst(" ", "")
                        binding.mainTopBarGpsTitle.text = locationClass.formattingFullAddress(addr)
                            .replaceFirst(" ", "")

                        hidePB()
                        ToastUtils(this@MainActivity).showMessage("현재 위치와의 오차가 존재 할 수 있습니다")
                    }
            }
        } else {
            locationClass.requestSystemGPSEnable()
        }
    }

    // 현재 위치정보로 DB 갱신
    private fun updateCurrentAddress(lat: Double, lng: Double, addr: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val roomDB = GpsRepository(this@MainActivity)
            sp.setString(lastAddress, addr)
            val model = GpsEntity()

            model.name = CURRENT_GPS_ID
            model.lat = lat
            model.lng = lng
            model.addr = addr
            if (dbIsEmpty(roomDB)) {
                roomDB.insert(model)
                Timber.tag(TAG_D)
                    .d("Insert GPS In GetLocation : " + model.id + ", " + model.name + ", " + model.addr)
            } else {
                roomDB.update(model)
                Timber.tag(TAG_D)
                    .d("Update GPS In GetLocation : " + model.id + ", " + model.name + ", " + model.addr)
            }
        }
    }

    // DB가 비어있는지 확인
    private fun dbIsEmpty(db: GpsRepository): Boolean {
        return db.findAll().isEmpty()
    }

    // 자외선 범주 아이템 추가
    private fun addUvLegendItem(index: Int, value: String, color: Int, grade: String) {
        val item = AdapterModel.UVLegendItem(value, color, grade)

        uvLegendList.add(index,item)
        uvLegendAdapter.notifyItemInserted(index)
    }

    // 자외선 단계별 대응요령 아이템 추가
    private fun addUvResponseItem(text: String) {
        val item = AdapterModel.UVResponseItem(text)

        uvResponseList.add(item)
    }

    private fun getUvArray(grade: String): Array<String> {
        return when(grade) {
            "위험" -> { resources.getStringArray(R.array.uv_caution) }
            "매우높음" -> {resources.getStringArray(R.array.uv_very_high)}
            "높음" -> {resources.getStringArray(R.array.uv_high)}
            "보통" -> {resources.getStringArray(R.array.uv_normal)}
            "낮음" -> {resources.getStringArray(R.array.uv_low)}
            else -> {resources.getStringArray(R.array.uv_none)}
        }
    }

    // 자외선 단계별 대응요령 필터링
    @SuppressLint("NotifyDataSetChanged")
    private fun applyUvResponseItem(grade: String) {
        val cautionArray = getUvArray(grade)
        cautionArray.forEach {
            addUvResponseItem(it)
        }
        uvResponseAdapter.notifyDataSetChanged()
    }

    // 메인화면 배경에 따라 텍스트의 색상을 변경
    private fun changeTextColorStyle(sky: String) {
        val changeColorTextViews = listOf(binding.mainGpsTitleTv, binding.mainLiveTempMinus,
            binding.mainLiveTempValue, binding.mainLiveTempUnit, binding.mainCompareTempTv,
        binding.mainTopBarGpsTitle,binding.mainMotionSlideGuide)
        val changeTintImageViews = listOf(binding.mainSideMenuIv, binding.mainAddAddress,
        binding.mainGpsFix, binding.mainMotionSLideImg)
        when(sky) {
            "맑음" -> {
                changeColorTextViews.forEach {
                    it.setTextColor(getColor(R.color.bg_black_color))
                }

                changeTintImageViews.forEach {
                    it.imageTintList = ColorStateList.valueOf(getColor(R.color.bg_black_color))
                }

                binding.mainSkyText.setTextColor(Color.parseColor("#FF8A48"))
                binding.mainMinMaxTitle.setTextColor(Color.parseColor("#703D3D3D"))
                binding.mainMinMaxValue.setTextColor(Color.parseColor("#703D3D3D"))
                binding.mainTopBarGpsTitle.compoundDrawables[0].setTint(getColor(R.color.bg_black_color))
            }
        }
    }

    // 미세먼지 그래프 그리기
    private fun drawingPmGraph(bar: SegmentedProgressBar, array: FloatArray) {
        if (array.size == 4) {
            bar.setContexts(
                barContexts = listOf(
                    SegmentedProgressBar.BarContext(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.progressGood,
                            null
                        ), //gradient start
                        ResourcesCompat.getColor(
                            resources,
                            R.color.progressGood,
                            null
                        ), //gradient stop
                        array[0] //percentage for segment
                    ),
                    SegmentedProgressBar.BarContext(
                        ResourcesCompat.getColor(resources, R.color.progressNormal, null),
                        ResourcesCompat.getColor(resources, R.color.progressNormal, null),
                        array[1]
                    ),
                    SegmentedProgressBar.BarContext(
                        ResourcesCompat.getColor(resources, R.color.progressBad, null),
                        ResourcesCompat.getColor(resources, R.color.progressBad, null),
                        array[2]
                    ),
                    SegmentedProgressBar.BarContext(
                        ResourcesCompat.getColor(resources, R.color.progressWorst, null),
                        ResourcesCompat.getColor(resources, R.color.progressWorst, null),
                        array[3]
                    )
                )
            )
        }
    }
}