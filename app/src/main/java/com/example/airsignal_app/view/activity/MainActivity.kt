package com.example.airsignal_app.view.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.animation.*
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VISIBLE
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.HandlerCompat
import androidx.core.view.setMargins
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.viewpager2.widget.ViewPager2
import androidx.work.*
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.*
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.StaticDataObject.CO_INDEX
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.dao.StaticDataObject.IN_COMPLETE_ADDRESS
import com.example.airsignal_app.dao.StaticDataObject.NO2_INDEX
import com.example.airsignal_app.dao.StaticDataObject.NOT_SHOWING_LOADING_FLOAT
import com.example.airsignal_app.dao.StaticDataObject.O3_INDEX
import com.example.airsignal_app.dao.StaticDataObject.PM10_INDEX
import com.example.airsignal_app.dao.StaticDataObject.PM2p5_INDEX
import com.example.airsignal_app.dao.StaticDataObject.SHOWING_LOADING_FLOAT
import com.example.airsignal_app.dao.StaticDataObject.SO2_INDEX
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.firebase.admob.AdViewClass
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.login.SilentLoginClass
import com.example.airsignal_app.repo.BaseRepository
import com.example.airsignal_app.util.*
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.DataTypeParser.applySkyImg
import com.example.airsignal_app.util.`object`.DataTypeParser.applySkyText
import com.example.airsignal_app.util.`object`.DataTypeParser.convertDayOfWeekToKorean
import com.example.airsignal_app.util.`object`.DataTypeParser.convertLocalDateTimeToLong
import com.example.airsignal_app.util.`object`.DataTypeParser.convertTimeToMinutes
import com.example.airsignal_app.util.`object`.DataTypeParser.getComparedTemp
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.DataTypeParser.getDataColor
import com.example.airsignal_app.util.`object`.DataTypeParser.getDataOpacityColor
import com.example.airsignal_app.util.`object`.DataTypeParser.getHourCountToTomorrow
import com.example.airsignal_app.util.`object`.DataTypeParser.getSkyImgSmall
import com.example.airsignal_app.util.`object`.DataTypeParser.isRainyDay
import com.example.airsignal_app.util.`object`.DataTypeParser.millsToString
import com.example.airsignal_app.util.`object`.DataTypeParser.modifyCurrentRainType
import com.example.airsignal_app.util.`object`.DataTypeParser.modifyCurrentTempType
import com.example.airsignal_app.util.`object`.DataTypeParser.translateSky
import com.example.airsignal_app.util.`object`.DataTypeParser.translateUV
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.util.`object`.GetAppInfo.getEntireSun
import com.example.airsignal_app.util.`object`.GetAppInfo.getIsNight
import com.example.airsignal_app.util.`object`.GetAppInfo.getTopicNotification
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLastAddress
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.example.airsignal_app.util.`object`.GetSystemInfo.isThemeNight
import com.example.airsignal_app.util.`object`.SetAppInfo.removeSingleKey
import com.example.airsignal_app.util.`object`.SetAppInfo.setCurrentLocation
import com.example.airsignal_app.util.`object`.SetAppInfo.setNotificationAddress
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserLastAddr
import com.example.airsignal_app.util.`object`.SetSystemInfo.setUvBackgroundColor
import com.example.airsignal_app.view.*
import com.example.airsignal_app.vmodel.GetWeatherViewModel
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import okhttp3.internal.format
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@SuppressLint("InflateParams")
class MainActivity
    : BaseActivity<ActivityMainBinding>() {
    override val resID: Int get() = R.layout.activity_main

    private var isBackPressed = false
    private val sideMenuBuilder by lazy { SideMenuBuilder(this) }
    private val sideMenuView: View by lazy {
        LayoutInflater.from(this@MainActivity).inflate(R.layout.side_menu, null)
    }
    private lateinit var indicators: Array<ImageView>
    private val vib by lazy { VibrateUtil(this) }
    private val getDataViewModel by viewModel<GetWeatherViewModel>()

    private val dailyWeatherList = ArrayList<AdapterModel.DailyWeatherItem>()
    private val weeklyWeatherList = ArrayList<AdapterModel.WeeklyWeatherItem>()
    private val uvLegendList = ArrayList<AdapterModel.UVLegendItem>()
    private val uvResponseList = ArrayList<AdapterModel.UVResponseItem>()
    private val dailyWeatherAdapter by lazy { DailyWeatherAdapter(this, dailyWeatherList) }
    private val weeklyWeatherAdapter by lazy { WeeklyWeatherAdapter(this, weeklyWeatherList) }
    private val reportViewPagerAdapter by lazy {
        ReportViewPagerAdapter(
            this,
            reportViewPagerItem,
            binding.nestedReportViewpager
        )
    }
    private val uvLegendAdapter = UVLegendAdapter(this, uvLegendList)
    private val uvResponseAdapter = UVResponseAdapter(this, uvResponseList)
    private val reportViewPagerItem = ArrayList<AdapterModel.ReportItem>()
    private val airQList = ArrayList<AdapterModel.AirQTitleItem>()
    private val airQAdapter = AirQTitleAdapter(this, airQList)

    private var currentSun = 0
    private var isSunAnimated = false
    private var isProgressed = false
    private val rotateAnim by lazy {
        RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 700
            interpolator = LinearInterpolator()
            repeatCount = Animation.INFINITE
            repeatMode = Animation.RESTART
        }
    }

    override fun onResume() {
        super.onResume()
        addSideMenu()
        if (!isProgressed) {
//            showPB()
            isProgressed = true
        }
        getDataSingleTime()
        Thread.sleep(100)
        binding.nestedAdView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.nestedAdView.destroy()
    }

    override fun onPause() {
        super.onPause()
        binding.nestedAdView.pause()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            window.setBackgroundDrawableResource(R.drawable.main_bg_snow)
            createWorkManager()        // 워크 매니저 생성
        }

        initBinding()
        binding.dataVM = getDataViewModel

        initializing()

        // 메인 하단 스크롤 유도 화살표 애니메이션 적용
        val bottomArrowAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_arrow_anim)
        binding.mainMotionSLideImg.startAnimation(bottomArrowAnim)

        //UV 범주 아이템 추가
        addUvLegendItem(0, "0 - 2", getColor(R.color.uv_low), getString(R.string.uv_low))
        addUvLegendItem(1, "3 - 5", getColor(R.color.uv_normal), getString(R.string.uv_normal))
        addUvLegendItem(2, "6 - 7", getColor(R.color.uv_high), getString(R.string.uv_high))
        addUvLegendItem(
            3,
            "8 - 10",
            getColor(R.color.uv_very_high),
            getString(R.string.uv_very_high)
        )
        addUvLegendItem(4, "11 - ", getColor(R.color.uv_caution), getString(R.string.uv_caution))

        binding.seekArc.setOnTouchListener { _, _ -> true } // 자외선 그래프 클릭 방지

        // 자외선 지수 접고 펴기 화살표
        binding.mainUVBox.apply {
            this.setOnClickListener {
                mVib()
                if (binding.mainUvCollapsedLayout.visibility == VISIBLE) {
                    binding.mainUvCollapseArrow.setImageDrawable(
                        ResourcesCompat.getDrawable(resources, R.drawable.btn_down, null)
                    )
                    binding.mainUvCollapsedLayout.apply { this.visibility = GONE }
                } else {
                    binding.mainUvCollapseArrow.setImageDrawable(
                        ResourcesCompat.getDrawable(resources, R.drawable.btn_up, null)
                    )
                    binding.mainUvCollapsedLayout.apply { this.visibility = VISIBLE }
                }
            }
        }

        // 스크롤 최상단으로 올리기 버튼
        binding.nestedFab.setOnClickListener {
            binding.nestedScrollview.smoothScrollTo(0, 0, 500)
        }

        binding.nestedScrollview.setOnScrollChangeListener { v, _, _, _, _ ->
            // 스크롤이 최하단일 경우 최초 한번만 일출/일몰 그래프 애니메이션
            if (!v.canScrollVertically(1)) {
                if (!isSunAnimated) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.seekArc.progress = 0
                        delay(100)
                        val animatorSun =
                            ObjectAnimator.ofInt(binding.seekArc, "progress", currentSun)
                        animatorSun.duration = 800
                        animatorSun.start()
                    }
                    isSunAnimated = true
                }
            }

            // 하단 스크롤시 네비게이션 바 색상 하얀색으로 변경
            if (v.scrollY == 0) {
                window.navigationBarColor = getColor(android.R.color.transparent)
                binding.nestedFab.apply { alpha = 0f }
            } else {
                window.navigationBarColor = getColor(R.color.theme_view_color)
                binding.nestedFab.apply { alpha = 1f }
            }
        }

        binding.mainRefreshData.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                v!!.startAnimation(rotateAnim)
                mVib()
                getDataSingleTime()
            }
        })

        binding.mainTopBarGpsTitle.requestFocus()
        binding.mainTopBarGpsTitle.setOnFocusChangeListener { v, hasFocus ->
            v.isSelected = hasFocus
        }
        binding.mainTopBarGpsTitleScroll.isHorizontalScrollBarEnabled = false

        // 플러스 모양 추가시 주소등록 다이얼로그
        binding.mainAddAddress.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mVib()
                val bottomSheet =
                    SearchDialog(
                        this@MainActivity, 0, supportFragmentManager,
                        BottomSheetDialogFragment().tag
                    )
                bottomSheet.show(0)
            }
        })

        // 현재 주소로 갱신
        binding.mainGpsFix.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mVib()
//                showPB()
                v!!.startAnimation(rotateAnim)
                getCurrentLocationData()
            }
        })

        // 사이드 메뉴 세팅
        binding.mainSideMenuIv.setOnClickListener(object : OnSingleClickListener() {
            @SuppressLint("InflateParams")
            override fun onSingleClick(v: View?) {
                sideMenuBuilder.show(sideMenuView, true)
            }
        })
    }

    // 햄버거 메뉴 세팅
    private fun addSideMenu() {
        try {
            val cancel = sideMenuView.findViewById<ImageView>(R.id.headerCancel)
            val profile = sideMenuView.findViewById<ImageView>(R.id.navHeaderProfileImg)
            val id = sideMenuView.findViewById<TextView>(R.id.navHeaderUserId)
            val weather = sideMenuView.findViewById<TextView>(R.id.navMenuWeather)
            val setting = sideMenuView.findViewById<TextView>(R.id.navMenuSetting)
            val headerTr = sideMenuView.findViewById<TableRow>(R.id.headerTr)
            val adView = sideMenuView.findViewById<AdView>(R.id.navMenuAdview)

            sideMenuBuilder.apply {
                setBackPressed(cancel)
                setUserData(profile, id)
                AdViewClass(this@MainActivity).loadAdView(adView)
            }

            headerTr.setOnClickListener {
                if (getUserLoginPlatform(this) == "")
                    EnterPageUtil(this@MainActivity).toLogin()
            }
            weather.setOnClickListener {
                sideMenuBuilder.dismiss()
            }
            setting.setOnClickListener {
                CompletableFuture.supplyAsync {
                    sideMenuBuilder.dismiss()
                }.thenAccept {
                    val intent = Intent(this@MainActivity, SettingActivity::class.java)
                    startActivity(intent)
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    // 진동 발생
    private fun mVib() {
        vib.make(20)
    }

    // 시간별 날씨 스크롤 첫번째 인덱스로 이동
    private fun scrollSmoothFirst(position: Int) {
        val layoutManager = binding.mainDailyWeatherRv.layoutManager
        layoutManager?.let {
            val smoothScroller = object : LinearSmoothScroller(this) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START // 가장 첫 번째로 스크롤되도록 설정
                }
            }
            binding.mainDailyWeatherRv.setPadding(22,0,15,0)
            smoothScroller.targetPosition = position + 5
            it.startSmoothScroll(smoothScroller)
        }
    }

    // 시간별 날씨 색션 컬러 변경
    private fun setSectionTextColor(t1: TextView, t2: TextView, t3: TextView) {
        t1.setTextColor(getColor(R.color.main_blue_color))
        t2.setTextColor(getColor(R.color.main_gray_color))
        t3.setTextColor(getColor(R.color.main_gray_color))
    }

    // 날씨 데이터 API 호출
    private fun getDataSingleTime() {
        if (RequestPermissionsUtil(this).isNetworkPermitted()) {
            if (RequestPermissionsUtil(this).isLocationPermitted()) {
                binding.mainDailyWeatherRv.scrollToPosition(0)
                val addrArray = resources.getStringArray(R.array.address)
                val lastAddress = getUserLastAddress(this)
                if (addrArray.contains(lastAddress)) {
                    loadSavedAddr(lastAddress)
                } else {
                    getCurrentLocationData()
                }
                // TimeOut
                HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                    if (isProgressed()) {
                        hidePB()
                    }
                }, 1000 * 9)
            } else {
                RequestPermissionsUtil(this@MainActivity).requestLocation()
            }
        } else {
            ToastUtils(this).showMessage(getString(R.string.error_network))
        }
    }

    // 저장된 주소로 데이터 호출
    private fun loadSavedAddr(addr: String?) {
        addr?.let {
            loadSavedViewModelData(it)

            RDBLogcat.writeGpsHistory(
                this, isSearched = true,
                gpsValue = addr,
                responseData = null
            )

            binding.mainGpsTitleTv.text = guardWordWrap(it)
            binding.mainTopBarGpsTitle.text = it
        }
    }

    // 7글자를 기준으로 WordWrap 적용
    private fun guardWordWrap(s: String): String {
        return try {
            val formS = if (s.first().toString() == " ")
                s.replaceFirst(" ", "") else s
            WrapTextClass().getFormedText(formS, 7)
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
            return s
        }
    }

    // 프로그래스 보이기
    private fun showPB() {
        if (binding.mainMotionLayout.alpha == NOT_SHOWING_LOADING_FLOAT) {
            binding.mainMotionLayout.alpha = SHOWING_LOADING_FLOAT
            binding.mainMotionLayout.isEnabled = false
        }
    }

    // 프로그래스 숨기기
    private fun hidePB() {
        if (binding.mainMotionLayout.alpha == SHOWING_LOADING_FLOAT) {
            binding.mainMotionLayout.alpha = NOT_SHOWING_LOADING_FLOAT
            binding.mainMotionLayout.isEnabled = true
        }
        binding.mainRefreshData.clearAnimation()
        binding.mainGpsFix.clearAnimation()
    }

    // 프로그래스 진행 여부
    private fun isProgressed(): Boolean {
        return binding.mainMotionLayout.alpha == SHOWING_LOADING_FLOAT || binding.mainRefreshData.animation != null
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initializing() {

        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(
            displayMetrics
        )
//        addExitDialog()
        // 자동 로그인
        SilentLoginClass().login(this@MainActivity, binding.mainMotionLayout)

        binding.mainDailyWeatherRv.adapter = dailyWeatherAdapter
        binding.mainWeeklyWeatherRv.adapter = weeklyWeatherAdapter
        binding.mainUVLegendRv.adapter = uvLegendAdapter
        binding.mainUvCollapseRv.adapter = uvResponseAdapter
        binding.nestedAirRv.adapter = airQAdapter

        binding.nestedReportViewpager.apply {
            adapter = reportViewPagerAdapter
            isClickable = false
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateIndicators(position)
                    binding.nestedReportViewpager.requestLayout()
                }
            })
        }

        binding.mainUvCollapseRv.isClickable = false

        AdViewClass(this).loadAdView(binding.nestedAdView)  // adView 생성

        binding.adViewCancelIv.setOnClickListener {
            it.visibility = GONE
            val layoutParams =
                RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.BELOW, R.id.nested_daily_box)
            layoutParams.setMargins(0)
            binding.adViewBox.layoutParams = layoutParams
            binding.nestedAdView.visibility = GONE
        }

        // 특정 포지션을 감지하고 처리
        val todaySection = binding.dailySectionToday
        val tomorrowSection = binding.dailySectionTomorrow
        val afterTomorrowSection = binding.dailySectionAfterTomorrow

        // 오늘 클릭
        todaySection.setOnClickListener {
            setSectionTextColor(todaySection, tomorrowSection, afterTomorrowSection)
            binding.mainDailyWeatherRv.smoothScrollToPosition(0)
        }

        // 내일 클릭
        tomorrowSection.setOnClickListener {
            binding.mainDailyWeatherRv.scrollToPosition(getHourCountToTomorrow())
            binding.mainDailyWeatherRv.post {
                scrollSmoothFirst(getHourCountToTomorrow())
            }
        }

        // 모레 클릭
        afterTomorrowSection.setOnClickListener {
            binding.mainDailyWeatherRv.post {
                scrollSmoothFirst(getHourCountToTomorrow() + 24)
            }
        }

        // 시간별 날씨 스크롤에 따른 탭 변화
        binding.mainDailyWeatherRv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx != 0) {
                    val sectionList = dailyWeatherAdapter.getDateSectionList()
                    // 현재 스크롤 위치 확인
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    sectionList.forEach {
                        if (firstVisibleItemPosition >= it) {
                            when (it) {
                                sectionList[0] -> {
                                    setSectionTextColor(
                                        todaySection,
                                        tomorrowSection,
                                        afterTomorrowSection
                                    )
                                }
                                sectionList[1] -> {
                                    setSectionTextColor(
                                        tomorrowSection,
                                        todaySection,
                                        afterTomorrowSection
                                    )
                                }
                                sectionList[2] -> {
                                    setSectionTextColor(
                                        afterTomorrowSection,
                                        todaySection,
                                        tomorrowSection
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                } else {
                    setSectionTextColor(
                        todaySection,
                        tomorrowSection,
                        afterTomorrowSection
                    )
                }
            }
        })

        // 실시간 공기질 리스트 클릭
        airQAdapter.setOnItemClickListener(object : AirQTitleAdapter.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                try {
                    val model = airQList[position]
                    applyAirQView(
                        model.nameKR, model.name, model.grade,
                        model.unit, model.value, model.maxValue
                    )

                    airQList.forEach {
                        it.isSelect = it.position == airQList[position].position
                    }
                    airQAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }
        })

        binding.nestedAirHelp.setImageDrawable(if (isThemeNight(this)) {
            ResourcesCompat.getDrawable(resources,R.drawable.ico_question_b,null)
        } else {
            ResourcesCompat.getDrawable(resources,R.drawable.ico_question,null)
        })

        binding.nestedAirHelp.setOnClickListener {
            if (binding.nestedAirHelpPopup.alpha == 0f) {
                val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                binding.nestedAirHelpPopup.apply {
                    bringToFront()
                    airQList.forEach {
                        if (it.isSelect) {
                            fetchData(
                                modifyDataSort(this@MainActivity, it.nameKR),
                                modifyDataGraph(this@MainActivity, it.nameKR)!!,
                                it.name, it.nameKR
                            )
                            return@forEach
                        }
                    }
                    startAnimation(fadeIn)
                    alpha = 1f
                }
            } else {
                val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                binding.nestedAirHelpPopup.apply {
                    startAnimation(fadeOut)
                    alpha = 0f
                }
                binding.nestedAirRv.bringToFront()
            }
        }
    }

    // 백그라운드 위치 호출
    private fun createWorkManager() {
        GetLocation(this).getGpsInBackground(0, 500f)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!isBackPressed) {
            ToastUtils(this)
                .showMessage(getString(R.string.back_press), 2)
            isBackPressed = true
        } else {
            removeSingleKey(this, lastAddress)
            EnterPageUtil(this).fullyExit()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            isBackPressed = false
        }, 2000)
    }

    // 토픽을 갱신하는 작업
    private fun reNewTopicInMain(newAddr: String) {
        val oldAddr = getTopicNotification(this)
        SubFCM().renewTopic(this, oldAddr, newAddr)
    }

    // 현재 옵저버가 없으면 생성
    private fun getDataObservers() {
        if (!getDataViewModel.fetchData().hasActiveObservers())
        { applyGetDataViewModel() }
    }


    // 뷰모델에서 Observing 한 데이터 결과 적용
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    fun applyGetDataViewModel(): MainActivity {
        getDataViewModel.fetchData().observe(this) { entireData ->
            entireData?.let { eData ->
                when (eData) {
                    is BaseRepository.ApiState.Success -> {
                        try {
                            val result = eData.data
                            val metaAddr = result.meta.address!!
                            reNewTopicInMain(metaAddr)

                            val realtime = result.realtime[0]
                            val sun = result.sun
                            val sunTomorrow = result.sun_tomorrow
                            val air = result.quality
                            val week = result.week
                            val today = result.today
                            val uv = result.uv!!
                            val yesterday = result.yesterday
                            val dateNow: LocalDateTime = LocalDateTime.now()
                            val current = result.current
                            val thunder = result.thunder!!
                            currentSun = GetAppInfo.getCurrentSun(sun.sunrise!!, sun.sunset!!)

                            airQList.clear()

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

                            current.temperature?.let { currentTemp ->
                                realtime.let {
                                    binding.mainLiveTempValue.text =
                                        modifyCurrentTempType(
                                            currentTemp,
                                            realtime.temp
                                        ).toString()
                                    binding.mainLiveTempUnit.text = "˚"

                                    binding.mainLiveTempValueC.text =
                                        modifyCurrentTempType(
                                            currentTemp,
                                            realtime.temp
                                        ).toString() + "˚"
                                }
                            }

                            realtime.let {
                                binding.mainSkyText.text = translateSky(
                                    this,
                                    applySkyText(
                                        this,
                                        modifyCurrentRainType(
                                            current.rainType,
                                            realtime.rainType
                                        ),
                                        realtime.sky,
                                        thunder
                                    )
                                )

                                binding.subAirHumid.fetchData(
                                    "${
                                        realtime.humid!!
                                            .roundToInt()
                                    }%", R.drawable.ico_main_humidity
                                )

                                binding.subAirWind.fetchData(
                                    "${
                                        realtime.windSpeed!!
                                            .roundToInt()
                                    }m/s", R.drawable.ico_main_wind
                                )

                                binding.subAirRainP.fetchData(
                                    "${
                                        realtime.rainP!!
                                            .roundToInt()
                                    }%", R.drawable.ico_main_rain
                                )
                            }

                            today?.let {
                                binding.mainMinMaxTitle.text = getString(R.string.min_max)
                                binding.mainMinMaxValue.text =
                                    "${filteringNullData(it.min!!)}˚/${filteringNullData(it.max!!)}˚"
                                binding.mainMinMaxValueC.text =
                                    "${filteringNullData(it.min)}˚/${filteringNullData(it.max)}˚"
                            }

                            updateAirQData(
                                PM2p5_INDEX, "초미세먼지", "PM2.5",
                                "㎍/㎥", air.pm25Value!!.toInt().toString(), 76f, air.pm25Grade1h!!
                            )
                            updateAirQData(
                                PM10_INDEX, "미세먼지", "PM10",
                                "㎍/㎥", air.pm10Value!!.toInt().toString(), 151f, air.pm10Grade1h!!
                            )
                            updateAirQData(
                                CO_INDEX, "일산화탄소", "CO",
                                "ppm", air.coValue!!.toString(), 15.01f, air.coGrade!!
                            )
                            updateAirQData(
                                SO2_INDEX, "아황산가스", "SO2",
                                "ppm", air.so2Value!!.toString(), 0.151f, air.so2Grade!!
                            )
                            updateAirQData(
                                NO2_INDEX, "이산화질소", "NO2",
                                "㎍/㎥", air.no2Value!!.toString(), 0.201f, air.no2Grade!!
                            )
                            updateAirQData(
                                O3_INDEX, "오존", "O3",
                                "㎍/㎥", air.o3Value!!.toString(), 0.151f, air.o3Grade!!
                            )

                            applyAirQView(
                                "초미세먼지", "PM2.5",
                                air.pm25Grade1h, "㎍/m3", air.pm25Value.toInt().toString(), 151f
                            )

                            airQList[PM2p5_INDEX].isSelect = true

                            // UV 값이 없으면 카드 없앰
                            if ((uv.flag == null) || (uv.value == null) || (uv.flag == "null") || (uv.value.toString() == "null"))
                                binding.mainUVBox.visibility = GONE
                            else {
                                binding.mainUVBox.visibility = VISIBLE
                                applyUvResponseItem(uv.flag)   // 자외선 단계별 대응요령 추가
                                setUvBackgroundColor(
                                    this,
                                    uv.flag,
                                    binding.mainUVLegendCardView
                                ) // UV 범주 색상 변경
                                binding.mainUvValue.text =
                                    translateUV(this, uv.flag) + "\n" + uv.value.toString()
                            }

                            val sbRise = StringBuffer().append(sun.sunrise).insert(2, ":")
                            val sbSet = StringBuffer().append(sun.sunset).insert(2, ":")
                            sunTomorrow?.let {
                                binding.mainSunBoxTomorrowTitle.visibility = VISIBLE
                                val sbRiseTom = StringBuffer().append(it.sunrise).insert(2, ":")
                                val sbSetTom = StringBuffer().append(it.sunset).insert(2, ":")
                                binding.mainSunRiseTom.text = sbRiseTom
                                binding.mainSunSetTom.text = sbSetTom
                            }

                            binding.mainSunRiseTime.text = sbRise
                            binding.mainSunSetTime.text = sbSet

                            getCompareTempText(
                                yesterday.temp!!,
                                modifyCurrentTempType(current.temperature, realtime.temp),
                                binding.mainCompareTempTv
                            )

                            binding.mainSkyImg.setImageDrawable(
                                applySkyImg(
                                    this,
                                    modifyCurrentRainType(current.rainType, realtime.rainType),
                                    realtime.sky, thunder,
                                    isLarge = true, isNight = getIsNight(currentSun)
                                )
                            )

                            applyWindowBackground(
                                currentSun,
                                applySkyText(
                                    this,
                                    modifyCurrentRainType(current.rainType, realtime.rainType),
                                    realtime.sky, thunder
                                )
                            )

                            reportViewPagerItem.clear()
                            result.summary?.let { sList ->
                                sList.forEach { summary ->
                                    addReportViewPagerItem(
                                        summary.replace("○", "")
                                            .replace("\n", "")
                                            .trim()
                                    )
                                }
                            }

                            createIndicators(binding.nestedReportIndicator)
                            reportViewPagerAdapter.notifyDataSetChanged()

                            if (reportViewPagerItem.size == 0) {
                                binding.nestedReportFrame.visibility = GONE
                            } else {
                                binding.nestedReportFrame.visibility = VISIBLE
                            }

                            binding.mainSensTitle.text = getString(R.string.sens_temp)
                            binding.mainSensValue.text =
                                SensibleTempFormula().getSensibleTemp(
                                    ta = current.temperature!!,
                                    rh = current.humidity!!,
                                    v = current.windSpeed!!
                                ).roundToInt().toString() + "˚"

                            binding.mainSensValueC.text =
                                SensibleTempFormula().getSensibleTemp(
                                ta = current.temperature,
                                rh = current.humidity,
                                v = current.windSpeed
                            ).roundToInt().toString() + "˚"

                            for (i: Int in 0 until result.realtime.size) {
                                val dailyIndex = result.realtime[i]
                                val forecastToday = LocalDateTime.parse(dailyIndex.forecast)
                                val dailyTime =
                                    millsToString(
                                        convertLocalDateTimeToLong(forecastToday),
                                        "HHmm"
                                    )
                                val dailySunProgress =
                                    100 * (convertTimeToMinutes(dailyTime) - convertTimeToMinutes(
                                        sun.sunrise
                                    )) /
                                            getEntireSun(sun.sunrise, sun.sunset)
                                val isNight = getIsNight(dailySunProgress)
                                if (i == result.realtime.lastIndex + 1) {
                                    break
                                } else if (i == 0) {
                                    addDailyWeatherItem(
                                        "${forecastToday.hour}${getString(R.string.hour)}",
                                        applySkyImg(
                                            this,
                                            modifyCurrentRainType(
                                                current.rainType,
                                                realtime.rainType
                                            ),
                                            dailyIndex.sky,
                                            thunder,
                                            isLarge = false,
                                            isNight = isNight
                                        )!!,
                                        "${
                                            modifyCurrentTempType(
                                                current.temperature, realtime.temp
                                            ).roundToInt()
                                        }˚",
                                        dailyIndex.forecast!!,
                                        isRainyDay(dailyIndex.rainType),
                                        dailyIndex.rainP!!
                                    )
                                } else {
                                    addDailyWeatherItem(
                                        "${forecastToday.hour}${getString(R.string.hour)}",
                                        applySkyImg(
                                            this,
                                            dailyIndex.rainType, dailyIndex.sky, thunder,
                                            isLarge = false, isNight = isNight
                                        )!!,
                                        "${dailyIndex.temp!!.roundToInt()}˚",
                                        dailyIndex.forecast!!,
                                        isRainyDay(dailyIndex.rainType),
                                        dailyIndex.rainP!!
                                    )
                                }
                            }

                            for (i: Int in 0 until (7)) {
                                try {
                                    val formedDate = dateNow.plusDays(i.toLong())
                                    val date: String = when (i) {
                                        0 -> {
                                            getString(R.string.today)
                                        }
                                        1 -> {
                                            getString(R.string.tomorrow)
                                        }
                                        else -> {
                                            "${
                                                convertDayOfWeekToKorean(
                                                    this, dateNow.dayOfWeek.value + i
                                                )
                                            }${getString(R.string.date)}"
                                        }
                                    }
                                    addWeeklyWeatherItem(
                                        date,
                                        convertDateAppendZero(formedDate),
                                        getSkyImgSmall(this, wfMin[i], false)!!,
                                        getSkyImgSmall(this, wfMax[i], false)!!,
                                        "${taMin[i]!!.roundToInt()}˚",
                                        "${taMax[i]!!.roundToInt()}˚"
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            weeklyWeatherAdapter.notifyDataSetChanged()
                            dailyWeatherAdapter.notifyDataSetChanged()
                            airQAdapter.notifyDataSetChanged()

                            changeTextColorStyle(
                                applySkyText(
                                    this,
                                    modifyCurrentRainType(current.rainType, realtime.rainType),
                                    realtime.sky, thunder
                                ),
                                getIsNight(currentSun)
                            )
                            runOnUiThread {
                                hidePB()
                                showAllViews()

                                RDBLogcat.writeGpsHistory(
                                    this,
                                    isSearched = false,
                                    gpsValue = metaAddr,
                                    responseData = "${getUserLastAddress(this)},${result.toString()}"
                                )
                            }
                        } catch (e: java.lang.NullPointerException) {
                            runOnUiThread {
                                hidePB()
                                hideAllViews(error = "NullPointerException")
                            }
                        } catch (e: IndexOutOfBoundsException) {
                            runOnUiThread {
                                hidePB()
                                hideAllViews(error = "IndexOutOfBoundsException")
                            }
                        }
                    }

                    is BaseRepository.ApiState.Error -> {
                        runOnUiThread {
                            hidePB()
                            hideAllViews(error = eData.errorMessage)
                        }
                    }

                    is BaseRepository.ApiState.Loading -> {
                        runOnUiThread { showPB() }
                    }
                }
            }
        }
        return this
    }

    // 하늘상태에 따라 윈도우 배경 변경
    private fun applyWindowBackground(progress: Int, sky: String?) {
        if (getIsNight(progress)) {
            window.setBackgroundDrawableResource(R.drawable.main_bg_night)
            binding.mainSkyStarImg.setImageDrawable(
                ResourcesCompat.getDrawable(resources,R.drawable.bg_nightsky,null)
            )
            changeTextColorStyle(sky!!, true)
        } else {
            binding.mainSkyStarImg.setImageDrawable(null)
            when (sky) {
                "맑음", "구름많음" -> window.setBackgroundDrawableResource(R.drawable.main_bg_clear)

                "구름많고 비/눈", "흐리고 비/눈", "비/눈", "구름많고 소나기",
                "흐리고 비", "구름많고 비", "흐리고 소나기", "소나기", "비", "흐림",
                "번개,뇌우", "비/번개" ->
                    window.setBackgroundDrawableResource(R.drawable.main_bg_cloudy)

                "구름많고 눈", "눈", "흐리고 눈" ->
                    window.setBackgroundDrawableResource(R.drawable.main_bg_snow)

                else -> window.setBackgroundDrawableResource(R.drawable.main_bg_snow)
            }
        }
    }

    // 뷰페이저 인디케이터 업데이트
    private fun updateIndicators(position: Int) {
        if (!indicators.indices.isEmpty()) {
            for (i in indicators.indices) {
                indicators[i].setImageResource(
                    if (i == position) R.drawable.indicator_fill // 선택된 원 이미지
                    else R.drawable.indicator_empty // 선택되지 않은 원 이미지
                )
            }
        }
    }

    // 뷰페이저 인디케이터 생성
    private fun createIndicators(indicator: LinearLayout) {
        binding.nestedReportIndicator.removeAllViews()
        indicators = Array(reportViewPagerItem.size) {
            val indicatorView = ImageView(this)
            val params = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            indicatorView.layoutParams = params
            indicatorView.setImageResource(R.drawable.indicator_empty) // 선택되지 않은 원 이미지
            indicator.addView(indicatorView)
            indicatorView
        }
        updateIndicators(binding.nestedReportViewpager.currentItem)
    }

    // 필드값이 없을 때 -100 출력 됨
    private fun filteringNullData(data: Double): String {
        return if (data != -100.0) data.roundToInt().toString() else ""
    }

    // 시간별 날씨 리사이클러뷰 아이템 추가
    private fun addDailyWeatherItem(
        time: String, img: Drawable, value: String, date: String,
        isRain: Boolean, rainP: Double?
    ) {
        val item = AdapterModel.DailyWeatherItem(time, img, value, date, rainP, isRain)

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
            s.length - 1, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv.text = span
    }

    // 미세먼지 그래프 화살표 색상 변경
    private fun setPm2p5ArrowTint(value: Int): Int {
        return when (value) {
            in 0..15 -> {
                ResourcesCompat.getColor(resources, R.color.air_good, null)
            }
            in 16..35 -> {
                ResourcesCompat.getColor(resources, R.color.air_normal, null)
            }
            in 36..75 -> {
                ResourcesCompat.getColor(resources, R.color.air_bad, null)
            }
            in 76..125 -> {
                ResourcesCompat.getColor(resources, R.color.air_very_bad, null)
            }
            else -> {
                ResourcesCompat.getColor(resources, com.aslib.R.color.progressError, null)
            }
        }
    }

    // 초미세먼지 그래프 화살표 색상 변경
    private fun setPm10ArrowTint(value: Int): Int {
        return when (value) {
            in 0..30 -> {
                ResourcesCompat.getColor(resources, R.color.air_good, null)
            }
            in 31..79 -> {
                ResourcesCompat.getColor(resources, R.color.air_normal, null)
            }
            in 80..150 -> {
                ResourcesCompat.getColor(resources, R.color.air_bad, null)
            }
            in 151..200 -> {
                ResourcesCompat.getColor(resources, R.color.air_very_bad, null)
            }
            else -> {
                ResourcesCompat.getColor(resources, com.aslib.R.color.progressError, null)
            }
        }
    }

    // 어제와 기온 비교
    @SuppressLint("SetTextI18n")
    private fun getCompareTempText(y: Double?, t: Double?, tv: TextView) {
        val compared = getComparedTemp(y, t)
        compared?.let {
            if (it < 0) {
                tv.visibility = VISIBLE
                tv.text =
                    if (resources.configuration.locales[0] == Locale.KOREA) {
                        "어제보다 ${it.absoluteValue}˚ 낮아요"
                    } else {
                        "${it.absoluteValue}˚ lower than yesterday"
                    }
            } else if (it == 0.0) {
                tv.visibility = VISIBLE
                tv.text = getString(R.string.similar_temp)
            } else {
                tv.visibility = VISIBLE
                tv.text =
                    if (resources.configuration.locales[0] == Locale.KOREA) {
                        "어제보다 ${it.absoluteValue} ˚ 높아요"
                    } else {
                        "${it.absoluteValue}˚ upper than yesterday"
                    }
            }
        }
    }

    // 통신에 실패할 경우 레이아웃 처리
    private fun hideAllViews(error: String?) {
        when (error) {
            "API ERROR OCCURRED" -> {
                binding.mainSkyText.text = "데이터 호출에 실패했습니다"
            }
            "NOT SERVICED Location" -> {
                binding.mainSkyText.text = getString(R.string.not_serviced_location)
            }
            "Network Error" -> {
                binding.mainSkyText.text = "요청시간 지연, 재갱신 필요"
            }
            "Timeout Error" -> {
                binding.mainSkyText.text = "네트워크 오류, 재갱신 필요"
            }
            "Get Location Error" -> {
                binding.mainSkyText.text = "주소 호출 실패"
            }
            "GPS Connect Error" -> {
                binding.mainSkyText.text = "GPS 연결 불가"
            }
            else -> {
                binding.mainSkyText.text = "알수없는 오류 발생"
            }
        }

        // 주소, 갱신버튼, 현재기온, 기온비교, 최저/최고 기온, 날씨 정보 더보기, 모션 슬라이드 막기
        binding.mainSkyImg.apply {
            setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.refresh, null
                )
            )
            imageTintList = ColorStateList.valueOf(getColor(R.color.main_blue_color))

            setOnClickListener {
                if (binding.mainSkyImg.imageTintList ==
                    ColorStateList.valueOf(getColor(R.color.main_blue_color)
                )) {
                    mVib()
//                    showPB()
                    getCurrentLocationData()
                }
            }

            setVisibilityForViews(GONE,error)
        }
    }

    // 통신에 성공할 경우 레이아웃 처리
    private fun showAllViews() {
        setVisibilityForViews(VISIBLE,null)

        binding.mainSkyImg.imageTintList = null
    }

    private fun setVisibilityForViews(visibility: Int, error: String?) {
        val textViewArray = arrayListOf(
            binding.mainGpsTitleTv,
            binding.mainLiveTempValue,
            binding.mainLiveTempUnit,
            binding.mainCompareTempTv,
            binding.mainMinMaxTitle,
            binding.mainMinMaxValue,
            binding.mainMotionSlideGuide,
            binding.mainTopBarGpsTitle,
            binding.mainSensTitle,
            binding.mainSensValue
            )

        if (visibility == GONE) {
            if (error == "Network Error") {
                binding.mainAddAddress.setImageDrawable(null)
            }
            textViewArray.forEach {
                it.text = ""
            }
            binding.mainGpsFix.setImageDrawable(null)
            binding.mainMotionSLideImg.setImageDrawable(null)
            binding.mainRefreshData.setImageDrawable(null)
            binding.mainSideMenuIv.setImageDrawable(null)

            binding.mainMotionLayout.apply {
                transitionToStart()
                Thread.sleep(100)
                isInteractionEnabled = false // 모션 레이아웃의 스와이프를 막음
            }

        } else {
            binding.mainSensTitle.text = getString(R.string.sens_temp)
            binding.mainMinMaxTitle.text = getString(R.string.min_max)
            binding.mainMotionSlideGuide.text = getString(R.string.slide_more)
            binding.mainGpsFix.setImageDrawable(
                ResourcesCompat.getDrawable(resources,R.drawable.gps_fix,null))
            binding.mainMotionSLideImg.setImageDrawable(
                ResourcesCompat.getDrawable(resources,R.drawable.drop_down_bottom,null)
            )
            binding.mainRefreshData.setImageDrawable(
                ResourcesCompat.getDrawable(resources,R.drawable.refresh,null)
            )
            binding.mainAddAddress.setImageDrawable(
                ResourcesCompat.getDrawable(resources,R.drawable.ico_add_w,null)
            )
            binding.mainSideMenuIv.setImageDrawable(
                ResourcesCompat.getDrawable(resources,R.drawable.ico_hamb_w,null)
            )
            // 원래 상태로 복구하기 위해 제약 조건 변경
            binding.mainMotionLayout.isInteractionEnabled = true
        }
    }

    private fun loadCurrentViewModelData(lat: Double, lng: Double) {
        getDataObservers()
        getDataViewModel.loadData(lat, lng, null)
    }

    private fun loadSavedViewModelData(addr: String) {
        getDataObservers()
        getDataViewModel.loadData(null, null, addr)
    }

    private fun setAirCPV(barColor: Int, unit: String, grade: Int) {
        binding.nestedAirCpv.apply {
            setBarColor(barColor)
            setUnit(unit)
            setTextColor(barColor)
            rimColor = getDataOpacityColor(this@MainActivity, grade)
        }
    }

    private fun applyAirQView(
        kr: String, en: String, grade: Int, unit: String,
        value: String, maxValue: Float
    ) {
        binding.nestedAirTitleKr.text = kr
        binding.nestedAirTitleEn.text = en
        setAirCPV(getDataColor(this, grade), unit, grade)

        binding.nestedAirCpv.apply {
            setText(DataTypeParser.getDataText(grade))
            setMaxValue(maxValue)
        }
        binding.nestedAirValue.text = value
        binding.nestedAirValue.setTextColor(getDataColor(this@MainActivity, grade))
        binding.nestedAirUnit.text = unit

        binding.nestedAirCpv.setValueAnimated(value.toFloat(), 500)
    }

    private fun addAirQItem(
        position: Int, nameKR: String, name: String, unit: String,
        value: String, maxValue: Float, grade: Int
    ) {
        val item = AdapterModel.AirQTitleItem(
            false, position, nameKR,
            name, unit, value, maxValue, grade
        )

        this.airQList.add(position, item)
        this.airQAdapter.notifyItemChanged(position)
    }

    private fun updateAirQData(
        position: Int, nameKR: String, name: String, unit: String,
        value: String, maxValue: Float, grade: Int
    ) {

        if (!airQList.contains(
                AdapterModel.AirQTitleItem(
                    false, position, nameKR,
                    name, unit, value, maxValue, grade
                )
            )
        ) {
            addAirQItem(
                position, nameKR,
                name, unit, value, maxValue, grade
            )
        }
    }

    // 현재 위치정보로 DB 갱신
    private fun updateCurrentAddress(lat: Double, lng: Double, addr: String?) {
        val roomDB = GpsRepository(this@MainActivity)
        addr?.let {
            setUserLastAddr(this@MainActivity, it)
        }
        val model = GpsEntity()

        model.name = CURRENT_GPS_ID
        model.lat = lat
        model.lng = lng
        model.addr = addr
        model.timeStamp = getCurrentTime()
        if (gpsDbIsEmpty(roomDB)) {
            roomDB.insert(model)
        } else {
            roomDB.update(model)
        }
    }

    // DB가 비어있는지 확인
    private fun gpsDbIsEmpty(db: GpsRepository): Boolean {
        return db.findAll().isEmpty()
    }

    // 자외선 범주 아이템 추가
    private fun addUvLegendItem(index: Int, value: String, color: Int, grade: String) {
        val item = AdapterModel.UVLegendItem(value, color, grade)

        uvLegendList.add(index, item)
        uvLegendAdapter.notifyItemInserted(index)
    }

    // 자외선 단계별 대응요령 아이템 추가
    private fun addUvResponseItem(text: String) {
        val item = AdapterModel.UVResponseItem(text)

        uvResponseList.add(item)
    }

    // 날씨특보 아이템 추가
    private fun addReportViewPagerItem(text: String) {
        val item = AdapterModel.ReportItem(text)

        reportViewPagerItem.add(item)
    }

    // 자외선 지수에 따른 대처요령 불러오기
    private fun getUvArray(grade: String): Array<String> {
        return when (grade) {
            "위험" -> {
                resources.getStringArray(R.array.uv_caution)
            }
            "매우높음" -> {
                resources.getStringArray(R.array.uv_very_high)
            }
            "높음" -> {
                resources.getStringArray(R.array.uv_high)
            }
            "보통" -> {
                resources.getStringArray(R.array.uv_normal)
            }
            "낮음" -> {
                resources.getStringArray(R.array.uv_low)
            }
            else -> {
                resources.getStringArray(R.array.uv_none)
            }
        }
    }

    // 자외선 단계별 대응요령 필터링
    @SuppressLint("NotifyDataSetChanged")
    private fun applyUvResponseItem(grade: String) {
        uvResponseList.clear()
        val cautionArray = getUvArray(grade)
        cautionArray.forEach {
            addUvResponseItem(it)
        }
        uvResponseAdapter.notifyDataSetChanged()
    }

    // 메인화면 배경에 따라 텍스트의 색상을 변경
    private fun changeTextColorStyle(sky: String, isNight: Boolean) {
        val changeColorTextViews = listOf(
            binding.mainLiveTempValue, binding.mainLiveTempUnit, binding.mainCompareTempTv,
            binding.mainTopBarGpsTitle, binding.mainMotionSlideGuide, binding.mainSkyText,
            binding.mainGpsTitleTv, binding.mainSensTitle, binding.mainSensValue,
            binding.mainMinMaxTitle,binding.mainMinMaxValue,binding.mainLiveTempTitleC,
            binding.mainLiveTempValueC,binding.mainSensTitleC,binding.mainSensValueC,
            binding.mainMinMaxTitleC,binding.mainMinMaxValueC

        )
        val changeTintImageViews = listOf(
            binding.mainSideMenuIv, binding.mainAddAddress,
            binding.mainGpsFix, binding.mainMotionSLideImg,
            binding.mainRefreshData
        )

        // 글자색 white로 변경
        @Suppress("DEPRECATION")
        fun white() {
            changeColorTextViews.forEach {
                it.setTextColor(getColor(R.color.white))
            }

            changeTintImageViews.forEach {
                it.imageTintList = ColorStateList.valueOf(getColor(R.color.white))
            }

            binding.mainTopBarGpsTitle.compoundDrawablesRelative[0].mutate()
                .setTint(ResourcesCompat.getColor(resources, R.color.white, null))
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        // 글자색 black으로 변경
        @Suppress("DEPRECATION")
        fun black() {
            changeColorTextViews.forEach {
                it.setTextColor(getColor(R.color.bg_black_color))
            }

            changeTintImageViews.forEach {
                it.imageTintList = ColorStateList.valueOf(getColor(R.color.bg_black_color))
            }

            binding.mainTopBarGpsTitle.compoundDrawablesRelative[0].mutate()
                .setTint(ResourcesCompat.getColor(resources, R.color.black, null))
            window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (!isNight) {
            when (sky) {
                "맑음", "구름많음", "구름많고 눈", "눈", "흐리고 눈" -> {
                    black()
                }
                else -> {
                    white()
                }
            }
        } else {
            white()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationData() {
        val locationClass = GetLocation(this)
        if (locationClass.isGPSConnected()) {
            CoroutineScope(Dispatchers.Default).launch {
                LocationServices.getFusedLocationProviderClient(this@MainActivity).run {
                    this.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            location?.let { loc ->
                                hidePB()
                                val addr = GetLocation(this@MainActivity)
                                    .getAddress(loc.latitude,loc.longitude)
                                val formatAddr = addr!!
                                    .replaceFirst(" ", "")
                                    .replace(getString(R.string.korea), "")
                                    .replace("null", "")

                                val regexAddr = AddressFromRegex(addr).getAddress()
                                val formedAddr = if (regexAddr != null &&
                                    regexAddr != IN_COMPLETE_ADDRESS) {
                                    regexAddr
                                } else {
                                    formatAddr
                                }

                                setCurrentLocation(this@MainActivity, formatAddr)

                                updateCurrentAddress(
                                    loc.latitude, loc.longitude,
                                    formatAddr
                                )

                                setNotificationAddress(this@MainActivity, formedAddr)

                                binding.mainGpsTitleTv.text = guardWordWrap(
                                    formedAddr
                                )

                                binding.mainTopBarGpsTitle.text =
                                    formedAddr

                                loadCurrentViewModelData(loc.latitude, loc.longitude)
                            }
                        }
                } .addOnFailureListener {
                    RDBLogcat.writeErrorNotANR(
                        this@MainActivity, sort = RDBLogcat.ERROR_LOCATION_FAILED,
                        msg = it.localizedMessage!!
                    )
                }
            }
        } else if (!locationClass.isGPSConnected() && locationClass.isNetWorkConnected()) {
            CoroutineScope(Dispatchers.Default).launch {
                val lm =
                    getSystemService(LOCATION_SERVICE) as LocationManager
                val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                location?.let { loc ->
                    loadCurrentViewModelData(loc.latitude, loc.longitude)
                }
            }
        } else {
            locationClass.requestSystemGPSEnable()
        }
    }
}