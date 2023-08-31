package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.LocationManager
import android.os.*
import android.os.Build.VERSION
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import app.airsignal.weather.adapter.*
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.dao.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.dao.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.dao.ErrorCode.ERROR_GET_LOCATION_FAILED
import app.airsignal.weather.dao.ErrorCode.ERROR_GPS_CONNECTED
import app.airsignal.weather.dao.ErrorCode.ERROR_LOCATION_FAILED
import app.airsignal.weather.dao.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.dao.ErrorCode.ERROR_NOT_SERVICED_LOCATION
import app.airsignal.weather.dao.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.dao.ErrorCode.ERROR_TIMEOUT
import app.airsignal.weather.dao.IgnoredKeyFile.lastAddress
import app.airsignal.weather.dao.IgnoredKeyFile.playStoreURL
import app.airsignal.weather.dao.StaticDataObject.CO_INDEX
import app.airsignal.weather.dao.StaticDataObject.CURRENT_GPS_ID
import app.airsignal.weather.dao.StaticDataObject.IN_COMPLETE_ADDRESS
import app.airsignal.weather.dao.StaticDataObject.LANG_EN
import app.airsignal.weather.dao.StaticDataObject.LANG_KR
import app.airsignal.weather.dao.StaticDataObject.NO2_INDEX
import app.airsignal.weather.dao.StaticDataObject.NOT_SHOWING_LOADING_FLOAT
import app.airsignal.weather.dao.StaticDataObject.O3_INDEX
import app.airsignal.weather.dao.StaticDataObject.PM10_INDEX
import app.airsignal.weather.dao.StaticDataObject.PM2p5_INDEX
import app.airsignal.weather.dao.StaticDataObject.SHOWING_LOADING_FLOAT
import app.airsignal.weather.dao.StaticDataObject.SO2_INDEX
import app.airsignal.weather.databinding.ActivityMainBinding
import app.airsignal.weather.db.room.model.GpsEntity
import app.airsignal.weather.db.room.repository.GpsRepository
import app.airsignal.weather.firebase.admob.AdViewClass
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.gps.GetLocation
import app.airsignal.weather.login.SilentLoginClass
import app.airsignal.weather.repo.BaseRepository
import app.airsignal.weather.util.*
import app.airsignal.weather.util.`object`.DataTypeParser.applySkyImg
import app.airsignal.weather.util.`object`.DataTypeParser.applySkyText
import app.airsignal.weather.util.`object`.DataTypeParser.convertDateAppendZero
import app.airsignal.weather.util.`object`.DataTypeParser.convertDayOfWeekToKorean
import app.airsignal.weather.util.`object`.DataTypeParser.convertLocalDateTimeToLong
import app.airsignal.weather.util.`object`.DataTypeParser.convertTimeToMinutes
import app.airsignal.weather.util.`object`.DataTypeParser.convertValueToGrade
import app.airsignal.weather.util.`object`.DataTypeParser.getComparedTemp
import app.airsignal.weather.util.`object`.DataTypeParser.getCurrentTime
import app.airsignal.weather.util.`object`.DataTypeParser.getDataColor
import app.airsignal.weather.util.`object`.DataTypeParser.getDataText
import app.airsignal.weather.util.`object`.DataTypeParser.getHourCountToTomorrow
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgSmall
import app.airsignal.weather.util.`object`.DataTypeParser.isRainyDay
import app.airsignal.weather.util.`object`.DataTypeParser.millsToString
import app.airsignal.weather.util.`object`.DataTypeParser.modifyCurrentRainType
import app.airsignal.weather.util.`object`.DataTypeParser.modifyCurrentTempType
import app.airsignal.weather.util.`object`.DataTypeParser.translateSkyText
import app.airsignal.weather.util.`object`.DataTypeParser.translateUV
import app.airsignal.weather.util.`object`.GetAppInfo
import app.airsignal.weather.util.`object`.GetAppInfo.getEntireSun
import app.airsignal.weather.util.`object`.GetAppInfo.getInitBackLogPerm
import app.airsignal.weather.util.`object`.GetAppInfo.getIsNight
import app.airsignal.weather.util.`object`.GetAppInfo.getTopicNotification
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLastAddress
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLocation
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLoginPlatform
import app.airsignal.weather.util.`object`.GetAppInfo.isPermedBackLoc
import app.airsignal.weather.util.`object`.GetSystemInfo.getLocale
import app.airsignal.weather.util.`object`.GetSystemInfo.isThemeNight
import app.airsignal.weather.util.`object`.SetAppInfo.removeSingleKey
import app.airsignal.weather.util.`object`.SetAppInfo.setCurrentLocation
import app.airsignal.weather.util.`object`.SetAppInfo.setUserLastAddr
import app.airsignal.weather.util.`object`.SetSystemInfo.setUvBackgroundColor
import app.airsignal.weather.view.*
import app.airsignal.weather.vmodel.GetWeatherViewModel
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
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
    private val vib by lazy { VibrateUtil(this) }
    private val getDataViewModel by viewModel<GetWeatherViewModel>()

    private val dailyWeatherList = ArrayList<AdapterModel.DailyWeatherItem>()
    private val weeklyWeatherList = ArrayList<AdapterModel.WeeklyWeatherItem>()
    private val uvLegendList = ArrayList<AdapterModel.UVLegendItem>()
    private val uvResponseList = ArrayList<AdapterModel.UVResponseItem>()
    private val dailyWeatherAdapter by lazy { DailyWeatherAdapter(this, dailyWeatherList) }
    private val weeklyWeatherAdapter by lazy { WeeklyWeatherAdapter(this, weeklyWeatherList) }
    private val reportViewPagerItem = ArrayList<String>()
    private val warningViewPagerAdapter by lazy {
        WarningViewPagerAdapter(
            this,
            reportViewPagerItem,
            binding.mainWarningVp
        )
    }
    private val reportArrayList = ArrayList<String>()
    private val uvLegendAdapter = UVLegendAdapter(this, uvLegendList)
    private val uvResponseAdapter = UVResponseAdapter(this, uvResponseList)
    private val airQList = ArrayList<AdapterModel.AirQTitleItem>()
    private val airQAdapter = AirQTitleAdapter(this, airQList)
    private var currentSun = 0
    private var isSunAnimated = false
    private var isProgressed = false
    private var isWarned = false
    private val sunPb by lazy { SunProgress(binding.seekArc) }
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

    private val adView by lazy { AdViewClass(this) }

    override fun onResume() {
        super.onResume()
        addSideMenu()
        getDataSingleTime(isCurrent = false)
        adView.loadAdView(binding.nestedAdView)  // adView 생성
        binding.nestedAdView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        isProgressed = false
        isWarned = false
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
        }

        if (getInitBackLogPerm(this)) {
            if (VERSION.SDK_INT >= 29) {
                if (RequestPermissionsUtil(this).isBackgroundRequestLocation()) {
                    createWorkManager()
                }
            } else {
                if (isPermedBackLoc(this)) {
                    createWorkManager()
                }
            }
        }

        initBinding()

        binding.dataVM = getDataViewModel

        initializing()


        sunPb.disableTouch()    // 일출/일몰 그래프 클릭 방지

        // 메인 하단 스크롤 유도 화살표 애니메이션 적용
        val bottomArrowAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_arrow_anim)
        binding.mainMotionSLideImg.startAnimation(bottomArrowAnim)

        // UV 범주 아이템 추가
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

        // 스크롤 최상단으로 올리기 버튼
        binding.nestedFab.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                binding.nestedScrollview.smoothScrollTo(0, 0, 500)
            }
        })

        binding.nestedScrollview.setOnScrollChangeListener { v, _, _, _, _ ->
            // 스크롤이 최하단일 경우 최초 한번만 일출/일몰 그래프 애니메이션
            if (!v.canScrollVertically(1)) {
                if (!isSunAnimated) {
                    sunPb.animate(currentSun)
                    isSunAnimated = true
                }
            }

//            // 하단 스크롤시 네비게이션 바 색상 하얀색으로 변경
//            if (v.scrollY == 0) {
//                window.navigationBarColor = getColor(android.R.color.transparent)
//                binding.nestedFab.apply { alpha = 0f }
//            } else {
//                window.navigationBarColor = getColor(R.color.theme_view_color)
//                binding.nestedFab.apply { alpha = 1f }
//            }
        }

        // 데이터 갱신 버튼 클릭
        binding.mainRefreshData.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                v!!.startAnimation(rotateAnim)
                mVib()
                getDataSingleTime(isCurrent = false)
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
                v!!.startAnimation(rotateAnim)
                getDataSingleTime(isCurrent = true)
            }
        })

        // 사이드 메뉴 세팅
        binding.mainSideMenuIv.setOnClickListener(object : OnSingleClickListener() {
            @SuppressLint("InflateParams")
            override fun onSingleClick(v: View?) {
                sideMenuBuilder.show(sideMenuView, true)
            }
        })

        // 공유하기 버튼 클릭
        binding.mainShareIv.setOnClickListener(object : OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                val doubleDialog = MakeDoubleDialog(this@MainActivity)
                if (getUserLocation(this@MainActivity) == LANG_EN) {
                    doubleDialog.make(
                        "Share with in English?",
                        "Yes",
                        "With in Korean",
                        R.color.main_blue_color
                    ).apply {
                        this.first.setOnClickListener(object : OnSingleClickListener() {
                            override fun onSingleClick(v: View?) {
                                doubleDialog.dismiss()
                                addShareMsg(LANG_EN)
                            }
                        })
                        this.second.setOnClickListener(object : OnSingleClickListener() {
                            override fun onSingleClick(v: View?) {
                                doubleDialog.dismiss()
                                addShareMsg(LANG_KR)
                            }
                        })
                    }
                } else {
                    doubleDialog.dismiss()
                    addShareMsg(LANG_KR)
                }
            }
        })
    }

    // 공유하기 언어별 대응
    private fun addShareMsg(locale: String) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "text/plain"
        if (locale == LANG_EN) {
            intent.putExtra(
                Intent.EXTRA_TEXT, "${
                    "The weather ${binding.mainTopBarGpsTitle.text} is ${binding.mainLiveTempValue.text}˚ " +
                            "${translateSkyText(binding.mainSkyText.text.toString())}. The chance of rain is ${binding.subAirRainP.getValue().text}," +
                            " and the humidity is ${binding.subAirHumid.getValue().text}"
                }\n\n${"Click the link for real-time weather information on Airsignal\n$playStoreURL"}"
            )
            startActivity(Intent.createChooser(intent, "Share weather data"))

        } else {
            intent.putExtra(
                Intent.EXTRA_TEXT, "${
                    "현재 ${binding.mainTopBarGpsTitle.text}의 날씨는 ${binding.mainLiveTempValue.text}˚로 ${binding.mainSkyText.text}입니다. " +
                            "강수확률은 ${binding.subAirRainP.getValue().text}이고 습도는 ${binding.subAirHumid.getValue().text}입니다."
                }\n\n${"에어시그널의 실시간 날씨 정보를 알고싶다면 아래 링크를 클릭하세요.\n$playStoreURL"}"
            )
            startActivity(Intent.createChooser(intent, "날씨 데이터 공유하기"))
        }
    }

    // 햄버거 메뉴 세팅
    private fun addSideMenu() {
        try {
            val cancel = sideMenuView.findViewById<ImageView>(R.id.headerCancel)
            val profile = sideMenuView.findViewById<ImageView>(R.id.navHeaderProfileImg)
            val id = sideMenuView.findViewById<TextView>(R.id.navHeaderUserId)
            val weather = sideMenuView.findViewById<TextView>(R.id.navMenuWeather)
            val setting = sideMenuView.findViewById<TextView>(R.id.navMenuSetting)
            val warning = sideMenuView.findViewById<TextView>(R.id.navMenuWarning)
            val headerTr = sideMenuView.findViewById<TableRow>(R.id.headerTr)
            val adView = sideMenuView.findViewById<AdView>(R.id.navMenuAdview)

            sideMenuBuilder.apply {
                setBackPressed(cancel)
                setUserData(profile, id)
                AdViewClass(this@MainActivity).loadAdView(adView)
            }

            if (getUserLocation(this) == LANG_EN ||
                    getLocale(this) == Locale.ENGLISH
            ) {
                warning.visibility = GONE
            } else {
                warning.visibility = VISIBLE
                warning.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        sideMenuBuilder.dismiss()
                        EnterPageUtil(this@MainActivity).toWarning()
                    }
                })
            }

            headerTr.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if (getUserLoginPlatform(this@MainActivity) == "")
                        EnterPageUtil(this@MainActivity).toLogin()
                }
            })
            weather.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    sideMenuBuilder.dismiss()
                }
            })
            setting.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    CompletableFuture.supplyAsync {
                        sideMenuBuilder.dismiss()
                    }.thenAccept {
                        val intent = Intent(this@MainActivity, SettingActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            })
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
            binding.mainDailyWeatherRv.setPadding(22, 0, 15, 0)
            smoothScroller.targetPosition = position + 5
            it.startSmoothScroll(smoothScroller)
        }
    }

    // 시간별 날씨 색션 컬러 변경
    private fun setSectionTextColor(t1: TextView, t2: TextView, t3: TextView) {
        if (dailyWeatherAdapter.getIsWhite()) {
            t1.typeface = Typeface.createFromAsset(assets, "spoqa_hansansneo_bold.ttf")
            t2.typeface = Typeface.createFromAsset(assets, "spoqa_hansansneo_regular.ttf")
            t3.typeface = Typeface.createFromAsset(assets, "spoqa_hansansneo_regular.ttf")
            t1.setTextColor(getColor(R.color.white))
            t2.setTextColor(getColor(R.color.sub_white))
            t3.setTextColor(getColor(R.color.sub_white))
        } else {
            t1.typeface = Typeface.createFromAsset(assets, "spoqa_hansansneo_medium.ttf")
            t2.typeface = Typeface.createFromAsset(assets, "spoqa_hansansneo_regular.ttf")
            t3.typeface = Typeface.createFromAsset(assets, "spoqa_hansansneo_regular.ttf")
            t1.setTextColor(getColor(R.color.main_blue_color))
            t2.setTextColor(getColor(R.color.sub_black))
            t3.setTextColor(getColor(R.color.sub_black))
        }
    }

    // 날씨 데이터 API 호출
    private fun getDataSingleTime(isCurrent: Boolean) {
        if (RequestPermissionsUtil(this).isNetworkPermitted()) {
            if (RequestPermissionsUtil(this).isLocationPermitted()) {
                val lastAddress = getUserLastAddress(this)
                if (!isCurrent) {
                    val addrArray = resources.getStringArray(R.array.address_korean)
                    if (addrArray.contains(lastAddress)) {
                        addrArray.forEachIndexed { index, s ->
                            if (lastAddress == s) {
                                loadSavedAddr(
                                    addrArray[index],
                                    resources.getStringArray(R.array.address_english)[index]
                                )
                            }
                        }
                    } else {
                        getCurrentLocationData()
                    }
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
    private fun loadSavedAddr(addr: String?, enAddr: String?) {
        addr?.let {
            loadSavedViewModelData(it)

            RDBLogcat.writeGpsHistory(
                this, isSearched = true,
                gpsValue = addr,
                responseData = null
            )

            val gps = if (getUserLocation(this) == LANG_EN) enAddr?.trim() else addr.trim()

            binding.mainGpsTitleTv.text = gps
            binding.mainTopBarGpsTitle.text = gps!!.split(" ").last()
        }
    }

    // 프로그래스 보이기
    private fun showPB() {
        if (binding.mainMotionLayout.alpha == NOT_SHOWING_LOADING_FLOAT) {
            binding.mainMotionLayout.alpha = SHOWING_LOADING_FLOAT
            binding.mainMotionLayout.isInteractionEnabled = false // 모션 레이아웃의 스와이프를 막음
            binding.mainMotionLayout.isEnabled = false
        }
    }

    // 프로그래스 숨기기
    private fun hidePB() {
        if (binding.mainMotionLayout.alpha == SHOWING_LOADING_FLOAT) {
            binding.mainMotionLayout.alpha = NOT_SHOWING_LOADING_FLOAT
            binding.mainMotionLayout.isInteractionEnabled = true // 모션 레이아웃의 스와이프를 허용
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

        // 자동 로그인
        SilentLoginClass().login(this@MainActivity)

        // 어댑터 바인딩
        binding.mainDailyWeatherRv.adapter = dailyWeatherAdapter
        binding.mainWeeklyWeatherRv.adapter = weeklyWeatherAdapter
        binding.mainUVLegendRv.adapter = uvLegendAdapter
        binding.mainUvCollapseRv.adapter = uvResponseAdapter
        binding.nestedAirRv.adapter = airQAdapter

        // 기상특보 뷰페이저 세팅
        binding.mainWarningVp.apply {
            adapter = warningViewPagerAdapter
            isClickable = true
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
        }

        binding.mainUvCollapseRv.isClickable = false

        // adView 닫기 클릭
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
                                // 오늘
                                sectionList[0] -> {
                                    setSectionTextColor(
                                        todaySection,
                                        tomorrowSection,
                                        afterTomorrowSection
                                    )
                                }
                                // 내일
                                sectionList[1] -> {
                                    setSectionTextColor(
                                        tomorrowSection,
                                        todaySection,
                                        afterTomorrowSection
                                    )
                                }
                                // 모레
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
                        model.name, model.nameKR,
                        model.value, model.unit
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

        // 외부 공기질 도움말 아이콘 이미지 설정
        binding.nestedAirHelp.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.help, null)
        )

        // 외부 공기질 도움말 클릭
        binding.nestedAirHelp.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (binding.nestedAirHelpPopup.alpha == 0f) {
                    val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
                    // 팝업 다이얼로그 생성
                    binding.nestedAirHelpPopup.apply {
                        bringToFront()
                        airQList.forEach {
                            try {
                                if (it.isSelect) {
                                    fetchData(
                                        modifyDataSort(this@MainActivity, it.nameKR),
                                        modifyDataGraph(this@MainActivity, it.nameKR)!!,
                                        it.name, it.nameKR
                                    )
                                    return@forEach
                                }
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }
                        }
                        startAnimation(fadeIn)
                        alpha = 1f
                    }
                } else {
                    val fadeOut = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out)
                    binding.nestedAirHelpPopup.apply {
                        startAnimation(fadeOut)
                        alpha = 0f
                    }
                    binding.nestedAirRv.bringToFront()
                }
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 뒤로가기 한번 클릭 시 토스트
        if (!isBackPressed) {
            ToastUtils(this)
                .showMessage(getString(R.string.back_press), 2)
            isBackPressed = true
        }
        // 2초안에 한번 더 클릭 시 종료
        else {
            removeSingleKey(this, lastAddress)
            EnterPageUtil(this).fullyExit()
        }
        // 2초간 스레드 유지
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
        if (!getDataViewModel.fetchData().hasActiveObservers()) {
            applyGetDataViewModel()
        }
    }

    // 뷰모델에서 Observing 한 데이터 결과 적용
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    fun applyGetDataViewModel(): MainActivity {
        getDataViewModel.fetchData().observe(this) { entireData ->
            entireData?.let { eData ->
                when (eData) {
                    // 통신 성공
                    is BaseRepository.ApiState.Success -> {
                        try {
                            binding.mainDailyWeatherRv.scrollToPosition(0)
                            binding.mainWarningVp.currentItem = 0
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
                            val terms24 = result.term24

                            currentSun = GetAppInfo.getCurrentSun(sun.sunrise!!, sun.sunset!!)

                            airQList.clear()

                            // 주간 오전 날씨
                            val wfMin = listOf(
                                week.wf0Am, week.wf1Am, week.wf2Am, week.wf3Am,
                                week.wf4Am, week.wf5Am, week.wf6Am, week.wf7Am
                            )
                            // 주간 오후 날씨
                            val wfMax = listOf(
                                week.wf0Pm, week.wf1Pm, week.wf2Pm, week.wf3Pm,
                                week.wf4Pm, week.wf5Pm, week.wf6Pm, week.wf7Pm
                            )
                            // 주간 최저 기온
                            val taMin = listOf(
                                week.taMin0, week.taMin1, week.taMin2, week.taMin3,
                                week.taMin4, week.taMin5, week.taMin6, week.taMin7
                            )
                            // 주간 최고 기온
                            val taMax = listOf(
                                week.taMax0, week.taMax1, week.taMax2, week.taMax3, week.taMax4,
                                week.taMax5, week.taMax6, week.taMax7
                            )

                            dailyWeatherList.clear()
                            weeklyWeatherList.clear()

                            current.temperature?.let { currentTemp ->
                                realtime.let {
                                    val temp = modifyCurrentTempType(
                                        currentTemp,
                                        realtime.temp
                                    ).toString()
                                    binding.mainLiveTempValue.text =
                                        temp
                                    binding.mainLiveTempUnit.text = "˚"

                                    binding.mainLiveTempValueC.text =
                                        "$temp˚"

                                }
                            }

                            realtime.let {
                                binding.subAirHumid.fetchData(
                                    "${
                                        realtime.humid!!
                                            .roundToInt()
                                    }%", R.drawable.ico_main_humidity,
                                    null
                                )

                                binding.subAirWind.fetchData(
                                    "${
                                        realtime.windSpeed!!
                                            .roundToInt()
                                    }m/s", R.drawable.ico_main_wind,
                                    realtime.vector
                                )

                                val rainP = "${realtime.rainP!!.roundToInt()}%"
                                binding.subAirRainP.fetchData(
                                    rainP, R.drawable.ico_main_rain,
                                    null
                                )
                            }

                            today?.let {
                                binding.mainMinValue.text = filteringNullData(it.min!!)+"˚"
                                binding.mainMaxValue.text = filteringNullData(it.max!!)+"˚"
                                binding.mainMinMaxValueC.text =
                                    "${filteringNullData(it.min)}˚/${filteringNullData(it.max)}˚"
                            }

                            updateAirQData(
                                PM2p5_INDEX, getString(R.string.pm2_5_full), "PM2.5",
                                "㎍/㎥", air.pm25Value!!.toInt().toString()
                            )
                            updateAirQData(
                                PM10_INDEX, getString(R.string.pm10_full), "PM10",
                                "㎍/㎥", air.pm10Value!!.toInt().toString()
                            )
                            updateAirQData(
                                CO_INDEX, getString(R.string.co_full), "CO",
                                "ppm", air.coValue!!.toString()
                            )
                            updateAirQData(
                                SO2_INDEX, getString(R.string.so2_full), "SO2",
                                "ppm", air.so2Value!!.toString()
                            )
                            updateAirQData(
                                NO2_INDEX, getString(R.string.no2_full), "NO2",
                                "ppm", air.no2Value!!.toString()
                            )
                            updateAirQData(
                                O3_INDEX, getString(R.string.o3_full), "O3",
                                "ppm", air.o3Value!!.toString()
                            )

                            applyAirQView(
                                "PM2.5", getString(R.string.pm2_5_full),
                                air.pm25Value.toInt().toString(), "㎍/m3"
                            )

                            airQList[PM2p5_INDEX].isSelect = true   // 초기 데이터 = 초미세먼지

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

                            // 일출/일몰 세팅
                            val sbRise = StringBuffer().append(sun.sunrise).insert(2, ":")
                            val sbSet = StringBuffer().append(sun.sunset).insert(2, ":")
                            val sbRiseTom =
                                StringBuffer().append(sunTomorrow!!.sunrise).insert(2, ":")
                            val sbSetTom = StringBuffer().append(sunTomorrow.sunset).insert(2, ":")
                            binding.mainSunRiseTime.text = sbRise
                            binding.mainSunSetTime.text = sbSet
                            binding.mainSunRiseTom.text = sbRiseTom
                            binding.mainSunSetTom.text = sbSetTom

                            // 기온 비교 세팅
                            getCompareTempText(
                                yesterday.temp!!,
                                modifyCurrentTempType(current.temperature, realtime.temp),
                                binding.mainCompareTempTv
                            )

                            // 메인 날씨 아이콘 세팅
                            binding.mainSkyImg.setImageDrawable(
                                applySkyImg(
                                    this,
                                    modifyCurrentRainType(current.rainType, realtime.rainType),
                                    realtime.sky, thunder,
                                    isLarge = true, isNight = getIsNight(currentSun)
                                )
                            )

                            val skyText = applySkyText(
                            this,
                            modifyCurrentRainType(current.rainType, realtime.rainType),
                            realtime.sky, thunder
                            )
                            binding.mainSkyText.text = skyText
                            // 날씨에 따라 배경화면 변경
                            applyWindowBackground(
                                currentSun, skyText
                            )

                            changeStrokeColor(binding.subAirPM25,
                                getDataColor(this,
                                convertValueToGrade("PM2.5",air.pm25Value.toDouble())))

                            changeStrokeColor(binding.subAirPM10,
                                getDataColor(this,
                                    convertValueToGrade("PM10",air.pm10Value.toDouble())))

                            reportViewPagerItem.clear()
                            reportArrayList.clear()
                            if (getUserLocation(this) == LANG_EN){
                                binding.mainWarningBox.setBackgroundColor(getColor(android.R.color.transparent))
                            } else {
                                // 기상특보 세팅
                                result.summary?.let { sList ->
                                    sList.forEachIndexed { index, summary ->
                                        val item = summary.replace("○", "")
                                            .replace("\n", "")
                                            .trim()
                                        reportArrayList.add(item)

                                        if (index == sList.lastIndex) {
                                            if (reportArrayList.size == 0) {
                                                binding.mainWarningBox.setBackgroundColor(getColor(android.R.color.transparent))
                                            } else {
                                                if (!isWarned) {
                                                    warningSlideAuto()
                                                    isWarned = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            binding.subAirPM25.text = "${getString(R.string.pm2_5_full)}   ${air.pm25Value.toInt()}"
                            binding.subAirPM10.text = "${getString(R.string.pm10_full)}   ${air.pm10Value.toInt()}"

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

                            // 시간별 날씨 아이템 추가
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
                                    )) / getEntireSun(sun.sunrise, sun.sunset)

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

                            // 주간별 날씨 아이템 추가
                            for (i: Int in 0 until (7)) {
                                try {
                                    val formedDate = dateNow.plusDays(i.toLong())
                                    val date: String = when (i) {
                                        0 -> { getString(R.string.today) }
                                        1 -> { getString(R.string.tomorrow) }
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

                            // 24절기 세팅
                            terms24?.let { term ->
                                val bundle = Term24Class().getTerms24Bundle(term)
                                bundle?.let { b ->
                                    binding.nestedTerms24Box.visibility = VISIBLE
                                    binding.mainTermsTitle.text = b.getString("title")
                                    binding.mainTermsDate.text = b.getString("date")
                                    binding.mainTermsExplain.text = b.getString("explain")
                                } ?: run {
                                    binding.nestedTerms24Box.visibility = GONE
                                }
                            } ?: run {
                                binding.nestedTerms24Box.visibility = GONE
                            }

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
                                    responseData = "${getUserLastAddress(this)},${result}"
                                )
                            }
                        } catch(e: Exception) {
                            hidePB()
                            when(e) {
                                is NullPointerException -> {
                                    runOnUiThread {
                                        hideAllViews(error = ERROR_API_PROTOCOL)
                                    }
                                }
                                is IndexOutOfBoundsException -> {
                                    if (GetLocation(this).isNetWorkConnected()) {
                                        hideAllViews(error = ERROR_GET_DATA)
                                    } else {
                                        hideAllViews(error = ERROR_NETWORK)
                                    }
                                }
                                else -> throw e
                            }
                        }
                    }

                    // 통신 실패
                    is BaseRepository.ApiState.Error -> {
                        runOnUiThread {
                            hidePB()
                            if (GetLocation(this).isNetWorkConnected()) {
                                hideAllViews(error = eData.errorMessage)
                            } else {
                                hideAllViews(error = ERROR_NETWORK)
                            }
                        }
                    }

                    // 통신 중
                    is BaseRepository.ApiState.Loading -> {
                        runOnUiThread { showPB() }
                    }
                }
            }
        }
        return this
    }

    // 외부 공기질 데이터 아이템 추가
    private fun applyAirQView(
        name: String,
        nameKR: String,
        value: String,
        unit: String
    ) {
        val grade = convertValueToGrade(name, value.toDouble())
        binding.nestedAirCpvCard.setCardBackgroundColor(getDataColor(this, grade))
        binding.nestedAirCpvText.text = getDataText(this, grade)
        binding.nestedAirValue.text = value
        binding.nestedAirTitleEn.text = name
        binding.nestedAirTitleKr.text = nameKR
        binding.nestedAirUnit.text = unit
        binding.nestedAirValue.setTextColor(getDataColor(this, grade))
    }

    // 하늘상태에 따라 윈도우 배경 변경
    private fun applyWindowBackground(progress: Int, sky: String?) {
        if (getIsNight(progress)) {
            window.setBackgroundDrawableResource(R.drawable.main_bg_night)
            binding.mainSkyStarImg.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.bg_nightsky, null)
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

    // 필드값이 없을 때 -100 출력 됨
    private fun filteringNullData(data: Double): String {
        return if (data != -100.0 && data != 100.0) data.roundToInt().toString() else ""
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
    @SuppressLint("SetTextI18n")
    private fun hideAllViews(error: String?) {
        binding.mainErrorRenewBtn.apply {
            text = getString(R.string.renew_data)
            // 에러 버튼 클릭
            setOnClickListener {
                mVib()
                getDataSingleTime(isCurrent = false)
            }
        }
        when (error) {
            ERROR_API_PROTOCOL, ERROR_SERVER_CONNECTING -> {
                binding.mainErrorTitle.text = getString(R.string.api_call_error)
            }
            ERROR_NOT_SERVICED_LOCATION -> {
                binding.mainErrorTitle.text = getString(R.string.not_serviced_location)
                binding.mainErrorRenewBtn.apply {
                    text = getString(R.string.register_new_address)
                    setOnClickListener {
                        mVib()
                        val bottomSheet =
                            SearchDialog(
                                this@MainActivity, 1, supportFragmentManager,
                                BottomSheetDialogFragment().tag
                            )
                        bottomSheet.show(1)
                    }
                }
            }
            ERROR_TIMEOUT -> {
                binding.mainErrorTitle.text = getString(R.string.timeout_error)
            }
            ERROR_NETWORK -> {
                binding.mainErrorTitle.text = getString(R.string.network_error)
                binding.mainErrorRenewBtn.apply {
                    text = getString(R.string.renew_data)
                    // 에러 버튼 클릭
                    setOnClickListener {
                        mVib()
                        getDataSingleTime(isCurrent = false)
                    }
                }
            }
            ERROR_GET_LOCATION_FAILED -> {
                binding.mainErrorTitle.text = getString(R.string.address_call_error)
            }
            ERROR_GPS_CONNECTED -> {
                binding.mainErrorTitle.text = getString(R.string.gps_call_error)
                binding.mainErrorRenewBtn.apply {
                    text = getString(R.string.enable_gps)
                    // 에러 버튼 클릭
                    setOnClickListener {
                        mVib()
                        GetLocation(this@MainActivity).requestSystemGPSEnable()
                    }
                }
            }
            ERROR_GET_DATA -> {
                binding.mainErrorTitle.text = getString(R.string.data_call_error)
            }
            else -> {
                RDBLogcat.writeErrorNotANR(this, ERROR_LOCATION_FAILED, error!!)
                binding.mainErrorTitle.text = getString(R.string.unknown_error)
            }
        }

        // 주소, 갱신버튼, 현재기온, 기온비교, 최저/최고 기온, 날씨 정보 더보기, 모션 슬라이드 막기
        binding.mainSkyImg.apply {
            if (isThemeNight(this@MainActivity)) {
                window.setBackgroundDrawableResource(R.color.black)
                this.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ico_error_b, null
                    )
                )
            } else {
                window.setBackgroundDrawableResource(R.color.white)
                this.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ico_error_w, null
                    )
                )
            }

            setVisibilityForViews(GONE, error)
        }
    }

    // 통신에 성공할 경우 레이아웃 처리
    private fun showAllViews() {
        setVisibilityForViews(VISIBLE, null)
    }

    // 레이아웃 숨김처리에 따른 뷰 세팅
    private fun setVisibilityForViews(visibility: Int, error: String?) {
        val textViewArray = arrayListOf(
            binding.mainGpsTitleTv,
            binding.mainLiveTempValue,
            binding.mainLiveTempUnit,
            binding.mainMotionSlideGuide,
            binding.mainTopBarGpsTitle,
            binding.mainSensTitle,
            binding.mainSensValue,
            binding.mainMaxValue,
            binding.mainMaxTitle,
            binding.mainMinValue,
            binding.mainMinTitle,
            binding.subAirPM25,
            binding.subAirPM10,
            binding.subAirHumid.getTitle(),
            binding.subAirWind.getTitle(),
            binding.subAirRainP.getTitle(),
            binding.mainCompareTempTv
        )

        // 숨김
        if (visibility == GONE) {
            if (error == ERROR_NETWORK ||
                error == ERROR_GET_DATA
            ) {
                binding.mainAddAddress.setImageDrawable(null)
                binding.mainSideMenuIv.setImageDrawable(null)
            } else {
                binding.mainAddAddress.imageTintList =
                    ColorStateList.valueOf(getColor(R.color.theme_text_color))
                binding.mainSideMenuIv.imageTintList =
                    ColorStateList.valueOf(getColor(R.color.theme_text_color))
            }

            textViewArray.forEach {
                it.text = ""
            }

            binding.mainGpsFix.setImageDrawable(null)
            binding.mainMotionSLideImg.setImageDrawable(null)
            binding.mainRefreshData.setImageDrawable(null)

            binding.mainMotionLayout.apply {
                transitionToStart()
                Thread.sleep(100)
                isInteractionEnabled = false // 모션 레이아웃의 스와이프를 막음
            }

            binding.mainErrorTitle.alpha = 1f
            binding.mainErrorRenewBtn.alpha = 1f
            binding.mainErrorRenewBtn.isClickable = true
            applyBackground(binding.mainWarningBox,null)
            applyBackground(binding.nestedSubAirFrame,null)
            binding.mainWarningVp.alpha = 0f
            binding.subAirHumid.alpha = 0f
            binding.subAirWind.alpha = 0f
            binding.subAirRainP.alpha = 0f

            changeStrokeColor(binding.subAirPM10, getColor(android.R.color.transparent))
            changeStrokeColor(binding.subAirPM25, getColor(android.R.color.transparent))

        }
        // 보임
        else {
            binding.mainSensTitle.text = getString(R.string.sens_temp)
            binding.mainMotionSlideGuide.text = getString(R.string.slide_more)
            binding.subAirHumid.getTitle().text = getString(R.string.humidity)
            binding.subAirWind.getTitle().text = getString(R.string.wind)
            binding.subAirRainP.getTitle().text = getString(R.string.rainPer)
            applyBackground(binding.mainWarningBox, R.drawable.report_frame_bg)
            binding.mainWarningVp.alpha = 1f
            binding.mainErrorTitle.alpha = 0f
            binding.mainErrorRenewBtn.alpha = 0f
            binding.mainErrorRenewBtn.isClickable = false
            binding.subAirHumid.alpha = 1f
            binding.subAirWind.alpha = 1f
            binding.subAirRainP.alpha = 1f

            binding.mainMinTitle.text = getString(R.string.min)
            binding.mainMaxTitle.text = getString(R.string.max)

            binding.mainGpsFix.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.gps_fix, null)
            )

            binding.mainMotionSLideImg.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.drop_down_bottom, null)
            )
            binding.mainRefreshData.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.refresh, null)
            )
            binding.mainAddAddress.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ico_add_w, null)
            )
            binding.mainSideMenuIv.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ico_hamb_w, null)
            )
            // 원래 상태로 복구하기 위해 제약 조건 변경
            binding.mainMotionLayout.isInteractionEnabled = true
        }
    }

    // 텍스트뷰 테두리 색상 변경
    private fun changeStrokeColor(textView: TextView, color: Int) {
        // 특정 상황에 맞게 원하는 색상으로 변경
        textView.setTextColor(color)
        textView.background.mutate().let { background ->
            if (background is GradientDrawable) {
                background.setStroke(3, color) // 테두리 두께와 색상 변경
            }
        }
    }

    // 현재 지역의 날씨 데이터 뷰모델 생성 및 호출
    private fun loadCurrentViewModelData(lat: Double, lng: Double) {
        getDataObservers()
        getDataViewModel.loadData(lat, lng, null)
    }

    // 저장된 지역의 날씨 데이터 뷰모델 생성 및 호출
    private fun loadSavedViewModelData(addr: String) {
        getDataObservers()
        getDataViewModel.loadData(null, null, addr)
    }

    // 외부 공기질 아이템 추가
    private fun addAirQItem(
        position: Int, nameKR: String, name: String, unit: String,
        value: String, grade: Int
    ) {
        val item = AdapterModel.AirQTitleItem(
            false, position, nameKR,
            name, unit, value, grade
        )

        this.airQList.add(position, item)
    }

    // 외부 공기질 데이터 갱신
    private fun updateAirQData(
        position: Int, nameKR: String, name: String, unit: String,
        value: String
    ) {

        if (!airQList.contains(
                AdapterModel.AirQTitleItem(
                    false, position, nameKR,
                    name, unit, value, convertValueToGrade(name, value.toDouble())
                )
            )
        ) {
            addAirQItem(
                position, nameKR,
                name, unit, value, convertValueToGrade(name, value.toDouble())
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
        model.position = -1
        model.lat = lat
        model.lng = lng
        model.addrKr = addr
        model.addrEn = addr
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
    }

    // 자외선 단계별 대응요령 아이템 추가
    private fun addUvResponseItem(text: String) {
        val item = AdapterModel.UVResponseItem(text)

        uvResponseList.add(item)
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
    }

    // 메인화면 배경에 따라 텍스트의 색상을 변경
    @SuppressLint("UseCompatTextViewDrawableApis","NotifyDataSetChanged")
    private fun changeTextColorStyle(sky: String, isNight: Boolean) {
        val changeColorTextViews = listOf(
            binding.mainLiveTempValue,binding.mainLiveTempUnit,binding.mainCompareTempTv,
            binding.mainTopBarGpsTitle,binding.mainMotionSlideGuide,
            binding.mainGpsTitleTv,binding.mainSensTitle,binding.mainSensValue,
            binding.mainLiveTempTitleC,binding.subAirWind.getTitle(),
            binding.subAirRainP.getTitle(),binding.subAirHumid.getTitle(),binding.subAirWind.getValue(),
            binding.subAirRainP.getValue(),binding.subAirHumid.getValue(),
            binding.mainLiveTempValueC,binding.mainSensTitleC,binding.mainSensValueC,
            binding.mainMinMaxTitleC,binding.mainMinMaxValueC,binding.mainDailyWeatherTitle,
            binding.mainWeeklyWeatherTitle,binding.nestedAirTitle,binding.mainUvTitle,
            binding.mainSunRiseTitle,binding.mainSunSetTitle,binding.mainSunRiseTime,
            binding.mainSunSetTime,binding.mainSunTomTitle,binding.mainUvCollapsedTitle,
            binding.nestedAirTitleEn,binding.dailySectionTomorrow,binding.dailySectionAfterTomorrow,
            binding.mainSunSetTom, binding.mainSunRiseTom
        )
        val changeColorSubTextViews = listOf(
            binding.mainLicenseText,binding.nestedAirTitleKr,binding.nestedAirUnit,
        )
        val changeTintLineViews = listOf(
            binding.nestedAirLine,binding.mainSunLine,binding.mainUvLine
        )
        val changeTintImageViews = listOf(
            binding.mainSideMenuIv, binding.mainAddAddress,
            binding.mainGpsFix, binding.mainMotionSLideImg,
            binding.mainRefreshData, binding.mainShareIv,
            binding.adViewCancelIv, binding.nestedAirHelp
        )
        val changeBoxViews = listOf(
            binding.mainWarningBox,binding.nestedSubAirFrame,
            binding.nestedDailyBox, binding.nestedWeeklyBox, binding.adViewBox,
            binding.nestedAirBox, binding.mainUVBox, binding.mainSunBox
        )

        setSectionTextColor(
            binding.dailySectionToday,
            binding.dailySectionTomorrow,
            binding.dailySectionAfterTomorrow)

        // 글자색 white 로 변경
        @Suppress("DEPRECATION")
        fun white() {
            binding.subAirWind.getValue().compoundDrawableTintList =
                ColorStateList.valueOf(getColor(R.color.white))

            changeBoxViews.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10000000"))
            }
            changeColorTextViews.forEach {
                it.setTextColor(getColor(R.color.white))
            }
            changeColorSubTextViews.forEach {
                it.setTextColor(getColor(R.color.sub_white))
            }
            changeTintLineViews.forEach {
                it.setBackgroundColor(getColor(R.color.sub_white))
            }
            changeTintImageViews.forEach {
                it.imageTintList = ColorStateList.valueOf(getColor(R.color.white))
            }

            binding.dailySectionTomorrow.setTextColor(getColor(R.color.sub_white))
            binding.dailySectionAfterTomorrow.setTextColor(getColor(R.color.sub_white))

            binding.mainTopBarGpsTitle.compoundDrawablesRelative[0].mutate()
                .setTint(ResourcesCompat.getColor(resources, R.color.white, null))
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

            dailyWeatherAdapter.setIsWhite(true)
            weeklyWeatherAdapter.setIsWhite(true)
            uvLegendAdapter.setIsWhite(true)
            uvResponseAdapter.setIsWhite(true)
            airQAdapter.setIsWhite(true)
            airQAdapter.notifyDataSetChanged()
            uvResponseAdapter.notifyDataSetChanged()
            uvLegendAdapter.notifyDataSetChanged()
            weeklyWeatherAdapter.notifyDataSetChanged()
            dailyWeatherAdapter.notifyDataSetChanged()
            warningViewPagerAdapter.changeTextColor(Color.WHITE)
            reportViewPagerItem.addAll(reportArrayList)
            warningViewPagerAdapter.notifyDataSetChanged()
        }

        // 글자색 black 으로 변경
        @Suppress("DEPRECATION")
        fun black() {
            binding.subAirWind.getValue().compoundDrawableTintList =
                ColorStateList.valueOf(getColor(R.color.main_black))

            changeBoxViews.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#40FFFFFF"))
            }
            changeColorTextViews.forEach {
                it.setTextColor(getColor(R.color.main_black))
            }

            changeColorSubTextViews.forEach {
                it.setTextColor(getColor(R.color.sub_black))
            }

            changeTintLineViews.forEach {
                it.setBackgroundColor(getColor(R.color.sub_black))
            }

            changeTintImageViews.forEach {
                it.imageTintList = ColorStateList.valueOf(getColor(R.color.main_black))
            }

            binding.dailySectionTomorrow.setTextColor(getColor(R.color.main_black))
            binding.dailySectionAfterTomorrow.setTextColor(getColor(R.color.main_black))

            binding.mainTopBarGpsTitle.compoundDrawablesRelative[0].mutate()
                .setTint(ResourcesCompat.getColor(resources, R.color.black, null))
            window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            dailyWeatherAdapter.setIsWhite(false)
            weeklyWeatherAdapter.setIsWhite(false)
            uvLegendAdapter.setIsWhite(false)
            uvResponseAdapter.setIsWhite(false)
            airQAdapter.setIsWhite(false)
            airQAdapter.notifyDataSetChanged()
            uvResponseAdapter.notifyDataSetChanged()
            uvLegendAdapter.notifyDataSetChanged()
            weeklyWeatherAdapter.notifyDataSetChanged()
            dailyWeatherAdapter.notifyDataSetChanged()
            warningViewPagerAdapter.changeTextColor(Color.BLACK)
            reportViewPagerItem.addAll(reportArrayList)
            warningViewPagerAdapter.notifyDataSetChanged()
        }

        // 일몰 후인지 아닌지 구분 후 적용
        if (!isNight) {
            when (sky) {
                "맑음", "구름많음", "구름많고 눈", "눈", "흐리고 눈" -> { black() }
                else -> { white() }
            }
        } else { white() }
    }

    // 기상특보 자동 슬라이드 적용
    private fun warningSlideAuto() {
        val vp = binding.mainWarningVp
        val handler = Handler(Looper.getMainLooper())
        if (reportArrayList.size > 1) {
            vp.currentItem = if (vp.currentItem + 1 < reportArrayList.size) vp.currentItem + 1 else 0
            handler.postDelayed({
                warningSlideAuto()
            },3500)
        }
    }

    // 현재 위치가 한국인지 아닌지 구분
    private fun isKorea(lat: Double, lng: Double): Boolean {
        RDBLogcat.writeGpsHistory(this, false, "서비스 지역 밖", "${lat},${lng}")
        return lng in 125.0..132.0 && lat in 33.0..39.0
    }

    // 현재 위치 불러오기
    @SuppressLint("MissingPermission")
    private fun getCurrentLocationData() {
        val locationClass = GetLocation(this)
        if (locationClass.isNetWorkConnected()) {
            if (locationClass.isGPSConnected()) {
                CoroutineScope(SupervisorJob().job + Dispatchers.IO).launch {
                    try {
                        LocationServices.getFusedLocationProviderClient(this@MainActivity).run {
                            this.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener { location ->
                                    location?.let { loc ->
                                        hidePB()
                                        if (isKorea(loc.latitude, loc.longitude)) {
                                            val addr = GetLocation(this@MainActivity)
                                                .getAddress(loc.latitude, loc.longitude)
                                            addr?.let {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    setUserLastAddr(this@MainActivity, it)
                                                    setCurrentLocation(this@MainActivity, it)
                                                    updateCurrentAddress(
                                                        loc.latitude, loc.longitude,
                                                        it
                                                    )
                                                }

                                                it.replaceFirst(" ", "")
                                                it.replace(getString(R.string.korea), "")
                                                it.replace("null", "")

                                                val regexAddr =
                                                    if (getUserLocation(this@MainActivity) == LANG_EN)
                                                        it.replace("South Korea", "")
                                                    else AddressFromRegex(it).getAddress()

                                                val formedAddr =
                                                    if (regexAddr != IN_COMPLETE_ADDRESS) {
                                                        regexAddr
                                                    } else {
                                                        it
                                                    }

                                                binding.mainGpsTitleTv.text = formedAddr

                                                binding.mainTopBarGpsTitle.text =
                                                    formedAddr.trim().split(" ").last()

                                                loadCurrentViewModelData(
                                                    loc.latitude,
                                                    loc.longitude
                                                )
                                            }
                                        } else {
                                            hideAllViews(
                                                ERROR_NOT_SERVICED_LOCATION
                                            )
                                        }
                                    }
                                }
                        }.addOnFailureListener {
                            RDBLogcat.writeErrorNotANR(
                                this@MainActivity, sort = ERROR_LOCATION_FAILED,
                                msg = it.localizedMessage!!
                            )
                        }
                    } catch (e: NullPointerException) {
                        hideAllViews(ERROR_GET_LOCATION_FAILED)
                    }
                }
            } else if (locationClass.isNetworkProviderConnected()) {
                CoroutineScope(Dispatchers.Default).launch {
                    val lm =
                        getSystemService(LOCATION_SERVICE) as LocationManager
                    val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    location?.let { loc ->
                        loadCurrentViewModelData(loc.latitude, loc.longitude)
                    }
                }
            } else {
                hideAllViews(ERROR_GPS_CONNECTED)
            }
        } else {
            MakeSingleDialog(this).makeDialog(
                getString(R.string.error_network_connect),
                getColor(R.color.theme_alert_double_apply_color),
                getString(R.string.ok),
                false
            )
        }
    }

    // 뷰 백그라운드 적용
    private fun <T> applyBackground(view: T, res: Int?) {
        res?.let {
            (view as View).background = ResourcesCompat.getDrawable(resources, it, null)
        } ?: apply {
            (view as View).background = null
        }
    }

    // 백그라운드 위치 호출
    private fun createWorkManager() {
        val loc = GetLocation(this)
        if (loc.isNetWorkConnected()) {
            loc.getGpsInBackground(0, 500f)
        }
    }
}