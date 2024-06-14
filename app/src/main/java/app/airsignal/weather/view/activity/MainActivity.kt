package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.animation.*
import android.widget.*
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.HandlerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import app.airsignal.weather.adapter.*
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.databinding.ActivityMainBinding
import app.airsignal.weather.db.room.repository.GpsRepository
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.GetSystemInfo
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.location.AddressFromRegex
import app.airsignal.weather.location.GetLocation
import app.airsignal.weather.login.SilentLoginClass
import app.airsignal.weather.api.ErrorCode
import app.airsignal.weather.api.NetworkUtils
import app.airsignal.weather.api.retrofit.ApiModel
import app.airsignal.weather.utils.controller.OnSingleClickListener
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.utils.*
import app.airsignal.weather.utils.DataTypeParser
import app.airsignal.weather.utils.TypeFaceObject
import app.airsignal.weather.utils.plain.*
import app.airsignal.weather.utils.view.EnterPageUtil
import app.airsignal.weather.utils.view.RefreshUtils
import app.airsignal.weather.utils.view.SunProgress
import app.airsignal.weather.view.*
import app.airsignal.weather.view.custom.ExternalAirView
import app.airsignal.weather.view.custom.MakeDoubleDialog
import app.airsignal.weather.view.dialog.*
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import app.airsignal.weather.viewmodel.GetWeatherViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@SuppressLint("InflateParams", "MissingPermission","SetTextI18n")
class MainActivity
    : BaseActivity<ActivityMainBinding>() {
    override val resID: Int get() = R.layout.activity_main

    companion object {
        const val SHOWING_LOADING_FLOAT = 1f
        const val NOT_SHOWING_LOADING_FLOAT = 0f
    }

    private val fcm: SubFCM by inject()

    private var isNight = false
    private var isBackPressed = false
    private var isProgressed = false
    private val sideMenuBuilder by lazy { SideMenuBuilder(this) }
    private val sideMenuView: View by lazy {
        LayoutInflater.from(this@MainActivity).inflate(R.layout.side_menu, null)
    }
    private val vib by lazy { VibrateUtil(this) }
    private val getDataViewModel by viewModel<GetWeatherViewModel>()
    private val locationClass: GetLocation by inject()
    private val dailyWeatherList = ArrayList<AdapterModel.DailyWeatherItem>()
    private val weeklyWeatherList = ArrayList<AdapterModel.WeeklyWeatherItem>()
    private val uvResponseList = ArrayList<AdapterModel.UVResponseItem>()
    private val dailyWeatherAdapter by lazy { DailyWeatherAdapter(this, dailyWeatherList) }
    private val weeklyWeatherAdapter by lazy { WeeklyWeatherAdapter(this, weeklyWeatherList) }
    private val reportViewPagerItem = ArrayList<String>()
    private val warningViewPagerAdapter by lazy { WarningViewPagerAdapter(this, reportViewPagerItem, binding.mainWarningVp) }
    private val inAppList = ArrayList<ApiModel.InAppMsgItem>()
    private val warningList = ArrayList<String>()
    private val uvResponseAdapter = UVResponseAdapter(this, uvResponseList)
    private var currentSun = 0
    private var isWarned = false
    private var isDataResponse = false
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

    private val inAppAdapter by lazy { InAppViewPagerAdapter(this@MainActivity, inAppList) }

    private var isInAppMsgShow = false

    private val fetch by lazy { getDataViewModel.getDataResultData }

    private val ioThread by lazy { CoroutineScope(Dispatchers.IO) }
    private val ioDispatcher by lazy { Dispatchers.IO }
    private val mainDispatcher by lazy { Dispatchers.Main }
    private val backgroundDispatcher by lazy { Dispatchers.Default }

    override fun onResume() {
        super.onResume()
        addSideMenu()
//        binding.nestedAdView.resume()
        applyRefreshScroll()
        getDataSingleTime(isCurrent = false)

        if (!fetch.hasActiveObservers()) applyGetDataViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        isWarned = false
        destroyObserver()
//        binding.nestedAdView.destroy()
    }

    private fun destroyObserver() {
        getDataViewModel.cancelJob()
        fetch.removeObservers(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kotlin.runCatching {
            initBinding()
            if (savedInstanceState == null) {
                showProgressBar()
                fcm.subTopic(StaticDataObject.FcmSort.FCM_PATCH.key)
                fcm.subTopic(StaticDataObject.FcmSort.FCM_DAILY.key)
                changeBackgroundResource(null)
                window.statusBarColor = getColor(R.color.theme_view_color)
                window.navigationBarColor = getColor(R.color.theme_view_color)
                binding.mainMotionLayout.apply {
                    isInteractionEnabled = false // 모션 레이아웃의 스와이프를 막음
                    isEnabled = false
                    setTransition(R.id.start, R.id.end)
                }
                applyGetDataViewModel()
            }

//            adViewClass.loadAdView(binding.nestedAdView)  // adView 생성

            initializing()

            sunPb.disableTouch()    // 일출/일몰 그래프 클릭 방지

            // 메인 하단 스크롤 유도 화살표 애니메이션 적용
            val bottomArrowAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_arrow_anim)
            binding.mainMotionSLideImg.startAnimation(bottomArrowAnim)

            // 스크롤 최상단으로 올리기 버튼
            binding.nestedFab.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    binding.nestedScrollview.smoothScrollTo(0, 0, 500)
                }
            })

            binding.mainTopBarGpsTitle.requestFocus()
            binding.mainTopBarGpsTitle.setOnFocusChangeListener { v, hasFocus ->
                v.isSelected = hasFocus
            }

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
                    v?.startAnimation(rotateAnim)
                    getDataSingleTime(isCurrent = true)
                }
            })

            // 사이드 메뉴 세팅
            binding.mainSideMenuIv.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    sideMenuBuilder.show(sideMenuView, true)
                }
            })

            binding.nestedScrollview.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                binding.nestedFab.alpha = if (scrollY == 0) 0f else 1f
            }

            // 공유하기 버튼 클릭
            binding.mainShareIv.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mVib()
                    val doubleDialog = MakeDoubleDialog(this@MainActivity)
                    if (GetAppInfo.getUserLocation(this@MainActivity) == StaticDataObject.LANG_EN) {
                        doubleDialog.make(
                            "Share with in English?",
                            "Yes",
                            "Change to Korean",
                            R.color.main_blue_color
                        ).apply {
                            this.first.setOnClickListener(object : OnSingleClickListener() {
                                override fun onSingleClick(v: View?) {
                                    doubleDialog.dismiss()
                                    addShareMsg(StaticDataObject.LANG_EN)
                                }
                            })
                            this.second.setOnClickListener(object : OnSingleClickListener() {
                                override fun onSingleClick(v: View?) {
                                    doubleDialog.dismiss()
                                    addShareMsg(StaticDataObject.LANG_KR)
                                }
                            })
                        }
                    } else {
                        doubleDialog.dismiss()
                        addShareMsg(StaticDataObject.LANG_KR)
                    }
                }
            })

            binding.mainSwipeLayout.setColorSchemeColors(
                Color.parseColor("#22D3EE"),
                Color.parseColor("#4DCF7D"),
                Color.parseColor("#FACC15"),
                Color.parseColor("#F87171")
            )

            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                // 스와이프 리프래시 레이아웃 리스너
                binding.mainSwipeLayout.setOnRefreshListener {
                    Handler(Looper.getMainLooper()).postDelayed({
                        getDataSingleTime(false)
                    }, 500)
                }
            }
        }.onFailure { exception ->
            if (exception == InstantiationException())
                RefreshUtils(this).refreshApplication() }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun startInAppMsg() {
        kotlin.runCatching {
            inAppList.clear()

            val inAppExtraSize = intent.extras?.getInt(SpDao.IN_APP_MSG_COUNT)
            if (inAppExtraSize != null) {
                repeat(inAppExtraSize) { index ->
                    val redirect = intent.extras?.getString("${SpDao.IN_APP_MSG_REDIRECT}${index}")
                    val img = intent.extras?.getString("${SpDao.IN_APP_MSG}${index}")
                    img?.let { pImg ->
                        redirect?.let { pRedirect ->
                            inAppList.add(ApiModel.InAppMsgItem(pImg,pRedirect))
                            inAppAdapter.notifyItemChanged(index)
                        }
                    }
                }

                val oneHour = (1000 * 60 * 60).toLong()
                val sevenDays = (1000 * 60 * 60 * 24 * 7).toLong()

                if (inAppExtraSize == 0) return

                val hour = if (GetAppInfo.getInAppMsgEnabled(this@MainActivity)) sevenDays else oneHour

                if (!isTimeToDialog(hour)) return

                runOnUiThread { inAppMsgDialog() }
            }
        }.exceptionOrNull()?.stackTraceToString()
    }

    private suspend fun isTimeToDialog(long: Long): Boolean = withContext(ioDispatcher) {
        return@withContext LocalDateTime.now()
            .isAfter(
                DataTypeParser.parseLongToLocalDateTime(
                    GetAppInfo.getInAppMsgTime(this@MainActivity) + (long)
                )
            )
    }

    private fun inAppMsgDialog() {
        val inAppDialog = AlertDialog.Builder(this, R.style.InAppDialogStyle)
        val inAppView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_in_app_msg, null, false)
        inAppDialog.setView(inAppView)
        val inAppAlert = inAppDialog.create()
        val inAppVp = inAppView.findViewById<ViewPager2>(R.id.inAppMsgVp)
        val inAppIndicator = inAppView.findViewById<LinearLayout>(R.id.inAppMsgIndicator)
        val inAppCancel = inAppView.findViewById<TextView>(R.id.inAppMsgCancel)
        val inAppHide = inAppView.findViewById<TextView>(R.id.inAppMsgHide)

        inAppVp.apply {
            adapter = inAppAdapter
            isClickable = true
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
        }

        inAppCancel.setOnClickListener {
            ioThread.launch {
                SetAppInfo.setInAppMsgDenied(this@MainActivity, false)
                withContext(mainDispatcher) {
                    inAppAlert.dismiss()
                }
            }
        }

        inAppHide.setOnClickListener {
            ioThread.launch {
                SetAppInfo.setInAppMsgDenied(this@MainActivity, true)
                withContext(mainDispatcher) {
                    inAppAlert.dismiss()
                }
            }
        }

        if (inAppList.isNotEmpty() && inAppList.size > 1) {
            inAppIndicator.removeAllViews()
            IndicatorView(this, inAppList.size).createIndicators(
                inAppIndicator,
                inAppVp,
                ColorStateList.valueOf(getColor(R.color.white))
            )
        }

        inAppAlert.show()
    }

    // 공유하기 언어별 대응
    private fun addShareMsg(locale: String) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "text/plain"
        if (locale == StaticDataObject.LANG_EN) {
            intent.putExtra(
                Intent.EXTRA_TEXT, "${
                    "The weather ${binding.mainTopBarGpsTitle.text} is ${binding.mainLiveTempValue.text}˚ " +
                            "${DataTypeParser.translateSkyText(binding.mainSkyText.text.toString())}. The chance of rain is ${binding.subAirRainP.getValue().text}," +
                            " and the humidity is ${binding.subAirHumid.getValue().text}"
                }\n\n${"Click the link for real-time weather information on Airsignal\n${IgnoredKeyFile.playStoreURL}"}"
            )
            startActivity(Intent.createChooser(intent, "Share weather data"))
        } else {
            intent.putExtra(
                Intent.EXTRA_TEXT, "${
                    "현재 ${binding.mainTopBarGpsTitle.text}의 날씨는 ${binding.mainLiveTempValue.text}˚로 ${binding.mainSkyText.text}입니다. " +
                            "강수확률은 ${binding.subAirRainP.getValue().text}이고 습도는 ${binding.subAirHumid.getValue().text}입니다."
                }\n\n${"에어시그널의 실시간 날씨 정보를 알고싶다면 아래 링크를 클릭하세요.\n${IgnoredKeyFile.playStoreURL}"}"
            )
            startActivity(Intent.createChooser(intent, "날씨 데이터 공유하기"))
        }
    }

    private fun applyRefreshScroll() {
        binding.mainMotionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {}
            override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionTrigger(motionLayout: MotionLayout, triggerId: Int, positive: Boolean, progress: Float) {}
            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                binding.mainSwipeLayout.isEnabled = motionLayout.currentState == R.id.start
            }
        })
    }

    // 햄버거 메뉴 세팅
    private fun addSideMenu() {
        kotlin.runCatching {
            val cancel = sideMenuView.findViewById<ImageView>(R.id.headerCancel)
            val profile = sideMenuView.findViewById<ImageView>(R.id.navHeaderProfileImg)
            val id = sideMenuView.findViewById<TextView>(R.id.navHeaderUserId)
            val weather = sideMenuView.findViewById<TextView>(R.id.navMenuWeather)
            val setting = sideMenuView.findViewById<TextView>(R.id.navMenuSetting)
            val warning = sideMenuView.findViewById<TextView>(R.id.navMenuWarning)
            val headerTr = sideMenuView.findViewById<TableRow>(R.id.headerTr)

            sideMenuBuilder.apply {
                setBackPressed(cancel)
                setUserData(profile, id)
            }

            if (GetAppInfo.getUserLocation(this) == StaticDataObject.LANG_EN || GetSystemInfo.getLocale(this) == Locale.ENGLISH)
                warning.visibility = GONE
            else {
                warning.visibility = VISIBLE
                warning.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        closeMenuAndCallback {
                            val intent = Intent(this@MainActivity, WarningDetailActivity::class.java)
                            startActivity(intent)
                        }
                    }
                })
            }

            headerTr.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if (GetAppInfo.getUserLoginPlatform(this@MainActivity) == "") {
                        closeMenuAndCallback {
                            val enter = EnterPageUtil(this@MainActivity)
                            enter.toLogin(EnterPageUtil.ENTER_FROM_MAIN)
                        }
                    }
                }
            })

            weather.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    sideMenuBuilder.dismiss()
                }
            })

            setting.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    closeMenuAndCallback {
                        val intent = Intent(this@MainActivity, SettingActivity::class.java)
                        startActivity(intent)
                    }
                }
            })
        }.onFailure { exception ->  if (exception == NullPointerException()) exception.printStackTrace()}
    }

    private fun closeMenuAndCallback(callback: () -> Unit) {
        CompletableFuture
            .supplyAsync { sideMenuBuilder.dismiss() }
            .thenAccept { callback.invoke() }
    }

    // 진동 발생
    private fun mVib() = vib.make(20)

    // 시간별 날씨 스크롤 첫번째 인덱스로 이동
    private fun scrollSmoothFirst(position: Int) {
        val layoutManager = binding.mainDailyWeatherRv.layoutManager
        layoutManager?.let {
            val smoothScroller = object : LinearSmoothScroller(this) {
                // 가장 첫 번째로 스크롤되도록 설정
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }
            binding.mainDailyWeatherRv.setPadding(22, 0, 15, 0)
            smoothScroller.targetPosition = position + 5
            it.startSmoothScroll(smoothScroller)
        }
    }

    // 시간별 날씨 색션 컬러 변경
    private fun setSectionTextColor(t1: TextView, t2: TextView, t3: TextView) {
        val isWhite = dailyWeatherAdapter.getIsWhite()
        t1.typeface = if (isWhite) TypeFaceObject.getBold(this) else TypeFaceObject.getMedium(this)
        t2.typeface = TypeFaceObject.getRegular(this)
        t3.typeface = TypeFaceObject.getRegular(this)
        t1.setTextColor(getColor(if (isWhite) R.color.white else R.color.main_blue_color))
        t2.setTextColor(getColor(if (isWhite) R.color.sub_white else R.color.sub_black))
        t3.setTextColor(getColor(if (isWhite) R.color.sub_white else R.color.sub_black))
    }

    // 날씨 데이터 API 호출
    private val permissionsUtil = RequestPermissionsUtil(this)

    private fun getDataSingleTime(isCurrent: Boolean) {
        if (isNetworkAndLocationPermitted()) {
            val lastAddress = GetAppInfo.getUserLastAddress(this)
            val addrArray = resources.getStringArray(R.array.address_korean)
            if (!isCurrent && addrArray.contains(lastAddress)) {
                addrArray.forEachIndexed { index, address ->
                    if (lastAddress == address)
                        loadSavedAddr(addrArray[index], resources.getStringArray(R.array.address_english)[index])
                }
            } else checkLocationAvailability()

            // TimeOut
            HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                hideProgressBar()
            }, 1000 * 8)
        }
    }

    private fun isNetworkAndLocationPermitted(): Boolean =
        permissionsUtil.isNetworkPermitted() && permissionsUtil.isLocationPermitted()

    // 저장된 주소로 데이터 호출
    private fun loadSavedAddr(addr: String?, enAddr: String?) {
        addr?.let { mAddr ->
            loadSavedViewModelData(mAddr)
            val gpsValue = if (GetAppInfo.getUserLocation(this) == StaticDataObject.LANG_EN) enAddr?.trim() else mAddr.trim()
            updateAddress(gpsValue)
        }
    }

    private fun setProgressVisibility(show: Boolean) {
        if (show && !isProgressed) {
            isProgressed = true
            binding.mainLoadingView.visibility = VISIBLE
            binding.mainLoadingView.alpha = SHOWING_LOADING_FLOAT
            binding.mainLoadingView.bringToFront()
            binding.mainMotionLayout.isInteractionEnabled = false
            binding.mainMotionLayout.isEnabled = false
        } else if (!show && binding.mainLoadingView.alpha == SHOWING_LOADING_FLOAT) {
            binding.mainLoadingView.visibility = GONE
            binding.mainLoadingView.alpha = NOT_SHOWING_LOADING_FLOAT
            binding.mainMotionLayout.isInteractionEnabled = true
            binding.mainMotionLayout.isEnabled = true
            binding.mainGpsFix.clearAnimation()
        }
    }

    // 프로그래스 보이기
    private fun showProgressBar() = setProgressVisibility(true)

    // 프로그래스 숨기기
    private fun hideProgressBar() = setProgressVisibility(false)

    private fun initializing() {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(displayMetrics)

        // 자동 로그인
        SilentLoginClass().login(this@MainActivity)

        //AdMob 초기화
//        AdViewClass(this).loadAdView(sideMenuView.findViewById(R.id.navMenuAdview))

        // 어댑터 바인딩
        binding.mainDailyWeatherRv.adapter = dailyWeatherAdapter
        binding.mainWeeklyWeatherRv.adapter = weeklyWeatherAdapter
        binding.mainUvCollapseRv.adapter = uvResponseAdapter

        // 기상특보 뷰페이저 세팅
        binding.mainWarningVp.apply {
            adapter = warningViewPagerAdapter
            isClickable = true
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
        }

        binding.mainUvCollapseRv.isClickable = false
        binding.nestedSubAirFrame.isClickable = false

        binding.mainSunRiseTitle.text = getString(R.string.sunrise)
        binding.mainUvTitle.text = getString(R.string.uv_title)
        binding.mainWeeklyWeatherTitle.text = getString(R.string.weekly_weather)
        binding.dailySectionAfterTomorrow.text = getString(R.string.daily_next_tomorrow)
        binding.dailySectionToday.text = getString(R.string.daily_today)
        binding.dailySectionTomorrow.text = getString(R.string.daily_tomorrow)
        binding.mainDailyWeatherTitle.text = getString(R.string.daily_weather)
        binding.mainSunTitle.text = getString(R.string.sun_rise_set)
        binding.mainMinMaxTitleC.text = getString(R.string.min_max)
        binding.mainSensTitleC.text = getString(R.string.sens_temp)
        binding.mainLiveTempTitleC.text = getString(R.string.live_temp)
        binding.mainLicenseText.text = getString(R.string.data_api_license)
        binding.mainSunTomTitle.text = getString(R.string.tomorrow)
        binding.mainSunSetTitle.text = getString(R.string.sunset)

//        // adView 닫기 클릭
//        binding.adViewCancelIv.setOnClickListener {
//            it.visibility = GONE
//            val layoutParams =
//                RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
//            layoutParams.addRule(RelativeLayout.BELOW, R.id.nested_daily_box)
//            layoutParams.setMargins(0)
//            binding.adViewBox.layoutParams = layoutParams
//            binding.nestedAdView.visibility = GONE
//        }

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
            if (dailyWeatherList.size >= DataTypeParser.getHourCountToTomorrow()) {
                tomorrowSection.visibility = VISIBLE
                binding.mainDailyWeatherRv.scrollToPosition(DataTypeParser.getHourCountToTomorrow())
                binding.mainDailyWeatherRv.post { scrollSmoothFirst(DataTypeParser.getHourCountToTomorrow()) }
            } else tomorrowSection.visibility = GONE
        }

        // 모레 클릭
        afterTomorrowSection.setOnClickListener {
            if (dailyWeatherList.size >= DataTypeParser.getHourCountToTomorrow() + 24) {
                afterTomorrowSection.visibility = VISIBLE
                binding.mainDailyWeatherRv.post { scrollSmoothFirst(DataTypeParser.getHourCountToTomorrow() + 24) }
            } else afterTomorrowSection.visibility = GONE
        }

        // 시간별 날씨 스크롤에 따른 탭 변화
        binding.mainDailyWeatherRv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val sectionList = dailyWeatherAdapter.getDateSectionList()
                // 현재 스크롤 위치 확인
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                when (sectionList.size) {
                    1 -> setSectionTextColor(todaySection, tomorrowSection, afterTomorrowSection)
                    2 -> when (layoutManager.findFirstVisibleItemPosition()) {
                            sectionList[0] -> setSectionTextColor(todaySection, tomorrowSection, afterTomorrowSection)
                            sectionList[1] -> setSectionTextColor(tomorrowSection, todaySection, afterTomorrowSection)
                        }
                    3 -> when (layoutManager.findFirstVisibleItemPosition()) {
                            sectionList[0] -> setSectionTextColor(todaySection, tomorrowSection, afterTomorrowSection)
                            sectionList[1] -> setSectionTextColor(tomorrowSection, todaySection, afterTomorrowSection)
                            sectionList[2] -> setSectionTextColor(afterTomorrowSection, todaySection, tomorrowSection)
                        }
                    else -> {}
                }
            }
        })
    }

    @SuppressLint("MissingSuperCall")
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
            SetAppInfo.removeSingleKey(this, IgnoredKeyFile.lastAddress)
            EnterPageUtil(this).fullyExit()
        }
        // 2초간 스레드 유지
        Handler(Looper.getMainLooper()).postDelayed({
            isBackPressed = false
        }, 2000)
    }

    // 토픽을 갱신하는 작업
    private fun reNewTopicInMain(newAddr: String) {
        val old = GetAppInfo.getTopicNotification(this)
        fcm.renewTopic(old, newAddr)
        SetAppInfo.setTopicNotification(this, newAddr)
    }

    private fun applyGetDataViewModel() {
        kotlin.runCatching {
            fetch.observe(this) { entireData ->
                entireData?.let { eData ->
                    binding.mainSwipeLayout.isRefreshing = false

                    when (eData) {
                        is BaseRepository.ApiState.Success -> handleApiSuccess(eData.data)
                        is BaseRepository.ApiState.Error -> handleApiError(eData.errorMessage)
                        is BaseRepository.ApiState.Loading -> showProgressBar()
                    }
                } ?: run {
                    hideProgressBar()
                    if (!isDataResponse) hideAllViews(error = ErrorCode.ERROR_NULL_DATA)
                }
            }
        }.onFailure { exception ->
            if (exception == IOException()) {
                binding.mainSwipeLayout.isRefreshing = false
                handleApiError(ErrorCode.ERROR_API_PROTOCOL)
                hideProgressBar()
            }
        }
    }

    // API 통신이 성공일 때 처리
    private fun handleApiSuccess(result: ApiModel.GetEntireData) {
        kotlin.runCatching {
            val metaAddr = result.meta.address
            ioThread.launch {
                metaAddr?.let { reNewTopicInMain(it) }

//                isNight = true
                isNight = GetAppInfo.getIsNight(result.sun?.sunrise ?: "0000",result.sun?.sunset ?: "0000")

                isDataResponse = true
                withContext(mainDispatcher) {
                    binding.mainGpsFix.clearAnimation()
                    binding.mainDailyWeatherRv.scrollToPosition(0)
                    binding.mainWarningVp.currentItem = 0
                    showAllViews()
                    updateUIWithData(result)

                    // 메인 날씨 텍스트 세팅
                    val isCurrent = currentIsAfterRealtime(
                        result.current.currentTime, result.realtime[0].forecast)
                    val skyText = DataTypeParser.translateSky(
                            this@MainActivity, DataTypeParser.applySkyText(this@MainActivity,
                                if (isCurrent) result.current.rainType else result.realtime[0].rainType,
                                if (isCurrent) result.realtime[0].sky else result.realtime[0].sky,
                                result.thunder)
                        )

                    val rainTypeText =
                        if (isCurrent) result.current.rainType
                        else result.realtime[0].rainType

//                 날씨에 따라 배경화면 변경
                    val testSky = getString(R.string.sky_sunny)
                    val testRain = getString(R.string.sky_rain_nothing)

//                    applyWindowBackground(sky = testSky, rainType = testRain)
//                    setMountain(sky = testSky, rainType = testRain)
//                    setSkyLottie(sky = testSky)
//                    setRainTypeLottie(testRain)

                    applyWindowBackground(sky = result.realtime[0].sky, rainType = rainTypeText)
                    setMountain(sky = result.realtime[0].sky, rainType = rainTypeText)
                    setSkyLottie(sky = result.realtime[0].sky)
                    setRainTypeLottie(rainType = rainTypeText)

                    binding.mainSkyText.text = skyText

                    hideProgressBar()

                    if (!isInAppMsgShow) {
                        isInAppMsgShow = true
                        ioThread.launch { startInAppMsg() }
                    }

                    binding.mainLunarBox.visibility = if (isNight) VISIBLE else GONE
                    binding.mainLunarBox.alpha = if (isNight) 1f else 0f
                }
            }
        }.onFailure { exception ->
            handleApiError(exception.localizedMessage ?: exception.stackTraceToString())
        }
    }

    private fun currentIsAfterRealtime(currentTime: String, realTime: String?): Boolean {
        val timeFormed = LocalDateTime.parse(currentTime)
        val realtimeFormed = LocalDateTime.parse(realTime)
        return realtimeFormed?.let { timeFormed.isAfter(it) } ?: true
    }

    // API 통신이 에러일 때 처리
    private fun handleApiError(errorMessage: String) {
        runOnUiThread {
            hideProgressBar()
            if (locationClass.isNetWorkConnected()) hideAllViews(error = errorMessage)
            if (errorMessage != "text") hideAllViews(error = ErrorCode.ERROR_NETWORK)
        }
    }

    // 결과에서 얻은 데이터로 UI 요소를 업데이트
    private fun updateUIWithData(result: ApiModel.GetEntireData) {
        currentSun = GetAppInfo.getCurrentSun(result.sun?.sunrise ?: "0600",result.sun?.sunset ?: "1900")
        val lunar = result.lunar?.date ?: -1
        val realtimeFirst = result.realtime[0]

        updateWeatherItems(result)
        updateAirQualityData(result.quality)
        updateUVData(result.uv)
        updateSunTimes(result.sun, result.sun_tomorrow)
        updateCurrentTemperature(result.yesterday, result.current, result.realtime)
        updateWeatherWarnings(result.summary)
        updateTerm24(result.term24)

        val wave = result.realtime[0].wave
        TimberUtil.d("testtest","wave is $wave")
        if (wave != null && wave != 0.0) {
            binding.subAirWave.fetchData(
                "${DataTypeParser.parseDoubleToDecimal(wave,1)}M",
                R.drawable.ico_main_wave,null)
            binding.subAirWave.visibility = View.VISIBLE
        } else {
            binding.subAirWave.visibility = View.GONE
        }

        // 메인 날씨 아이콘 세팅
        binding.mainSkyImg.setImageDrawable(
            DataTypeParser.applySkyImg(
            this,
            if (currentIsAfterRealtime(result.current.currentTime, realtimeFirst.forecast))
                result.current.rainType else realtimeFirst.rainType,
            realtimeFirst.sky, result.thunder,
            isLarge = true, isNight,  lunar = lunar))

        val lunarClass = LunarShape(result.lunarAge)
        binding.mainLunarImg.setImageDrawable(lunarClass.shapeDrawable(this))
        binding.mainLunarProgress.text = "${lunarClass.progress()}%"

        binding.mainLunarShapeText.text = lunarClass.shapeText(this)
    }

    private fun updateWeatherItems(result: ApiModel.GetEntireData) {
        // 일일 및 주간 날씨 항목 업데이트
        runOnUiThread {
            dailyWeatherList.clear()
            weeklyWeatherList.clear()
        }

        val week = result.week
        // 주간 오전 날씨
        val wfMin = listOf(
            week.wf0Am, week.wf1Am, week.wf2Am, week.wf3Am,
            week.wf4Am, week.wf5Am, week.wf6Am, week.wf7Am
        )
        // 주간 오후 날씨
        val wfMax = listOf(
            week.wf0Pm, week.wf1Pm, week.wf2Pm, week.wf3Pm,
            week.wf4Pm, week.wf5Pm, week.wf6Pm, week.wf7Pm,
        )
        // 주간 최저 기온
        val taMin = listOf(
            result.today?.min, week.taMin1, week.taMin2, week.taMin3,
            week.taMin4, week.taMin5, week.taMin6, week.taMin7
        )
        // 주간 최고 기온
        val taMax = listOf(
            result.today?.max, week.taMax1, week.taMax2, week.taMax3, week.taMax4,
            week.taMax5, week.taMax6, week.taMax7
        )
        // 오전 강수확률
        val amRain = listOf(
            week.rnSt0Am, week.rnSt1Am, week.rnSt2Am, week.rnSt3Am,
            week.rnSt4Am, week.rnSt5Am, week.rnSt6Am
        )
        val pmRain = listOf(
            week.rnSt0Pm, week.rnSt1Pm, week.rnSt2Pm, week.rnSt3Pm,
            week.rnSt4Pm, week.rnSt5Pm, week.rnSt6Pm
        )

        // 최저/최대 기온 적용
        result.today?.let { mToday ->
            binding.mainMinValue.text = "${filteringNullData(mToday.min)}˚"
            binding.mainMaxValue.text = "${filteringNullData(mToday.max)}˚"
            binding.mainMinMaxUnit.text = "/"
            binding.mainMinMaxValueC.text = "${filteringNullData(mToday.min)}˚/${filteringNullData(mToday.max)}˚"
        }

        // 시간별 날씨 아이템 추가
        val current = result.current
        val thunder = result.thunder
        val lunar = result.lunar?.date ?: -1

        result.realtime.forEachIndexed { realtimeIndex, dailyIndex ->
            val forecastToday = LocalDateTime.parse(dailyIndex.forecast)

            if (realtimeIndex == 0) {
                val isAfterRealtime =
                    currentIsAfterRealtime(current.currentTime, dailyIndex.forecast)
                val skyImg = DataTypeParser.applySkyImg(
                    this, if (isAfterRealtime) current.rainType else dailyIndex.rainType,
                    dailyIndex.sky, thunder,
                    isLarge = false, isNight, lunar = lunar
                )
                val temperature =
                    if (isAfterRealtime) "${current.temperature.roundToInt()}˚" else "${dailyIndex.temp.roundToInt()}˚"
                val rainType = if (isAfterRealtime) current.rainType else dailyIndex.rainType
                val rainP =  dailyIndex.rainP ?: 0.0

                addDailyWeatherItem(
                    "${forecastToday.hour}${getString(R.string.hour)}",
                    skyImg,
                    temperature,
                    dailyIndex.forecast ?: "",
                    DataTypeParser.isRainyDay(rainType),
                    rainP
                )
            } else {
                val skyImg = DataTypeParser.applySkyImg(
                    this, dailyIndex.rainType, dailyIndex.sky, thunder,
                    isLarge = false, isNight, lunar = lunar
                ) ?: getR(R.drawable.ic_error)

                addDailyWeatherItem(
                    "${forecastToday.hour}${getString(R.string.hour)}",
                    skyImg,
                    "${dailyIndex.temp.roundToInt()}˚",
                    dailyIndex.forecast ?: "",
                    DataTypeParser.isRainyDay(dailyIndex.rainType),
                    dailyIndex.rainP
                )
            }
        }

        val dateNow: LocalDateTime = LocalDateTime.now()

        // 주간별 날씨 아이템 추가
        repeat(7) {
            kotlin.runCatching {
                val date: String = when (it) {
                    0 -> getString(R.string.today_main)
                    1 -> getString(R.string.tomorrow_week)
                    else ->
                        "${DataTypeParser.parseDayOfWeekToKorean(this, 
                            dateNow.dayOfWeek.value + it)}${getString(R.string.date)}"
                }

                addWeeklyWeatherItem(
                    date,
                    DataTypeParser.dateAppendZero(dateNow.plusDays(it.toLong())),
                    DataTypeParser.getSkyImgSmall(this, wfMin[it], false),
                    DataTypeParser.getSkyImgSmall(this, wfMax[it], true),
                    "${(taMin[it] ?: 0.0).roundToInt()}˚",
                    "${(taMax[it] ?: 0.0).roundToInt()}˚",
                    amRain[it]?.toInt() ?: 0,
                    pmRain[it]?.toInt() ?: 0
                )
            }.exceptionOrNull()?.stackTraceToString()
        }
    }

    private fun updateAirQualityData(air: ApiModel.AirQualityData) {
        val pm25 = (air.pm25Value ?: air.pm25Value24 ?: 0.0)
        val pm10 = (air.pm10Value ?: air.pm10Value24 ?: 0.0)
        val pm25Grade = DataTypeParser.getDataText(this,
            DataTypeParser.convertValueToGrade(ExternalAirView.AirQ.PM2_5.sort, pm25.toDouble()))
        val pm10Grade = DataTypeParser.getDataText(this,
            DataTypeParser.convertValueToGrade(ExternalAirView.AirQ.PM10.sort, pm10))

        binding.mainAirPm10.setOnClickListener(binding.nestedAirHelpPopup)
            .fetchData(ExternalAirView.AirQ.PM10, pm10.toInt())
        binding.mainAirPm2p5.setOnClickListener(binding.nestedAirHelpPopup)
            .fetchData(ExternalAirView.AirQ.PM2_5, pm25.toInt())
        binding.mainAirCo.setOnClickListener(binding.nestedAirHelpPopup)
            .fetchData(ExternalAirView.AirQ.CO,air.coValue ?: 0.0)
        binding.mainAirNo2.setOnClickListener(binding.nestedAirHelpPopup)
            .fetchData(ExternalAirView.AirQ.NO2,air.no2Value ?: 0.0)
        binding.mainAirSo2.setOnClickListener(binding.nestedAirHelpPopup)
            .fetchData(ExternalAirView.AirQ.SO2,air.so2Value ?: 0.0)
        binding.mainAirO3.setOnClickListener(binding.nestedAirHelpPopup)
            .fetchData(ExternalAirView.AirQ.O3,air.o3Value ?: 0.0)

        binding.subAirPM25.text = "${getString(R.string.pm2_5_full)}   $pm25Grade"
        binding.subAirPM10.text = "${getString(R.string.pm10_full)}   $pm10Grade"
        changeStrokeColor(binding.subAirPM25, DataTypeParser.getDataColor(this,
            DataTypeParser.convertValueToGrade(ExternalAirView.AirQ.PM2_5.sort, pm25.toDouble())))
        changeStrokeColor(binding.subAirPM10, DataTypeParser.getDataColor(this,
            DataTypeParser.convertValueToGrade(ExternalAirView.AirQ.PM10.sort, pm10)))
    }

    private fun updateUVData(uv: ApiModel.UV?) {
        // 자외선 데이터 업데이트  UV 값이 없으면 카드 없앰
        if (uv?.flag == null || uv.value == null) {
            binding.mainUVBox.visibility = GONE
            return
        }

        if (binding.mainUVBox.visibility != VISIBLE) binding.mainUVBox.visibility = VISIBLE

        applyUvResponseItem(uv.flag)   // 자외선 단계별 대응요령 추가
        DataTypeParser.applyUvColor(this, uv.flag, binding.mainUvValue) // UV 범주 색상 변경
        binding.mainUvValue.text = DataTypeParser.translateUV(this, uv.flag)
    }

    private fun updateSunTimes(
        sun: ApiModel.SunData?,
        sunTomorrow: ApiModel.SunTomorrow?) {
        // 일출 및 일몰 시간 업데이트
        sunPb.animate(currentSun)

        // 일출/일몰 세팅
        sunTomorrow?.let { tom ->
            binding.mainSunRiseTom.text = convertTimeFormat(tom.sunrise)
            binding.mainSunSetTom.text = convertTimeFormat(tom.sunset)
        }
        binding.mainSunRiseTime.text = convertTimeFormat(sun?.sunrise)
        binding.mainSunSetTime.text = convertTimeFormat(sun?.sunset)
        binding.mainLunarRiseValue.text = convertTimeFormat(sun?.moonrise)
        binding.mainLunarSetValue.text = convertTimeFormat(sun?.moonset)
    }

    private fun convertTimeFormat(time: String?): String =
        time?.let {(it.substring(0, 2) + " : " + it.substring(2)).trim()} ?: ""

    private fun updateCurrentTemperature(
        yesterdayTemp: ApiModel.YesterdayTemp,
        current: ApiModel.Current,
        realtime: List<ApiModel.RealTimeData>) {
        // 현재 온도 적용
        val real0 = realtime[0]
        val currentTemperature = current.temperature.toString()
        val currentHumidity = NetworkUtils.modifyCurrentHumid(current.humidity, real0.humid)
        val currentWindSpeed = NetworkUtils.modifyCurrentWindSpeed(current.windSpeed, real0.windSpeed)

        binding.mainLiveTempValue.text = currentTemperature
        binding.mainLiveTempUnit.text = "˚"
        binding.mainLiveTempValueC.text = "$currentTemperature˚"

        if (real0.wave == null || real0.wave == 0.0) binding.mainSubAirTr.setPadding(150,0,150,0)
        else binding.mainSubAirTr.setPadding(40,0,40,0)

        // 서브 날씨(습도,바람,강수확률) 적용
        binding.subAirHumid.fetchData(
            "${currentHumidity.roundToInt()}%", R.drawable.ico_main_humidity, null
        )
        binding.subAirWind.fetchData(
            "${currentWindSpeed.roundToInt()}m/s",
            R.drawable.ico_main_wind,
            current.vector ?: real0.vector
        )
        binding.subAirRainP.fetchData("${(real0.rainP ?: 0.0).roundToInt()}%", R.drawable.ico_main_rain, null)

        // 온도 비교 업데이트
        getCompareTempText(yesterdayTemp.temp ?: real0.temp, current.temperature, binding.mainCompareTempTv)

        // 체감 온도 업데이트
        binding.mainSensTitle.text = getString(R.string.sens_temp)

        kotlin.runCatching {
            DataTypeParser.parseDoubleToDecimal(
                SensibleTempFormula().getSensibleTemp(
                    ta = current.temperature,
                    rh = currentHumidity,
                    v = currentWindSpeed,
                    SensibleTempFormula().getCurrentSeason()
                ), digit = 1
            )
        }.fold(
            onSuccess = {
                binding.mainSensValue.text = "$it˚"
                binding.mainSensValueC.text = "$it˚"
            },
            onFailure = {
                DataTypeParser.parseDoubleToDecimal(
                    SensibleTempFormula().getSensibleTemp(
                        ta = real0.temp,
                        rh = currentHumidity,
                        v = currentWindSpeed,
                        SensibleTempFormula().getCurrentSeason()
                    ), digit = 1
                )
            }
        )
    }

    // 주소 업데이트 후 적용
    private fun updateAddress(addr: String?) {
        // UI 업데이트: 주소 텍스트뷰에 주소를 설정하고 데이터 로딩을 시작합니다.
        binding.mainGpsTitleTv.text = addr
        binding.mainTopBarGpsTitle.text = addr ?: " ".trim().split(" ").last()
    }

    // 기상 경보 업데이트
    private fun updateWeatherWarnings(summary: List<String>?) {
        runOnUiThread {
            reportViewPagerItem.clear()
            warningList.clear()
        }

        if (GetAppInfo.getUserLocation(this) == StaticDataObject.LANG_EN)
            binding.mainWarningBox.setBackgroundColor(getColor(android.R.color.transparent))
        else {
            // 기상특보 세팅
            summary?.let { sList ->
                val filteredList = sList.map { summary ->
                    summary.replace("○", "").replace("\n", "").trim()
                }

                warningList.addAll(filteredList)

                if (warningList.isNotEmpty() && !isWarned) {
                    warningSlideAuto()
                    isWarned = true
                }
            }
        }
    }

    private fun updateTerm24(terms24: String?) {
        // 24절기 세팅
        terms24?.let { term ->
            Term24Class.getTerms24Bundle(term)?.let { b ->
                binding.nestedTerms24Box.visibility = VISIBLE
                binding.mainTermsTitle.text = b.getString(Term24Class.TERMS_TITLE)
                binding.mainTermsDate.text = b.getString(Term24Class.TERMS_DATE)
                binding.mainTermsExplain.text = b.getString(Term24Class.TERMS_EXPLAIN)
            } ?: run { binding.nestedTerms24Box.visibility = GONE }
        } ?: run { binding.nestedTerms24Box.visibility = GONE }
    }

    // 하늘상태에 따라 윈도우 배경 변경
    private fun applyWindowBackground(sky: String?, rainType: String?) {
        changeBackgroundResource(
            when(rainType) {
                getString(R.string.sky_snowy),
                getString(R.string.sky_sunny_cloudy_snowy),
                getString(R.string.sky_cloudy_snowy) -> R.drawable.main_bg_snow
                else -> when(sky) {
                    getString(R.string.sky_sunny),
                    getString(R.string.sky_sunny_cloudy),
                    getString(R.string.sky_rainy),
                    getString(R.string.sky_shower),
                    getString(R.string.sky_rainy_snowy),
                    getString(R.string.sky_sunny_cloudy_shower),
                    getString(R.string.sky_sunny_cloudy_rainy),
                    getString(R.string.sky_sunny_cloudy_rainy_snowy) -> if (isNight) R.drawable.main_bg_night else R.drawable.main_bg_clear

                    getString(R.string.sky_cloudy),
                    getString(R.string.sky_cloudy_rainy),
                    getString(R.string.sky_cloudy_rainy_snowy),
                    getString(R.string.sky_cloudy_shower) -> if (isNight) R.drawable.main_bg_cloudy_night else R.drawable.main_bg_cloudy

                    getString(R.string.sky_snowy),
                    getString(R.string.sky_sunny_cloudy_snowy),
                    getString(R.string.sky_cloudy_snowy) -> R.drawable.main_bg_snow

                    else -> if (isNight) R.drawable.main_bg_night else R.drawable.main_bg_clear
                }
            }
        )
    }

    private fun setMountain(sky: String?, rainType: String?) {
        when (rainType) {
            getString(R.string.sky_snowy),
            getString(R.string.sky_sunny_cloudy_snowy),
            getString(R.string.sky_cloudy_snowy) -> {
                binding.mainBottomDecoImg.setImageResource(R.drawable.bg_mt_snow)
                if (isNight) binding.mainBottomDecoImg.colorFilter = setBrightness(0.6F)
            }
            else -> {
                when (sky) {
                    getString(R.string.sky_sunny),
                    getString(R.string.sky_sunny_cloudy),
                    getString(R.string.sky_rainy),
                    getString(R.string.sky_shower),
                    getString(R.string.sky_rainy_snowy),
                    getString(R.string.sky_sunny_cloudy_shower),
                    getString(R.string.sky_sunny_cloudy_rainy),
                    getString(R.string.sky_sunny_cloudy_rainy_snowy) ->
                        if (isNight) binding.mainBottomDecoImg.setImageResource(R.drawable.bg_mt_clear_night)
                        else binding.mainBottomDecoImg.setImageResource(R.drawable.bg_mt_clear)

                    getString(R.string.sky_cloudy),
                    getString(R.string.sky_cloudy_rainy),
                    getString(R.string.sky_cloudy_rainy_snowy),
                    getString(R.string.sky_cloudy_shower) ->
                        binding.mainBottomDecoImg.setImageResource(
                            if (isNight) R.drawable.bg_mt_cloud_night else R.drawable.bg_mt_cloud
                        )

                    getString(R.string.sky_snowy),
                    getString(R.string.sky_sunny_cloudy_snowy),
                    getString(R.string.sky_cloudy_snowy) -> changeBackgroundResource(R.drawable.main_bg_snow)

                    else ->
                        binding.mainBottomDecoImg.setImageResource(
                            if (isNight) R.drawable.bg_mt_clear_night else R.drawable.bg_mt_clear)
                }
            }
        }
    }

    private fun setSkyLottie(sky: String?) {
        when(sky) {
            getString(R.string.sky_sunny),
            getString(R.string.sky_rainy),
            getString(R.string.sky_shower),
            getString(R.string.sky_rainy_snowy) -> setSkyAnimation(if (isNight) R.raw.ani_main_sunny_night else R.raw.ani_main_sunny_day)

            getString(R.string.sky_sunny_cloudy),
            getString(R.string.sky_sunny_cloudy_shower),
            getString(R.string.sky_sunny_cloudy_rainy),
            getString(R.string.sky_sunny_cloudy_rainy_snowy) -> setSkyAnimation(if (isNight) R.raw.ani_main_sunny_cloudy_night else R.raw.ani_main_sunny_cloudy_day)

            getString(R.string.sky_cloudy),
            getString(R.string.sky_cloudy_rainy),
            getString(R.string.sky_cloudy_rainy_snowy),
            getString(R.string.sky_cloudy_shower) -> setSkyAnimation(if (isNight) R.raw.ani_main_cloudy_night else R.raw.ani_main_cloudy_day)

            else -> setEmptyAnimation(1)
        }

        binding.mainSkyLottie.translationZ = -20F
        binding.mainSkyLottie.invalidate()
    }

    private fun setRainTypeLottie(rainType: String?) {
        when(rainType) {
            getString(R.string.sky_rainy),
            getString(R.string.sky_sunny_cloudy_rainy),
            getString(R.string.sky_cloudy_rainy),
            getString(R.string.sky_shower),
            getString(R.string.sky_cloudy_shower),
            getString(R.string.sky_sunny_cloudy_shower)
            -> setRainAnimation(R.raw.ani_main_rain)

            getString(R.string.sky_rainy_snowy),
            getString(R.string.sky_cloudy_rainy_snowy),
            getString(R.string.sky_sunny_cloudy_rainy_snowy)
            -> setRainAnimation(R.raw.ani_main_rain)

            getString(R.string.sky_snowy),
            getString(R.string.sky_cloudy_snowy),
            getString(R.string.sky_sunny_cloudy_snowy)
            -> setRainAnimation(R.raw.ani_main_snow)
            else -> setEmptyAnimation(2)
        }

        binding.mainRainLottie.translationZ = -10F
        binding.mainRainLottie.invalidate()
    }

    private fun setBrightness(level: Float): ColorFilter =
        ColorMatrixColorFilter(ColorMatrix().apply { setScale(level, level, level, 1f) })

    private fun setSkyAnimation(animationResource: Int?) {
        animationResource?.let {
            binding.mainSkyLottie.setAnimation(it)
            binding.mainSkyLottie.playAnimation()
        } ?: run { setEmptyAnimation(1) }
    }

    private fun setRainAnimation(animationResource: Int?) {
        animationResource?.let {
            binding.mainSkyLottie.setAnimation(it)
            binding.mainSkyLottie.playAnimation()
        } ?: run { setEmptyAnimation(2) }
    }

    private fun setEmptyAnimation(flag: Int) {
        val emptyJson = "{}"
        when(flag) {
            1 -> binding.mainSkyLottie.setAnimationFromJson(emptyJson, "emptyKey")
            2 -> binding.mainRainLottie.setAnimationFromJson(emptyJson, "emptyKey")
            else -> {
                binding.mainSkyLottie.setAnimationFromJson(emptyJson, "emptyKey")
                binding.mainRainLottie.setAnimationFromJson(emptyJson, "emptyKey")
            }
        }
    }

    private fun changeBackgroundResource(id: Int?) {
        id?.let { window.setBackgroundDrawableResource(it)
        } ?: window.setBackgroundDrawableResource(R.color.theme_view_color)

        changeTextColorStyle(id ?: R.color.theme_view_color)
    }

    // 필드값이 없을 때 -100 출력 됨
    private fun filteringNullData(data: Double?): String =
        if (data != -100.0 && data != 100.0) data?.roundToInt().toString() else ""

    // 시간별 날씨 리사이클러뷰 아이템 추가
    private fun addDailyWeatherItem(
        time: String, img: Drawable?, value: String, date: String,
        isRain: Boolean, rainP: Double?
    ) = this.dailyWeatherList.add(
            AdapterModel.DailyWeatherItem(time, img, value, date, rainP, isRain))

    // 시간별 날씨 리사이클러뷰 아이템 추가
    private fun addWeeklyWeatherItem(
        day: String, date: String, minImg: Drawable?,
        maxImg: Drawable?, minText: String, maxText: String,minRain: Int, maxRain: Int
    ) = this.weeklyWeatherList.add(
            AdapterModel.WeeklyWeatherItem(day, date, minImg, maxImg, minText, maxText,minRain,maxRain))

    // 어제와 기온 비교
    private fun getCompareTempText(y: Double?, t: Double?, tv: TextView) {
        val compared = DataTypeParser.getComparedTemp(y, t)
        compared?.let {
            val subTitle = if (compared < 0) "↓" else if (compared == 0.0) "=" else "↑"
            tv.text = "${subTitle}${compared.absoluteValue}˚"
        } ?: run {
            tv.visibility = GONE
            tv.text = ""
        }
    }

    // 에러 코드에 따라 에러 메시지 설정
    private fun setErrorMessage(error: String): String =
        when (error) {
            ErrorCode.ERROR_API_PROTOCOL, ErrorCode.ERROR_SERVER_CONNECTING, ErrorCode.ERROR_NULL_DATA -> getString(
                R.string.api_call_error
            )
            ErrorCode.ERROR_NOT_SERVICED_LOCATION -> getString(R.string.not_serviced_location_error)
            ErrorCode.ERROR_TIMEOUT -> getString(R.string.timeout_error)
            ErrorCode.ERROR_NETWORK -> getString(R.string.network_error)
            ErrorCode.ERROR_GET_LOCATION_FAILED -> getString(R.string.address_call_error)
            ErrorCode.ERROR_GPS_CONNECTED -> getString(R.string.gps_call_error)
            ErrorCode.ERROR_GET_DATA -> getString(R.string.data_call_error)
            else -> getString(R.string.unknown_error)
        }

    // 에러 버튼에 클릭 리스너 설정
    private fun setOnClickListenerForErrorButton(error: String) {
        val buttonTextResId = when (error) {
            ErrorCode.ERROR_NOT_SERVICED_LOCATION -> R.string.register_new_address
            ErrorCode.ERROR_GPS_CONNECTED -> R.string.enable_gps
            else -> R.string.renew_data
        }

        binding.mainErrorRenewBtn.apply {
            text = getString(buttonTextResId)
            setOnClickListener {
                mVib()
                when (error) {
                    ErrorCode.ERROR_NOT_SERVICED_LOCATION -> {
                        val bottomSheet = SearchDialog(
                            this@MainActivity,
                            1,
                            supportFragmentManager,
                            BottomSheetDialogFragment().tag
                        )
                        bottomSheet.show(1)
                    }
                    ErrorCode.ERROR_GPS_CONNECTED -> locationClass.requestSystemGPSEnable()
                    else -> getDataSingleTime(isCurrent = false)
                }
            }
        }
    }

    // 에러 메시지와 뷰 가시성 설정
    private fun updateViewsForError(error: String) {
        hideProgressBar()
        binding.mainErrorTitle.text = setErrorMessage(error)
        setVisibilityForViews(GONE, error)
    }

    // 모든 뷰 숨김 처리 및 에러 메시지 표시
    private fun hideAllViews(error: String?) {
        val isThemeNight = GetSystemInfo.isThemeNight(this@MainActivity)

        runOnUiThread {
            error?.let { e ->
                setOnClickListenerForErrorButton(e)
                updateViewsForError(e)
            }
            binding.mainSkyImg.apply {
                changeBackgroundResource(if (isThemeNight) R.color.black else R.color.white)
                setDrawable(this@apply, if (isThemeNight) R.drawable.ico_error_b else R.drawable.ico_error_w)
            }
        }
    }

    // 통신에 성공할 경우 레이아웃 처리
    private fun showAllViews() = setVisibilityForViews(VISIBLE, null)

    // 레이아웃 숨김처리에 따른 뷰 세팅
    private fun setVisibilityForViews(visibility: Int, error: String?) {
        runOnUiThread {
            val textViewArray = arrayListOf(
                binding.mainGpsTitleTv,
                binding.mainLiveTempValue,
                binding.mainLiveTempUnit,
                binding.mainTopBarGpsTitle,
                binding.mainSensTitle,
                binding.mainSensValue,
                binding.mainMaxValue,
                binding.mainMinValue,
                binding.mainMinMaxUnit,
                binding.subAirPM25,
                binding.subAirPM10,
                binding.subAirHumid.getTitle(),
                binding.subAirWind.getTitle(),
                binding.subAirRainP.getTitle(),
                binding.mainCompareTempTv
            )

            val clickableChangeArray = arrayOf(
                binding.mainSideMenuIv,
                binding.mainShareIv,
                binding.mainAddAddress,
                binding.mainGpsFix
            )

            clickableChangeArray.forEach { it.isEnabled = visibility == VISIBLE }

            // 숨김
            if (visibility == GONE) {
                if (error == ErrorCode.ERROR_NETWORK || error == ErrorCode.ERROR_GET_DATA) {
                    setDrawable(binding.mainAddAddress, null)
                    setDrawable(binding.mainSideMenuIv, null)
                } else tintImageDrawables()

                clearTextViews(textViewArray)

                setDrawable(binding.mainGpsFix, null)
                setDrawable(binding.mainMotionSLideImg, null)
                setDrawable(binding.mainGpsFix, null)
                binding.mainShareIv.isEnabled = false
                binding.mainSwipeLayout.isEnabled = false

                binding.mainMotionLayout.apply {
                    transitionToStart()
                    HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                        isInteractionEnabled = false // 모션 레이아웃의 스와이프를 막음
                    },100)
                }

                applyBackground(binding.mainWarningBox, null)
                applyBackground(binding.nestedSubAirFrame, null)

                changeStrokeColor(binding.subAirPM10, getColor(android.R.color.transparent))
                changeStrokeColor(binding.subAirPM25, getColor(android.R.color.transparent))

                updateErrorViewsVisibility(GONE)
            } else updateViewsForVisibleState()
        }
    }

    private fun updateViewsForVisibleState() {
        // 보임
        binding.mainSensTitle.text = getString(R.string.sens_temp)
        binding.subAirHumid.getTitle().text = getString(R.string.humidity)
        binding.subAirWind.getTitle().text = getString(R.string.wind)
        binding.subAirRainP.getTitle().text = getString(R.string.rainPer)
        binding.subAirWave.getTitle().text = getString(R.string.wave)
        applyBackground(binding.mainWarningBox, R.drawable.report_frame_bg)
        binding.mainShareIv.isEnabled = true

        setDrawable(binding.mainGpsFix, R.drawable.gps_fix)
        setDrawable(binding.mainMotionSLideImg, R.drawable.drop_down_bottom)
        setDrawable(binding.mainAddAddress, R.drawable.search)
        setDrawable(binding.mainSideMenuIv, R.drawable.ico_hamb_w)

        // 원래 상태로 복구하기 위해 제약 조건 변경
        binding.mainMotionLayout.isInteractionEnabled = true

        updateErrorViewsVisibility(VISIBLE)
    }

    // 이미지뷰의 이미지를 설정
    private fun setDrawable(imageView: ImageView, drawableResId: Int?) =
        drawableResId?.let { imageView.setImageDrawable(getR(it)) } ?: imageView.setImageDrawable(null)

    // 이미지뷰의 이미지 틴트 적용
    private fun tintImageDrawables() {
        binding.mainAddAddress.imageTintList = ColorStateList.valueOf(getColor(R.color.theme_text_color))
        binding.mainSideMenuIv.imageTintList = ColorStateList.valueOf(getColor(R.color.theme_text_color))
        binding.mainAddAddress.isEnabled = true
        binding.mainSideMenuIv.isEnabled = true
    }

    // 텍스트뷰의 텍스트 지우기
    private fun clearTextViews(textViews: List<TextView>) = textViews.forEach { it.text = "" }

    // 텍스트뷰 테두리 색상 변경
    private fun changeStrokeColor(textView: TextView, color: Int) {
        // 특정 상황에 맞게 원하는 색상으로 변경
        textView.setTextColor(color)
        textView.background.mutate().let { background ->
            (background as GradientDrawable).setStroke(3, color) // 테두리 두께와 색상 변경
        }
    }

    // 에러 관련 뷰의 가시성 업데이트
    private fun updateErrorViewsVisibility(visibility: Int) {
        binding.mainLoadingView.alpha = 0f
        binding.mainWarningVp.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainErrorTitle.alpha = if (visibility == VISIBLE) 0f else 1f
        binding.mainErrorRenewBtn.alpha = if (visibility == VISIBLE) 0f else 1f
        binding.subAirHumid.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.subAirWind.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.subAirRainP.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainSkyLottie.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainRainLottie.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainShareIv.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainSkyText.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainErrorRenewBtn.isClickable = visibility == GONE
    }

    // 현재 지역의 날씨 데이터 뷰모델 생성 및 호출
    private fun loadCurrentViewModelData(lat: Double, lng: Double, addr: String?) =
        getDataViewModel.loadData(lat, lng, addr)


    // 저장된 지역의 날씨 데이터 뷰모델 생성 및 호출
    private fun loadSavedViewModelData(addr: String) = getDataViewModel.loadData(null, null, addr)

    // 현재 위치 정보로 DB 갱신
    private fun updateCurrentAddress(mLat: Double, mLng: Double, mAddr: String?) =
        locationClass.updateDatabaseWithLocationData(mLat, mLng, mAddr)

    // 자외선 단계별 대응요령 아이템 추가
    private fun addUvResponseItem(text: Array<String>) {
        val itemList = text.map { AdapterModel.UVResponseItem(it) }
        itemList.forEachIndexed { index, item ->
            uvResponseList.add(index, item)
            uvResponseAdapter.notifyItemInserted(index)
        }
    }

    // 자외선 지수에 따른 대처요령 불러오기
    private fun getUvArray(grade: String): Array<String> {
        return when (grade) {
            getString(R.string.uv_caution) -> resources.getStringArray(R.array.uv_caution)
            getString(R.string.uv_very_high) -> resources.getStringArray(R.array.uv_very_high)
            getString(R.string.uv_high) -> resources.getStringArray(R.array.uv_high)
            getString(R.string.uv_normal) -> resources.getStringArray(R.array.uv_normal)
            getString(R.string.uv_low) -> resources.getStringArray(R.array.uv_low)
            else -> resources.getStringArray(R.array.uv_none)
        }
    }

    // 자외선 단계별 대응요령 필터링
    private fun applyUvResponseItem(grade: String) {
        uvResponseList.clear()
        addUvResponseItem(getUvArray(grade))
    }

    // 메인화면 배경에 따라 텍스트의 색상을 변경
    @SuppressLint("UseCompatTextViewDrawableApis", "NotifyDataSetChanged")
    private fun changeTextColorStyle(bg: Int) {
        val changeColorTextViews = listOf(
            binding.mainLiveTempValue,
            binding.mainLiveTempUnit,
            binding.mainCompareTempTv,
            binding.mainTopBarGpsTitle,
            binding.mainGpsTitleTv,
            binding.mainSensTitle,
            binding.mainSensValue,
            binding.mainLiveTempTitleC,
            binding.subAirWind.getTitle(),
            binding.subAirRainP.getTitle(),
            binding.subAirHumid.getTitle(),
            binding.subAirWave.getTitle(),
            binding.subAirWind.getValue(),
            binding.subAirRainP.getValue(),
            binding.subAirHumid.getValue(),
            binding.subAirWave.getValue(),
            binding.mainLiveTempValueC,
            binding.mainSensTitleC,
            binding.mainSensValueC,
            binding.mainMinMaxTitleC,
            binding.mainMinMaxValueC,
            binding.mainDailyWeatherTitle,
            binding.mainSunTitle,
            binding.mainWeeklyWeatherTitle,
            binding.mainSunRiseTitle,
            binding.mainSunSetTitle,
            binding.mainSunRiseTime,
            binding.mainSunSetTime,
            binding.mainSunTomTitle,
            binding.mainSunSetTom,
            binding.mainSunRiseTom,
            binding.mainTermsTitle,
            binding.mainTermsExplain,
            binding.mainSkyText,
            binding.mainUvTitle,
            binding.mainMinValue,
            binding.mainMaxValue,
            binding.mainMinMaxUnit
        )
        val changeColorSubTextViews = listOf(binding.mainLicenseText)
        val changeTintLineViews = listOf(binding.mainSunLine)
        val changeTintImageViews = listOf(
            binding.mainSideMenuIv, binding.mainAddAddress,
            binding.mainGpsFix, binding.mainShareIv
        )
        val changeBoxViews = listOf(
            binding.mainWarningBox, binding.nestedSubAirFrame,
            binding.nestedDailyBox, binding.nestedWeeklyBox,
            binding.mainUVBox, binding.mainSunBox,
            binding.nestedTerms24Box, binding.mainLunarBox
        )

        val gridBoxView = listOf(
            binding.mainAirPm10, binding.mainAirPm2p5, binding.mainAirSo2,
            binding.mainAirNo2, binding.mainAirCo, binding.mainAirO3
        )

        // 리소스 색상 가져오기
        val colorWhite = getColor(R.color.white)
        val colorBlack = getColor(R.color.main_black)
        val colorSubWhite = getColor(R.color.sub_white)
        val colorSubBlack = getColor(R.color.sub_black)

        // 글자색 변경 함수
        fun changeTextColor(color: Int, subColor: Int, isWhite: Boolean) {
            // 일괄 처리를 통한 업데이트 지연
            CoroutineScope(mainDispatcher).launch {
                changeColorTextViews.forEach { it.setTextColor(color) }
                changeColorSubTextViews.forEach { it.setTextColor(subColor) }
                changeTintLineViews.forEach { it.setBackgroundColor(color) }
                changeTintImageViews.forEach { it.imageTintList = ColorStateList.valueOf(color) }
                delay(100L)

                launch {
                    binding.mainTopBarGpsTitle.compoundDrawablesRelative[0].mutate().setTint(color)
                    binding.subAirWind.getValue().compoundDrawableTintList = ColorStateList.valueOf(color)

                    gridBoxView.forEach { it.fetchWhite(isWhite) }
                    dailyWeatherAdapter.setIsWhite(isWhite)
                    weeklyWeatherAdapter.setIsWhite(isWhite)
                    uvResponseAdapter.setIsWhite(isWhite)
                    uvResponseAdapter.notifyDataSetChanged()
                    weeklyWeatherAdapter.notifyDataSetChanged()
                    dailyWeatherAdapter.notifyDataSetChanged()
                    dailyWeatherAdapter.submitList(dailyWeatherList)
                    warningViewPagerAdapter.changeTextColor(color)
                    reportViewPagerItem.addAll(warningList)
                    warningViewPagerAdapter.notifyDataSetChanged()
                }
            }
        }

        val savedProgressBlackBox = GetAppInfo.getWeatherBoxOpacity(this)
        val transSavedProgressBlackBox = DataTypeParser.progressToHex(savedProgressBlackBox)
        val savedProgressWhiteBox = GetAppInfo.getWeatherBoxOpacity2(this)
        val transSavedProgressWhiteBox = DataTypeParser.progressToHex(savedProgressWhiteBox)

        // 글자색 변경: 텍스트 및 리소스 색상 사용
        fun changeTextToWhite() {
            changeTextColor(colorWhite, colorSubWhite, true)
            changeBoxViews.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#${transSavedProgressWhiteBox}000000"))
            }
            gridBoxView.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#${transSavedProgressWhiteBox}000000"))
            }
            binding.mainMotionSLideImg.imageTintList = ColorStateList.valueOf(getColor(R.color.white))
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        fun changeTextToBlack() {
            changeTextColor(colorBlack, colorSubBlack, false)
            changeBoxViews.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#${transSavedProgressBlackBox}FFFFFF"))
            }
            gridBoxView.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#${transSavedProgressBlackBox}FFFFFF"))
            }
            binding.mainMotionSLideImg.imageTintList = ColorStateList.valueOf(getColor(R.color.white))
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        // 주어진 조건에 따라 텍스트 색상 변경
        when (bg) {
            R.drawable.main_bg_clear, R.drawable.main_bg_snow -> changeTextToBlack()
            R.drawable.main_bg_night, R.drawable.main_bg_cloudy, R.drawable.main_bg_cloudy_night ->
                changeTextToWhite()
        }

        window.navigationBarColor = getColor(android.R.color.transparent)
        window.statusBarColor = getColor(android.R.color.transparent)

        setSectionTextColor(
            binding.dailySectionToday,
            binding.dailySectionTomorrow,
            binding.dailySectionAfterTomorrow
        )
    }

    // 기상특보 자동 슬라이드 적용
    private fun warningSlideAuto() {
        val vp = binding.mainWarningVp
        if (warningList.size > 1) {
            vp.currentItem = if (vp.currentItem + 1 < warningList.size) vp.currentItem + 1 else 0
            Handler(Looper.getMainLooper()).postDelayed({ warningSlideAuto() }, 5000)
        }
    }

    // 현재 위치가 한국인지 아닌지 구분
    private fun isKorea(lat: Double, lng: Double): Boolean = lng in 125.0..132.0 && lat in 33.0..39.0

    private fun checkLocationAvailability() {
        if (!locationClass.isNetWorkConnected())  {
            hideAllViews(ErrorCode.ERROR_NETWORK)
            return
        }

        if (locationClass.isGPSConnected()) {
            requestLocationWithGPS()
            return
        }

        if (locationClass.isNetworkProviderConnected()) {
            requestLocationWithNetworkProvider()
            return
        }

        hideAllViews(ErrorCode.ERROR_GPS_CONNECTED)
    }

    private fun requestLocationWithGPS() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val onSuccess: (Location?) -> Unit = { location ->
            location?.let { loc ->
                val lat = loc.latitude
                val lng = loc.longitude
                if (isKorea(lat, lng)) processAddress(lat, lng, locationClass.getAddress(lat, lng))
                else {
                    ToastUtils(this).showMessage(getString(R.string.error_not_service_locale))
                    loadSavedViewModelData(getString(R.string.seoul_si))
                }
            } ?: run {
                hideProgressBar()
                callSavedLoc()
            }
        }

        val onFailure: (e: Exception) -> Unit = {
            it.printStackTrace()
            hideProgressBar()
            callSavedLoc()
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    private fun callSavedLoc() {
        kotlin.runCatching {
            ioThread.launch {
                val db = GpsRepository(this@MainActivity).findByName(SpDao.CURRENT_GPS_ID)
                val lat = db?.lat
                val lng = db?.lng

                if (lat == null || lng == null) {
                    ToastUtils(this@MainActivity)
                        .showMessage(getString(R.string.error_not_service_locale))
                    loadSavedViewModelData(getString(R.string.seoul_si))
                } else {
                    if (isKorea(lat, lng)) {
                        val getAddr = locationClass.getAddress(lat, lng)
                        if (getAddr != "") {
                            ToastUtils(this@MainActivity)
                                .showMessage(getString(R.string.last_location_call_msg), 1)
                            processAddress(lat, lng, getAddr)
                        } else hideAllViews(ErrorCode.ERROR_GET_LOCATION_FAILED)
                    } else loadSavedViewModelData(getString(R.string.seoul_si))
                }
            }
        }.onFailure { exception ->
            when(exception) {
                NullPointerException() -> hideAllViews(ErrorCode.ERROR_GET_LOCATION_FAILED)
                NumberFormatException() -> {
                    handleLocationFailure()
                    hideAllViews(ErrorCode.ERROR_GET_LOCATION_FAILED)
                }
            }
        }
    }

    private fun requestLocationWithNetworkProvider() =
        CoroutineScope(backgroundDispatcher).launch {
            (getSystemService(LOCATION_SERVICE) as LocationManager)
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                        loc -> loadCurrentViewModelData(loc.latitude, loc.longitude, null)
                }
        }

    private fun processAddress(lat: Double, lng: Double, address: String?) {
        address?.let { addr ->
            // 주소 정보를 저장하고 업데이트
            SetAppInfo.setUserLastAddr(this@MainActivity, addr)
            updateCurrentAddress(lat, lng, addr)

            // 주소 문자열에서 불필요한 부분을 제거
            val cleanedAddr = addr
                .replaceFirst(" ", "")
                .replace(getString(R.string.korea), "")
                .replace("null", "")

            // 주소 포맷을 정의하거나 필요한 경우 다른 변환 작업을 수행
            val formattedAddr =
                if (GetAppInfo.getUserLocation(this@MainActivity) == StaticDataObject.LANG_EN)
                    cleanedAddr.replace("South Korea", "")
                else AddressFromRegex(cleanedAddr).getAddress()

            loadCurrentViewModelData(lat, lng, formattedAddr)
            updateAddress(formattedAddr)
        }
    }

    private fun getR(id: Int): Drawable? = ResourcesCompat.getDrawable(resources, id, null)

    // 뷰 백그라운드 적용
    private fun <T> applyBackground(view: T, res: Int?) =
        res?.let { (view as View).background = getR(it) } ?: apply { (view as View).background = null }

    private fun handleLocationFailure() = hideProgressBar()

    fun recreateMainActivity(addrKr: String?, addrEn: String?) = addrKr?.let { loadSavedAddr(addrKr, addrEn) }
}