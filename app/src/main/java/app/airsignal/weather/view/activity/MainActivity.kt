package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.animation.*
import android.webkit.WebView
import android.widget.*
import android.widget.LinearLayout.VISIBLE
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
import app.airsignal.weather.address.AddressFromRegex
import app.airsignal.weather.as_eye.activity.EyeListActivity
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.dao.IgnoredKeyFile.landingPageUrl
import app.airsignal.weather.dao.IgnoredKeyFile.lastAddress
import app.airsignal.weather.dao.IgnoredKeyFile.playStoreURL
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.dao.StaticDataObject.LANG_EN
import app.airsignal.weather.dao.StaticDataObject.LANG_KR
import app.airsignal.weather.databinding.ActivityMainBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.room.repository.GpsRepository
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.GetAppInfo.getEntireSun
import app.airsignal.weather.db.sp.GetAppInfo.getIsNight
import app.airsignal.weather.db.sp.GetAppInfo.getTopicNotification
import app.airsignal.weather.db.sp.GetAppInfo.getUserLastAddress
import app.airsignal.weather.db.sp.GetAppInfo.getUserLocation
import app.airsignal.weather.db.sp.GetAppInfo.getUserLoginPlatform
import app.airsignal.weather.db.sp.GetAppInfo.isLandingNotification
import app.airsignal.weather.db.sp.GetAppInfo.millsToString
import app.airsignal.weather.db.sp.GetAppInfo.parseTimeToMinutes
import app.airsignal.weather.db.sp.GetSystemInfo.getLocale
import app.airsignal.weather.db.sp.GetSystemInfo.isThemeNight
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.db.sp.SetAppInfo.removeSingleKey
import app.airsignal.weather.db.sp.SetAppInfo.setLandingNotification
import app.airsignal.weather.db.sp.SetAppInfo.setUserLastAddr
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.db.sp.SpDao.CURRENT_GPS_ID
import app.airsignal.weather.db.sp.SpDao.IN_APP_MSG
import app.airsignal.weather.firebase.admob.AdViewClass
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.location.GetLocation
import app.airsignal.weather.login.SilentLoginClass
import app.airsignal.weather.network.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.network.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.network.ErrorCode.ERROR_GET_LOCATION_FAILED
import app.airsignal.weather.network.ErrorCode.ERROR_GPS_CONNECTED
import app.airsignal.weather.network.ErrorCode.ERROR_LOCATION_FAILED
import app.airsignal.weather.network.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.network.ErrorCode.ERROR_NOT_SERVICED_LOCATION
import app.airsignal.weather.network.ErrorCode.ERROR_NULL_DATA
import app.airsignal.weather.network.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.network.ErrorCode.ERROR_TIMEOUT
import app.airsignal.weather.network.NetworkUtils.modifyCurrentHumid
import app.airsignal.weather.network.NetworkUtils.modifyCurrentWindSpeed
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.*
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.util.`object`.DataTypeParser.applySkyImg
import app.airsignal.weather.util.`object`.DataTypeParser.applySkyText
import app.airsignal.weather.util.`object`.DataTypeParser.convertValueToGrade
import app.airsignal.weather.util.`object`.DataTypeParser.dateAppendZero
import app.airsignal.weather.util.`object`.DataTypeParser.getComparedTemp
import app.airsignal.weather.util.`object`.DataTypeParser.getDataColor
import app.airsignal.weather.util.`object`.DataTypeParser.getDataText
import app.airsignal.weather.util.`object`.DataTypeParser.getHourCountToTomorrow
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgSmall
import app.airsignal.weather.util.`object`.DataTypeParser.isRainyDay
import app.airsignal.weather.util.`object`.DataTypeParser.parseDayOfWeekToKorean
import app.airsignal.weather.util.`object`.DataTypeParser.parseLocalDateTimeToLong
import app.airsignal.weather.util.`object`.DataTypeParser.setUvBackgroundColor
import app.airsignal.weather.util.`object`.DataTypeParser.translateSky
import app.airsignal.weather.util.`object`.DataTypeParser.translateSkyText
import app.airsignal.weather.util.`object`.DataTypeParser.translateUV
import app.airsignal.weather.view.*
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.MakeSingleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import app.airsignal.weather.view.custom_view.SnackBarUtils
import app.airsignal.weather.view.dialog.*
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import app.airsignal.weather.viewmodel.GetWeatherViewModel
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


@SuppressLint("InflateParams")
class MainActivity
    : BaseActivity<ActivityMainBinding>() {
    override val resID: Int get() = R.layout.activity_main

    companion object {
        const val SHOWING_LOADING_FLOAT = 1f
        const val NOT_SHOWING_LOADING_FLOAT = 0f
        const val PM2p5_INDEX = 0
        const val PM10_INDEX = 1
        const val CO_INDEX = 2
        const val SO2_INDEX = 3
        const val NO2_INDEX = 4
        const val O3_INDEX = 5
    }

    private val fcm by lazy {SubFCM()}

    private var isBackPressed = false
    private var isProgressed = false
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
    private val inAppList = ArrayList<ApiModel.InAppMsgItem>()
    private val warningList = ArrayList<String>()
    private val uvLegendAdapter = UVLegendAdapter(this, uvLegendList)
    private val uvResponseAdapter = UVResponseAdapter(this, uvResponseList)
    private val airQList = ArrayList<AdapterModel.AirQTitleItem>()
    private val airQAdapter = AirQTitleAdapter(this, airQList)
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

    private val inAppAdapter by lazy {InAppViewPagerAdapter(this@MainActivity, inAppList)}

    private val adViewClass by lazy { AdViewClass(this) }

    private var isInAppMsgShow = false

    private val fetch by lazy {getDataViewModel.fetchData()}

    override fun onResume() {
        super.onResume()
        addSideMenu()
//        binding.nestedAdView.resume()
        applyRefreshScroll()
        getDataSingleTime(isCurrent = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        isWarned = false
//        binding.nestedAdView.destroy()
    }

    private fun destroyObserver() {
        getDataViewModel.cancelJob()
        fetch.removeObservers(this)
        TimberUtil().w("lifecycle_test", "메인 옵저버 제거")
    }

    override fun onPause() {
        super.onPause()
//        binding.nestedAdView.pause()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            initBinding()
            if (savedInstanceState == null) {
                showProgressBar()
                fcm.subTopic(SubFCM.Sort.FCM_PATCH.key)
                fcm.subTopic(SubFCM.Sort.FCM_DAILY.key)
                changeBackgroundResource(null)
                window.statusBarColor = getColor(R.color.theme_view_color)
                window.navigationBarColor = getColor(R.color.theme_view_color)
                binding.mainMotionLayout.apply {
                    isInteractionEnabled = false // 모션 레이아웃의 스와이프를 막음
                    isEnabled = false
                    setTransition(R.id.start, R.id.end)
                }
            }

//            adViewClass.loadAdView(binding.nestedAdView)  // adView 생성

            initializing()

            sunPb.disableTouch()    // 일출/일몰 그래프 클릭 방지

            // 메인 하단 스크롤 유도 화살표 애니메이션 적용
            val bottomArrowAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_arrow_anim)
            binding.mainMotionSLideImg.startAnimation(bottomArrowAnim)
            val eyeIconAnim = AnimationUtils.loadAnimation(this, R.anim.anim_eye_icon_scale)
            if (!SharedPreferenceManager(this).getBoolean(SpDao.TUTORIAL_SKIP, false))
                binding.mainTopEye.startAnimation(eyeIconAnim)

            // UV 범주 아이템 추가
            addUvLegendItem(0, "0 - 2", getColor(R.color.uv_low), getString(R.string.uv_low))
            addUvLegendItem(1, "3 - 5", getColor(R.color.uv_normal), getString(R.string.uv_normal))
            addUvLegendItem(2, "6 - 7", getColor(R.color.uv_high), getString(R.string.uv_high))
            addUvLegendItem(3, "8 - 10", getColor(R.color.uv_very_high), getString(R.string.uv_very_high))
            addUvLegendItem(4, "11 - ", getColor(R.color.uv_caution), getString(R.string.uv_caution))

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

            binding.mainTopEye.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if (SharedPreferenceManager(this@MainActivity).getString("user_email") != "") {
                        val intent = Intent(this@MainActivity, EyeListActivity::class.java)
                        startActivity(intent)
                    } else {
                        val builder = MakeDoubleDialog(this@MainActivity)
                        val dialog = builder.make("로그인이 필요한 서비스입니다.",
                            "로그인","취소",R.color.main_blue_color)
                        dialog.first.setOnClickListener {
                            builder.dismiss()
                            EnterPageUtil(this@MainActivity).toLogin("main")
                        }
                        dialog.second.setOnClickListener {
                            builder.dismiss()
                        }
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
        } catch (e : androidx.fragment.app.Fragment.InstantiationException) {
            RefreshUtils(this).refreshApplication()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun startInAppMsg() {
        @Suppress("DEPRECATION") val inAppExtraList = intent.getParcelableArrayExtra(IN_APP_MSG)?.map {it as ApiModel.InAppMsgItem}
        inAppList.clear()
        inAppExtraList?.let {
            it.forEach { dao ->
                inAppList.add(dao)
                inAppAdapter.notifyDataSetChanged()
            }
            val oneHour = (1000 * 60 * 60).toLong()
            val sevenDays = (1000 * 60 * 60 * 24 * 7).toLong()

            if (it.isNotEmpty()) {
                if (!GetAppInfo.getInAppMsgEnabled(this@MainActivity)) {
                    if (isTimeToDialog(oneHour))  runOnUiThread { inAppMsgDialog() }
                } else {
                    if (isTimeToDialog(sevenDays)) runOnUiThread { inAppMsgDialog() }
                }
            }
        }
    }

    private suspend fun isTimeToDialog(long: Long): Boolean = withContext(Dispatchers.IO) {
        return@withContext LocalDateTime.now()
            .isAfter(
                DataTypeParser.parseLongToLocalDateTime(
                    GetAppInfo.getInAppMsgTime(
                        this@MainActivity
                    ) + (long)
                )
            )
    }

    private fun inAppMsgDialog() {
        val inAppDialog = AlertDialog.Builder(this, R.style.InAppDialogStyle)
        val inAppView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_in_app_msg,null,false)
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
            CoroutineScope(Dispatchers.IO).launch {
                SetAppInfo.setInAppMsgDenied(this@MainActivity,false)
                withContext(Dispatchers.Main) {
                    inAppAlert.dismiss()
                }
            }
        }

        inAppHide.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                SetAppInfo.setInAppMsgDenied(this@MainActivity,true)
                withContext(Dispatchers.Main) {
                    inAppAlert.dismiss()
                }
            }
        }

        if (inAppList.isNotEmpty() && inAppList.size > 1) {
            inAppIndicator.removeAllViews()
            IndicatorView(this, inAppList.size).createIndicators(inAppIndicator,inAppVp,ColorStateList.valueOf(getColor(R.color.white)))
        }

        inAppAlert.show()
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

    private fun applyRefreshScroll() {
        // MotionLayout 상태 변경 리스너를 설정합니다.
        // 스와이프 리프래시 레이아웃의 상태를 모션레이아웃의 스와이프 상태에 맞춰서 변경합니다.
        binding.mainMotionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout,
                startId: Int,
                endId: Int
            ) {}

            override fun onTransitionChange(
                motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float
            ) {}

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                binding.mainSwipeLayout.isEnabled = motionLayout.currentState == R.id.start
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout, triggerId: Int, positive: Boolean, progress: Float
            ) {}
        }
        )
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
            val eye = sideMenuView.findViewById<TextView>(R.id.navMenuEye)

            sideMenuBuilder.apply {
                setBackPressed(cancel)
                setUserData(profile, id)
                adViewClass.loadAdView(adView)
            }

            if (getUserLocation(this) == LANG_EN || getLocale(this) == Locale.ENGLISH)
                warning.visibility = GONE
            else {
                warning.visibility = VISIBLE
                warning.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        sideMenuBuilder.dismiss()
                        val intent = Intent(this@MainActivity, WarningDetailActivity::class.java)
                        startActivity(intent)
                    }
                })
            }

            headerTr.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if (getUserLoginPlatform(this@MainActivity) == "") {
                        CompletableFuture.supplyAsync {
                            sideMenuBuilder.dismiss()
                        }.thenAccept {
                            EnterPageUtil(this@MainActivity).toLogin("main")
                        }
                    }
                }
            })
            weather.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    sideMenuBuilder.dismiss()
                }
            })
            eye.setOnClickListener(object : OnSingleClickListener() {
                @SuppressLint("SetJavaScriptEnabled")
                override fun onSingleClick(v: View?) {
                    val intent = Intent(this@MainActivity, EyeListActivity::class.java)
                    startActivity(intent)
                }
            })
            setting.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    CompletableFuture.supplyAsync {
                        sideMenuBuilder.dismiss()
                    }.thenAccept {
                        val intent = Intent(this@MainActivity, SettingActivity::class.java)
                        startActivity(intent)
                    }
                }
            })
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun startLanding() {
        sideMenuBuilder.dismiss()
        val landingView = LayoutInflater
            .from(this@MainActivity)
            .inflate(R.layout.dialog_eye_landing, null)
        val landingBack = landingView.findViewById<ImageView>(R.id.eyeLandingBack)
        val landingBtn =
            landingView.findViewById<RelativeLayout>(R.id.eyeLandingRelative)
        val landingText = landingView.findViewById<TextView>(R.id.eyeLandingBtnTitle)
        val landingCheck: CheckBox = landingView.findViewById(R.id.eyeLandingBtnCheck)
        val landingWebView: WebView = landingView.findViewById(R.id.eyeLandingWebView)
        val landingFab: ImageView = landingView.findViewById(R.id.eyeLandingFab)
        val notiPerm = RequestPermissionsUtil(this@MainActivity)
        val dialog = ShowDialogClass(this@MainActivity, false).setBackPressed(landingBack)

        val requestOkText = "출시 알림받기 완료 \uD83D\uDE00"
        val requestNoText = "제품 출시 알림받기\n광고 및 이벤트성 알림 발송에 동의합니다"

        // 웹뷰 세팅
        WebViewSetting().apply(landingWebView)

        landingWebView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            landingFab.visibility = if (scrollY == 0) GONE else View.VISIBLE
        }

        landingFab.setOnClickListener {
            landingWebView.pageUp(true)
        }

        landingWebView.loadUrl(landingPageUrl)

        landingBtn.isActivated = landingCheck.isChecked

        landingCheck.visibility = if (getLandingEnable()) GONE else VISIBLE

        landingCheck.setOnCheckedChangeListener { _, isChecked ->
            landingBtn.isActivated = isChecked
        }

        val span = SpannableStringBuilder(requestNoText)
        val tx = requestNoText.split('\n')[1]
        span.setSpan(
            RelativeSizeSpan(0.7f),
            requestNoText.indexOf(tx),
            requestNoText.lastIndex + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        span.setSpan(
            ForegroundColorSpan(Color.parseColor("#90FFFFFF")),
            requestNoText.indexOf(tx),
            requestNoText.lastIndex + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        landingBack.bringToFront()
        if (!getLandingEnable()) landingText.text = span else landingText.text = requestOkText

        landingBtn.setOnClickListener {
            if (!getLandingEnable()) {
                if (!notiPerm.isNotificationPermitted()) {
                    ToastUtils(this@MainActivity).showMessage("알림을 허용해주세요")
                    notiPerm.requestNotification()
                } else {
                    if (landingCheck.isChecked) {
                        val subModal = MakeSingleDialog(this@MainActivity)
                        subModal.makeDialog(
                            "출시가 완료되면 알림 메시지를 보낼게요 ${String(Character.toChars(0x1F514))}",
                            R.color.main_blue_color, "확인", false
                        )
                        subModal.apply.setOnClickListener {
                            CoroutineScope(Dispatchers.IO).launch {
                                setLandingNotification(this@MainActivity,true)
                            }
                            landingBtn.isActivated = false
                            landingText.text = requestOkText
                            landingCheck.visibility = GONE
                            subModal.dismiss()
                        }
                    } else {
                        ToastUtils(this@MainActivity).showMessage("알림 동의를 체크해주세요!")
                    }
                }
            } else {
                val cancelModal = MakeDoubleDialog(this@MainActivity)
                val make = cancelModal.make("알림받기를 취소하시겠습니까?","네",
                    "아니오",R.color.red)
                make.first.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        setLandingNotification(this@MainActivity,false)

                        withContext(Dispatchers.Main) {
                            SnackBarUtils(landingView,"성공적으로 취소되었습니다",
                                getR(R.drawable.alert_off)!!).show()
                            landingBtn.isActivated = false
                            landingText.text = span
                            landingCheck.isChecked = false
                            landingCheck.visibility = VISIBLE
                            cancelModal.dismiss()
                        }
                    }
                }
                make.second.setOnClickListener {
                    cancelModal.dismiss()
                }
            }
        }
        dialog.show(landingView, true, null)
    }

    private fun getLandingEnable(): Boolean {
        return isLandingNotification(this@MainActivity)
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
        val isWhite = dailyWeatherAdapter.getIsWhite()
        t1.typeface = Typeface.createFromAsset(
            assets,
            if (isWhite) "spoqa_hansansneo_bold.ttf" else "spoqa_hansansneo_medium.ttf"
        )
        t2.typeface = Typeface.createFromAsset(
            assets,
            if (isWhite) "spoqa_hansansneo_regular.ttf" else "spoqa_hansansneo_regular.ttf"
        )
        t3.typeface = Typeface.createFromAsset(
            assets,
            if (isWhite) "spoqa_hansansneo_regular.ttf" else "spoqa_hansansneo_regular.ttf"
        )
        t1.setTextColor(getColor(if (isWhite) R.color.white else R.color.main_blue_color))
        t2.setTextColor(getColor(if (isWhite) R.color.sub_white else R.color.sub_black))
        t3.setTextColor(getColor(if (isWhite) R.color.sub_white else R.color.sub_black))
    }

    // 날씨 데이터 API 호출
    private val permissionsUtil = RequestPermissionsUtil(this)

    private fun getDataSingleTime(isCurrent: Boolean) {
        if (isNetworkAndLocationPermitted()) {
            val lastAddress = getUserLastAddress(this)
            if (!isCurrent) {
                val addrArray = resources.getStringArray(R.array.address_korean)
                if (addrArray.contains(lastAddress)) {
                    addrArray.forEachIndexed { index, address ->
                        if (lastAddress == address) {
                            loadSavedAddr(
                                addrArray[index],
                                resources.getStringArray(R.array.address_english)[index]
                            )
                        }
                    }
                } else {
                    checkLocationAvailability()
                }
            } else {
                checkLocationAvailability()
            }

            // TimeOut
            HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                hideProgressBar()
            }, 1000 * 8)
        }
    }

    private fun isNetworkAndLocationPermitted(): Boolean {
        return permissionsUtil.isNetworkPermitted() && permissionsUtil.isLocationPermitted()
    }

    // 저장된 주소로 데이터 호출
    private fun loadSavedAddr(addr: String?, enAddr: String?) {
        addr?.let { mAddr ->
            loadSavedViewModelData(mAddr)

            val isSearched = true
            val gpsValue = if (getUserLocation(this) == LANG_EN) enAddr?.trim() else mAddr.trim()

            RDBLogcat.writeGpsHistory(this, isSearched, gpsValue ?: "잘못된 주소", null)

            updateAddress(gpsValue)
        }
    }

    private fun setProgressVisibility(show: Boolean) {
        if (show) {
            if (!isProgressed) {
                isProgressed = true
                binding.mainLoadingView.visibility = View.VISIBLE
                binding.mainLoadingView.alpha = SHOWING_LOADING_FLOAT
                binding.mainLoadingView.bringToFront()
                binding.mainMotionLayout.isInteractionEnabled = false
                binding.mainMotionLayout.isEnabled = false
            }
        } else {
            if (binding.mainLoadingView.alpha == SHOWING_LOADING_FLOAT) {
                binding.mainLoadingView.visibility = View.GONE
                binding.mainLoadingView.alpha = NOT_SHOWING_LOADING_FLOAT
                binding.mainMotionLayout.isInteractionEnabled = true
                binding.mainMotionLayout.isEnabled = true
                binding.mainGpsFix.clearAnimation()
            }
        }
    }

    // 프로그래스 보이기
    private fun showProgressBar() { setProgressVisibility(true) }

    // 프로그래스 숨기기
    private fun hideProgressBar() { setProgressVisibility(false) }

    @SuppressLint("NotifyDataSetChanged")
    private fun initializing() {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(displayMetrics)

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
        binding.nestedSubAirFrame.isClickable = false

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
            if (dailyWeatherList.size >= getHourCountToTomorrow()) {
                tomorrowSection.visibility = VISIBLE
                binding.mainDailyWeatherRv.scrollToPosition(getHourCountToTomorrow())
                binding.mainDailyWeatherRv.post { scrollSmoothFirst(getHourCountToTomorrow()) }
            } else tomorrowSection.visibility = GONE
        }

        // 모레 클릭
        afterTomorrowSection.setOnClickListener {
            if (dailyWeatherList.size >= getHourCountToTomorrow() + 24) {
                afterTomorrowSection.visibility = VISIBLE
                binding.mainDailyWeatherRv.post {
                    scrollSmoothFirst(getHourCountToTomorrow() + 24)
                }
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
                    1 -> {
                        setSectionTextColor(
                            todaySection,
                            tomorrowSection,
                            afterTomorrowSection
                        )
                    }
                    2 -> {
                        when (layoutManager.findFirstVisibleItemPosition()) {
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
                            else -> {}
                        }
                    }
                    3 -> {
                        when (layoutManager.findFirstVisibleItemPosition()) {
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
                    else -> {}
                }
            }
        })

        // 실시간 공기질 리스트 클릭
        airQAdapter.setOnItemClickListener(object : AirQTitleAdapter.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                try {
                    val model = airQList[position]

                    applyAirQView(model.name, model.nameKR, model.value, model.unit)

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
        binding.nestedAirHelp.setImageDrawable(getR(R.drawable.help))

        // 외부 공기질 도움말 클릭
        binding.nestedAirHelp.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (binding.nestedAirHelpPopup.alpha == 0f) {
                    val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
                    fadeIn.duration = 400
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

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 뒤로가기 한번 클릭 시 토스트
        if (!isBackPressed) {
            ToastUtils(this).showMessage(getString(R.string.back_press), 2)
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
        val old = getTopicNotification(this)
        fcm.renewTopic(old, newAddr)
        SetAppInfo.setTopicNotification(this, newAddr)
    }

    // 현재 옵저버가 없으면 생성
    private fun getDataObservers() {
        if (fetch.hasActiveObservers()) {
            destroyObserver()
            binding.mainSwipeLayout.isRefreshing = false
        }
        TimberUtil().w("lifecycle_test", "메인 옵저버 생성")
        applyGetDataViewModel()
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    fun applyGetDataViewModel() {
        try {
            fetch.observe(this) { entireData ->
                entireData?.let { eData ->
                    binding.mainSwipeLayout.isRefreshing = false

                    when (eData) {
                        is BaseRepository.ApiState.Success -> {
                            handleApiSuccess(eData.data)
                        }
                        is BaseRepository.ApiState.Error -> {
                            handleApiError(eData.errorMessage)
                        }
                        is BaseRepository.ApiState.Loading -> {
                            showProgressBar()
                        }
                    }
                } ?: run {
                    hideProgressBar()
                    if (!isDataResponse) {
                        hideAllViews(error = ERROR_NULL_DATA)
                    }
                }
            }
        } catch (e: IOException) {
            binding.mainSwipeLayout.isRefreshing = false
            handleApiError(ERROR_API_PROTOCOL)
            hideProgressBar()
        }
    }

    // API 통신이 성공일 때 처리
    private fun handleApiSuccess(result: ApiModel.GetEntireData) {
        try {
            val metaAddr = result.meta.address ?: "주소 호출 에러"
            CoroutineScope(Dispatchers.IO).launch {
                reNewTopicInMain(metaAddr)
            }
            runOnUiThread {
                binding.mainGpsFix.clearAnimation()
                binding.mainDailyWeatherRv.scrollToPosition(0)
                binding.mainWarningVp.currentItem = 0
                showAllViews()
                updateUIWithData(result)
                RDBLogcat.writeGpsHistory(
                    this,
                    isSearched = false,
                    gpsValue = metaAddr,
                    responseData = "${getUserLastAddress(this)},${result}"
                )

                isDataResponse = true
                // 메인 날씨 텍스트 세팅
                val skyText = if (currentIsAfterRealtime(
                        result.current.currentTime,
                        result.realtime[0].forecast
                    )
                ) {
                    translateSky(
                        this, applySkyText(
                            this,
                            result.current.rainType,
                            result.realtime[0].sky, result.thunder
                        )
                    )
                } else {
                    translateSky(
                        this, applySkyText(
                            this,
                            result.realtime[0].rainType,
                            result.realtime[0].sky, result.thunder
                        )
                    )
                }

                binding.mainSkyText.text = skyText
                // 날씨에 따라 배경화면 변경
                applyWindowBackground(currentSun, skyText)
                hideProgressBar()
                if (!isInAppMsgShow) {
                    CoroutineScope(Dispatchers.IO).launch {
                        startInAppMsg()
                    }
                    isInAppMsgShow = true
                }
            }
        } catch (e: Exception) {
            handleApiError(e.localizedMessage ?: e.stackTraceToString())
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
            try {
                RDBLogcat.writeErrorANR("handleApiError", "handleApiError cause $errorMessage")
            } catch (e: Exception) { e.printStackTrace() }
            if (GetLocation(this).isNetWorkConnected()) {
                hideAllViews(error = errorMessage)
            } else { if (errorMessage != "text") { hideAllViews(error = ERROR_NETWORK) } }
        }
    }

    // 결과에서 얻은 데이터로 UI 요소를 업데이트
    private fun updateUIWithData(result: ApiModel.GetEntireData) {
        currentSun =
            GetAppInfo.getCurrentSun(result.sun?.sunrise ?: "0600", result.sun?.sunset ?: "1900")
        val lunar = result.lunar?.date ?: -1
        val realtimeFirst = result.realtime[0]
        val isAfterRealtime =
            currentIsAfterRealtime(result.current.currentTime, realtimeFirst.forecast)

        updateWeatherItems(result)
        updateAirQualityData(result.quality)
        updateUVData(result.uv)
        updateSunTimes(result.sun, result.sun_tomorrow)
        updateCurrentTemperature(result.yesterday, result.current, result.realtime)
        updateWeatherWarnings(result.summary)
        updateTerm24(result.term24)

        // 메인 날씨 아이콘 세팅
        binding.mainSkyImg.setImageDrawable(
            applySkyImg(
                this,
                if (isAfterRealtime) result.current.rainType else realtimeFirst.rainType,
                realtimeFirst.sky, result.thunder,
                isLarge = true, isNight = getIsNight(currentSun),
                lunar
            )
        )
    }

    @SuppressLint("SetTextI18n")
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
            week.wf4Am, week.wf5Am, week.wf6Am
        )
        // 주간 오후 날씨
        val wfMax = listOf(
            week.wf0Pm, week.wf1Pm, week.wf2Pm, week.wf3Pm,
            week.wf4Pm, week.wf5Pm, week.wf6Pm
        )
        // 주간 최저 기온
        val taMin = listOf(
            week.taMin0, week.taMin1, week.taMin2, week.taMin3,
            week.taMin4, week.taMin5, week.taMin6
        )
        // 주간 최고 기온
        val taMax = listOf(
            week.taMax0, week.taMax1, week.taMax2, week.taMax3, week.taMax4,
            week.taMax5, week.taMax6
        )

        // 최저/최대 기온 적용
        result.today?.let { mToday ->
            binding.mainMinValue.text = "${filteringNullData(mToday.min)}˚"
            binding.mainMaxValue.text = "${filteringNullData(mToday.max)}˚"
            binding.mainMinMaxValueC.text =
                "${filteringNullData(mToday.min)}˚/${filteringNullData(mToday.max)}˚"
        }

        // 시간별 날씨 아이템 추가
        val sun = result.sun
        val current = result.current
        val thunder = result.thunder
        val lunar = result.lunar?.date ?: -1

        result.realtime.forEachIndexed { realtimeIndex, dailyIndex ->
            val forecastToday = LocalDateTime.parse(dailyIndex.forecast)
            val dailyTime = millsToString(parseLocalDateTimeToLong(forecastToday), "HHmm")
            val sunset = sun?.sunset ?: "0600"
            val sunrise = sun?.sunrise ?: "1900"
            val entireSun =
                if (getEntireSun(sunrise, sunset) == 0) 1 else getEntireSun(sunrise, sunset)
            val dailySunProgress =
                100 * (parseTimeToMinutes(dailyTime) - parseTimeToMinutes(sunrise)) / entireSun
            val isNight = getIsNight(dailySunProgress)

            if (realtimeIndex == 0) {
                val isAfterRealtime =
                    currentIsAfterRealtime(current.currentTime, dailyIndex.forecast)
                val skyImg = applySkyImg(
                    this, if (isAfterRealtime) current.rainType else dailyIndex.rainType,
                    if (isAfterRealtime) dailyIndex.sky else dailyIndex.sky, thunder,
                    isLarge = false, isNight = isNight, lunar = lunar
                )!!
                val temperature =
                    if (isAfterRealtime) "${current.temperature.roundToInt()}˚" else "${dailyIndex.temp.roundToInt()}˚"
                val rainType = if (isAfterRealtime) current.rainType else dailyIndex.rainType
                val rainP = if (isAfterRealtime) dailyIndex.rainP ?: 0.0 else dailyIndex.rainP!!

                addDailyWeatherItem(
                    "${forecastToday.hour}${getString(R.string.hour)}",
                    skyImg,
                    temperature,
                    dailyIndex.forecast!!,
                    isRainyDay(rainType),
                    rainP
                )
            } else {
                val skyImg = applySkyImg(
                    this, dailyIndex.rainType, dailyIndex.sky, thunder,
                    isLarge = false, isNight = isNight, lunar = lunar
                )!!
                val temperature = "${dailyIndex.temp.roundToInt()}˚"
                val rainType = dailyIndex.rainType
                val rainP = dailyIndex.rainP!!

                addDailyWeatherItem(
                    "${forecastToday.hour}${getString(R.string.hour)}",
                    skyImg,
                    temperature,
                    dailyIndex.forecast ?: "",
                    isRainyDay(rainType),
                    rainP
                )
            }
        }

        val dateNow: LocalDateTime = LocalDateTime.now()

        // 주간별 날씨 아이템 추가
        repeat(7) {
            try {
                val formedDate = dateNow.plusDays(it.toLong())
                val date: String = when (it) {
                    0 -> getString(R.string.today)
                    1 -> getString(R.string.tomorrow)
                    else -> {
                        "${
                            parseDayOfWeekToKorean(
                                this,
                                dateNow.dayOfWeek.value + it
                            )
                        }${getString(R.string.date)}"
                    }
                }

                addWeeklyWeatherItem(
                    date,
                    dateAppendZero(formedDate),
                    getSkyImgSmall(this, wfMin[it]!!, isNight = false)!!,
                    getSkyImgSmall(this, wfMax[it]!!, isNight = true)!!,
                    "${taMin[it]!!.roundToInt()}˚",
                    "${taMax[it]!!.roundToInt()}˚"
                )
            } catch (e: Exception) {
                RDBLogcat.writeErrorANR(
                    RDBLogcat.DATA_CALL_ERROR,
                    "updateWeatherItems is ${e.stackTraceToString()}"
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAirQualityData(air: ApiModel.AirQualityData) {
        // 대기 질 데이터 업데이트
        airQList.clear()

        val pm25 = (air.pm25Value ?: air.pm25Value24 ?: 0.0)
        val pm10 = (air.pm10Value ?: air.pm10Value24 ?: 0.0)

        updateAirQData(
            PM2p5_INDEX, getString(R.string.pm2_5_full), "PM2.5",
            "㎍/㎥", pm25.toInt().toString()
        )
        updateAirQData(
            PM10_INDEX, getString(R.string.pm10_full), "PM10",
            "㎍/㎥", pm10.toInt().toString()
        )
        updateAirQData(
            CO_INDEX, getString(R.string.co_full), "CO",
            "ppm", air.coValue.toString()
        )
        updateAirQData(
            SO2_INDEX, getString(R.string.so2_full), "SO2",
            "ppm", air.so2Value.toString()
        )
        updateAirQData(
            NO2_INDEX, getString(R.string.no2_full), "NO2",
            "ppm", air.no2Value.toString()
        )
        updateAirQData(
            O3_INDEX, getString(R.string.o3_full), "O3",
            "ppm", air.o3Value.toString()
        )

        applyAirQView(
            "PM2.5", getString(R.string.pm2_5_full),
            pm25.toInt().toString(), "㎍/m3"
        )

        airQList[PM2p5_INDEX].isSelect = true   // 초기 데이터 = 초미세먼지

        changeStrokeColor(
            binding.subAirPM25,
            getDataColor(
                this,
                convertValueToGrade("PM2.5", pm25.toDouble())
            )
        )

        changeStrokeColor(
            binding.subAirPM10,
            getDataColor(
                this,
                convertValueToGrade("PM10", pm10)
            )
        )

        binding.subAirPM25.text = "${getString(R.string.pm2_5_full)}   ${pm25.toInt()}"
        binding.subAirPM10.text = "${getString(R.string.pm10_full)}   ${(pm10.toInt())}"
    }

    @SuppressLint("SetTextI18n")
    private fun updateUVData(uv: ApiModel.UV?) {
        // 자외선 데이터 업데이트
        // UV 값이 없으면 카드 없앰
        uv?.let {
            it.flag?.let { mFlag ->
                it.value?.let { mValue ->
                    if (mFlag != "null") {
                        binding.mainUVBox.visibility = VISIBLE
                        applyUvResponseItem(mFlag)   // 자외선 단계별 대응요령 추가
                        setUvBackgroundColor(
                            this, mFlag, binding.mainUVLegendCardView
                        ) // UV 범주 색상 변경
                        binding.mainUvValue.text =
                            "${translateUV(this, mFlag)}\n$mValue"
                    }
                } ?: run { binding.mainUVBox.visibility = GONE }
            } ?: run { binding.mainUVBox.visibility = GONE }
        } ?: run { binding.mainUVBox.visibility = GONE }
    }

    private fun updateSunTimes(
        sun: ApiModel.SunData?,
        sunTomorrow: ApiModel.SunTomorrow?
    ) {
        // 일출 및 일몰 시간 업데이트
        sunPb.animate(currentSun)

        // 일출/일몰 세팅
        val sbRise = StringBuffer().append(sun?.sunrise).insert(2, ":")
        val sbSet = StringBuffer().append(sun?.sunset).insert(2, ":")
        sunTomorrow?.let { tom ->
            val sbRiseTom =
                StringBuffer().append(tom.sunrise).insert(2, ":")
            val sbSetTom = StringBuffer().append(tom.sunset).insert(2, ":")
            binding.mainSunRiseTom.text = sbRiseTom
            binding.mainSunSetTom.text = sbSetTom
        }

        binding.mainSunRiseTime.text = sbRise
        binding.mainSunSetTime.text = sbSet

    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentTemperature(
        yesterdayTemp: ApiModel.YesterdayTemp,
        current: ApiModel.Current,
        realtime: List<ApiModel.RealTimeData>
    ) {
        // 현재 온도 적용
        val real0 = realtime[0]
        val currentTemperature = current.temperature.toString()
        val currentHumidity = modifyCurrentHumid(current.humidity, real0.humid)
        val currentWindSpeed = modifyCurrentWindSpeed(current.windSpeed, real0.windSpeed)
        val currentRainP = (real0.rainP ?: 0.0).roundToInt()

        binding.mainLiveTempValue.text = currentTemperature
        binding.mainLiveTempUnit.text = "˚"
        binding.mainLiveTempValueC.text = "$currentTemperature˚"

        // 서브 날씨(습도,바람,강수확률) 적용
        binding.subAirHumid.fetchData(
            "${currentHumidity.roundToInt()}%", R.drawable.ico_main_humidity, null
        )
        binding.subAirWind.fetchData(
            "${currentWindSpeed.roundToInt()}m/s",
            R.drawable.ico_main_wind,
            current.vector ?: real0.vector
        )
        binding.subAirRainP.fetchData("${currentRainP}%", R.drawable.ico_main_rain, null)

        // 온도 비교 업데이트
        getCompareTempText(
            yesterdayTemp.temp ?: real0.temp,
            current.temperature,
            binding.mainCompareTempTv
        )

        // 체감 온도 업데이트
        binding.mainSensTitle.text = getString(R.string.sens_temp)

        val sensibleTemp = try {
            DataTypeParser.parseDoubleToDecimal(
                SensibleTempFormula().getSensibleTemp(
                    ta = current.temperature,
                    rh = currentHumidity,
                    v = currentWindSpeed
                ), digit = 1
            )
        } catch (e: Exception) {
            DataTypeParser.parseDoubleToDecimal(
                SensibleTempFormula().getSensibleTemp(
                    ta = real0.temp,
                    rh = currentHumidity,
                    v = currentWindSpeed
                ), 1
            )
        }

        binding.mainSensValue.text = "$sensibleTemp˚"
        binding.mainSensValueC.text = "$sensibleTemp˚"
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

        if (getUserLocation(this) == LANG_EN) {
            binding.mainWarningBox.setBackgroundColor(getColor(android.R.color.transparent))
        } else {
            // 기상특보 세팅
            summary?.let { sList ->
                val filteredList = sList.map { summary ->
                    summary.replace("○", "").replace("\n", "").trim()
                }
                warningList.addAll(filteredList)

                if (warningList.isNotEmpty()) {
                    if (!isWarned) {
                        warningSlideAuto()
                        isWarned = true
                    }
                }
            }
        }
    }

    private fun updateTerm24(terms24: String?) {
        // 24절기 세팅
        terms24?.let { term ->
            val bundle = Term24Class().getTerms24Bundle(term)
            bundle?.let { b ->
                binding.nestedTerms24Box.visibility = VISIBLE
                binding.mainTermsTitle.text = b.getString("title")
                binding.mainTermsDate.text = b.getString("date")
                binding.mainTermsExplain.text = b.getString("explain")
            } ?: run { binding.nestedTerms24Box.visibility = GONE }
        } ?: run { binding.nestedTerms24Box.visibility = GONE }
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
        val isNight = getIsNight(progress)
        if (isNight && (sky == getString(R.string.sky_sunny) || sky == getString(R.string.sky_sunny_cloudy))) {
            changeBackgroundResource(R.drawable.main_bg_night)
            binding.mainSkyStarImg.setImageDrawable(getR(R.drawable.bg_nightsky))
        } else {
            binding.mainSkyStarImg.setImageDrawable(null)
            val backgroundResource = when (sky) {
                getString(R.string.sky_sunny), getString(R.string.sky_sunny_cloudy) ->
                    R.drawable.main_bg_clear
                getString(R.string.sky_sunny_cloudy_rainy_snowy), getString(R.string.sky_cloudy_rainy_snowy),
                getString(R.string.sky_rainy_snowy), getString(R.string.sky_sunny_cloudy_shower),
                getString(R.string.sky_cloudy_rainy), getString(R.string.sky_sunny_cloudy_rainy),
                getString(R.string.sky_cloudy_shower), getString(R.string.sky_shower), getString(R.string.sky_rainy),
                getString(R.string.sky_cloudy) -> R.drawable.main_bg_cloudy
                getString(R.string.sky_sunny_cloudy_snowy), getString(R.string.sky_snowy), getString(
                    R.string.sky_cloudy_snowy
                )
                -> R.drawable.main_bg_snow
                else -> R.drawable.main_bg_snow
            }
            changeBackgroundResource(backgroundResource)
        }
    }

    private fun changeBackgroundResource(id: Int?) {
        id?.let {
            window.setBackgroundDrawableResource(it)
        } ?: window.setBackgroundDrawableResource(R.color.theme_view_color)

        changeTextColorStyle(id ?:  R.color.theme_view_color)
    }

    // 필드값이 없을 때 -100 출력 됨
    private fun filteringNullData(data: Double?): String {
        return if (data != -100.0 && data != 100.0) data?.roundToInt().toString() else ""
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
        val isKorea = resources.configuration.locales[0] == Locale.KOREA

        val comparisonText = when {
            compared == null -> ""
            compared < 0 -> {
                if (isKorea) "어제보다 ${compared.absoluteValue}˚ 낮아요"
                else "${compared.absoluteValue}˚ lower than yesterday"
            }
            compared == 0.0 -> getString(R.string.similar_temp)
            else -> {
                if (isKorea) "어제보다 ${compared.absoluteValue}˚ 높아요"
                else "${compared.absoluteValue}˚ upper than yesterday"
            }
        }

        tv.visibility = if (comparisonText.isNotEmpty()) View.VISIBLE else GONE
        tv.text = comparisonText
    }

    // 에러 코드에 따라 에러 메시지 설정
    private fun setErrorMessage(error: String): String {
        return when (error) {
            ERROR_API_PROTOCOL, ERROR_SERVER_CONNECTING, ERROR_NULL_DATA -> getString(R.string.api_call_error)
            ERROR_NOT_SERVICED_LOCATION -> getString(R.string.not_serviced_location_error)
            ERROR_TIMEOUT -> getString(R.string.timeout_error)
            ERROR_NETWORK -> getString(R.string.network_error)
            ERROR_GET_LOCATION_FAILED -> getString(R.string.address_call_error)
            ERROR_GPS_CONNECTED -> getString(R.string.gps_call_error)
            ERROR_GET_DATA -> getString(R.string.data_call_error)
            else -> {
                RDBLogcat.writeErrorANR(
                    getString(R.string.unknown_error),
                    "setErrorMessage is $error"
                )
                getString(R.string.unknown_error)
            }
        }
    }

    // 에러 버튼에 클릭 리스너 설정
    private fun setOnClickListenerForErrorButton(error: String) {
        val buttonTextResId = when (error) {
            ERROR_NOT_SERVICED_LOCATION -> R.string.register_new_address
            ERROR_GPS_CONNECTED -> R.string.enable_gps
            else -> R.string.renew_data
        }

        binding.mainErrorRenewBtn.apply {
            text = getString(buttonTextResId)
            setOnClickListener {
                mVib()
                when (error) {
                    ERROR_NOT_SERVICED_LOCATION -> {
                        val bottomSheet = SearchDialog(
                            this@MainActivity,
                            1,
                            supportFragmentManager,
                            BottomSheetDialogFragment().tag
                        )
                        bottomSheet.show(1)
                    }
                    ERROR_GPS_CONNECTED -> GetLocation(this@MainActivity).requestSystemGPSEnable()
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
        val isNight = isThemeNight(this@MainActivity)
        val backgroundResourceId = if (isNight) R.color.black else R.color.white

        runOnUiThread {
            error?.let { e ->
                setOnClickListenerForErrorButton(e)
                updateViewsForError(e)
            }
            binding.mainSkyImg.apply {
                changeBackgroundResource(backgroundResourceId)
                setImageDrawable(getR(if (isNight) R.drawable.ico_error_b else R.drawable.ico_error_w))
            }
        }
    }

    // 통신에 성공할 경우 레이아웃 처리
    private fun showAllViews() {
        setVisibilityForViews(VISIBLE, null)
    }

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

            val clickableChangeArray = arrayOf(
                binding.mainSideMenuIv,
                binding.mainShareIv,
                binding.mainAddAddress,
                binding.mainGpsFix
            )

            clickableChangeArray.forEach {
                it.isEnabled = visibility == VISIBLE
            }

            // 숨김
            if (visibility == GONE) {
                if (error == ERROR_NETWORK ||
                    error == ERROR_GET_DATA
                ) {
                    setDrawable(binding.mainAddAddress, null)
                    setDrawable(binding.mainSideMenuIv, null)
                } else {
                    tintImageDrawables()
                }

                clearTextViews(textViewArray)

                setDrawable(binding.mainGpsFix, null)
                setDrawable(binding.mainMotionSLideImg, null)
                setDrawable(binding.mainGpsFix, null)
                binding.mainShareIv.isEnabled = false
                binding.mainSwipeLayout.isEnabled = false

                binding.mainMotionLayout.apply {
                    transitionToStart()
                    Thread.sleep(100)
                    isInteractionEnabled = false // 모션 레이아웃의 스와이프를 막음
                }

                binding.mainMotionSlideGuide.apply {
                    text = getString(R.string.error_guide)
                    setTextColor(getC(R.color.theme_text_color))
                }
                applyBackground(binding.mainWarningBox, null)
                applyBackground(binding.nestedSubAirFrame, null)

                changeStrokeColor(binding.subAirPM10, getColor(android.R.color.transparent))
                changeStrokeColor(binding.subAirPM25, getColor(android.R.color.transparent))

                updateErrorViewsVisibility(GONE)
            } else {
                updateViewsForVisibleState()
            }
        }
    }

    private fun updateViewsForVisibleState() {
        // 보임
        binding.mainSensTitle.text = getString(R.string.sens_temp)
        binding.subAirHumid.getTitle().text = getString(R.string.humidity)
        binding.subAirWind.getTitle().text = getString(R.string.wind)
        binding.subAirRainP.getTitle().text = getString(R.string.rainPer)
        applyBackground(binding.mainWarningBox, R.drawable.report_frame_bg)
        binding.mainMotionSlideGuide.text = getString(R.string.slide_more)
        binding.mainMinTitle.text = getString(R.string.min)
        binding.mainMaxTitle.text = getString(R.string.max)
        binding.mainShareIv.isEnabled = true

        setDrawable(binding.mainGpsFix, R.drawable.gps_fix)
        setDrawable(binding.mainMotionSLideImg, R.drawable.drop_down_bottom)
        setDrawable(binding.mainAddAddress, R.drawable.search)
        setDrawable(binding.mainSideMenuIv, R.drawable.ico_hamb_w)
        setDrawable(binding.mainTopEye, R.drawable.ico_eye_beta_bk)

        // 원래 상태로 복구하기 위해 제약 조건 변경
        binding.mainMotionLayout.isInteractionEnabled = true

        updateErrorViewsVisibility(VISIBLE)
    }

    // 이미지뷰의 이미지를 설정
    private fun setDrawable(imageView: ImageView, drawableResId: Int?) {
        drawableResId?.let {
            imageView.setImageDrawable(getR(it))
        } ?: imageView.setImageDrawable(null)
    }

    // 이미지뷰의 이미지 틴트 적용
    private fun tintImageDrawables() {
        binding.mainAddAddress.imageTintList =
            ColorStateList.valueOf(getColor(R.color.theme_text_color))
        binding.mainSideMenuIv.imageTintList =
            ColorStateList.valueOf(getColor(R.color.theme_text_color))
        binding.mainAddAddress.isEnabled = true
        binding.mainSideMenuIv.isEnabled = true
    }

    // 텍스트뷰의 텍스트 지우기
    private fun clearTextViews(textViews: List<TextView>) {
        textViews.forEach { it.text = "" }
    }

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
        binding.mainSkyStarImg.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainShareIv.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainSkyText.alpha = if (visibility == VISIBLE) 1f else 0f
        binding.mainErrorRenewBtn.isClickable = visibility == GONE
        binding.mainTopEye.alpha = if (visibility == VISIBLE) 1f else 0f
    }

    // 현재 지역의 날씨 데이터 뷰모델 생성 및 호출
    private fun loadCurrentViewModelData(lat: Double, lng: Double, addr: String?) {
        getDataObservers()
        getDataViewModel.loadData(lat, lng, addr)
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
            false, position, nameKR, name, unit, value, grade
        )

        this.airQList.add(position, item)
    }

    // 외부 공기질 데이터 갱신
    private fun updateAirQData(
        position: Int, nameKR: String, name: String, unit: String, value: String
    ) {
        val item = AdapterModel.AirQTitleItem(
            false, position, nameKR, name, unit, value, convertValueToGrade(name, value.toDouble())
        )
        if (!airQList.contains(item)) addAirQItem(position, nameKR, name, unit, value, item.grade)
    }

    // 현재 위치 정보로 DB 갱신
    private fun updateCurrentAddress(mLat: Double, mLng: Double, mAddr: String?) {
        GetLocation(this@MainActivity).updateDatabaseWithLocationData(mLat, mLng, mAddr)
    }

    // 자외선 범주 아이템 추가
    private fun addUvLegendItem(index: Int, value: String, color: Int, grade: String) {
        val item = AdapterModel.UVLegendItem(value, color, grade)
        uvLegendList.add(index, item)
    }

    // 자외선 단계별 대응요령 아이템 추가
    private fun addUvResponseItem(text: Array<String>) {
        val itemList = text.map { AdapterModel.UVResponseItem(it) }
        uvResponseList.addAll(itemList)
    }

    // 자외선 지수에 따른 대처요령 불러오기
    private fun getUvArray(grade: String): Array<String> {
        return when (grade) {
            "위험" -> resources.getStringArray(R.array.uv_caution)
            "매우높음" -> resources.getStringArray(R.array.uv_very_high)
            "높음" -> resources.getStringArray(R.array.uv_high)
            "보통" -> resources.getStringArray(R.array.uv_normal)
            "낮음" -> resources.getStringArray(R.array.uv_low)
            else -> resources.getStringArray(R.array.uv_none)
        }
    }

    // 자외선 단계별 대응요령 필터링
    @SuppressLint("NotifyDataSetChanged")
    private fun applyUvResponseItem(grade: String) {
        uvResponseList.clear()
        val cautionArray = getUvArray(grade)
        addUvResponseItem(cautionArray)
    }

    // 메인화면 배경에 따라 텍스트의 색상을 변경
    @SuppressLint("UseCompatTextViewDrawableApis", "NotifyDataSetChanged")
    private fun changeTextColorStyle(bg: Int) {
        val changeColorTextViews = listOf(
            binding.mainLiveTempValue,
            binding.mainLiveTempUnit,
            binding.mainCompareTempTv,
            binding.mainTopBarGpsTitle,
            binding.mainMotionSlideGuide,
            binding.mainGpsTitleTv,
            binding.mainSensTitle,
            binding.mainSensValue,
            binding.mainLiveTempTitleC,
            binding.subAirWind.getTitle(),
            binding.subAirRainP.getTitle(),
            binding.subAirHumid.getTitle(),
            binding.subAirWind.getValue(),
            binding.subAirRainP.getValue(),
            binding.subAirHumid.getValue(),
            binding.mainLiveTempValueC,
            binding.mainSensTitleC,
            binding.mainSensValueC,
            binding.mainMinMaxTitleC,
            binding.mainMinMaxValueC,
            binding.mainDailyWeatherTitle,
            binding.mainWeeklyWeatherTitle,
            binding.nestedAirTitle,
            binding.mainUvTitle,
            binding.mainSunRiseTitle,
            binding.mainSunSetTitle,
            binding.mainSunRiseTime,
            binding.mainSunSetTime,
            binding.mainSunTomTitle,
            binding.mainUvCollapsedTitle,
            binding.nestedAirTitleEn,
            binding.dailySectionTomorrow,
            binding.dailySectionAfterTomorrow,
            binding.mainSunSetTom,
            binding.mainSunRiseTom,
            binding.mainTermsTitle,
            binding.mainTermsExplain,
            binding.mainSkyText
        )
        val changeColorSubTextViews = listOf(
            binding.mainLicenseText, binding.nestedAirTitleKr, binding.nestedAirUnit,
        )
        val changeTintLineViews = listOf(
            binding.nestedAirLine, binding.mainSunLine, binding.mainUvLine
        )
        val changeTintImageViews = listOf(
            binding.mainSideMenuIv, binding.mainAddAddress,
            binding.mainGpsFix, binding.mainMotionSLideImg,
            binding.mainShareIv, binding.nestedAirHelp
        )
        val changeBoxViews = listOf(
            binding.mainWarningBox, binding.nestedSubAirFrame,
            binding.nestedDailyBox, binding.nestedWeeklyBox,
            binding.nestedAirBox, binding.mainUVBox, binding.mainSunBox,
            binding.nestedTerms24Box
        )

        // 리소스 색상 가져오기
        val colorWhite = getColor(R.color.white)
        val colorBlack = getColor(R.color.main_black)
        val colorSubWhite = getColor(R.color.sub_white)
        val colorSubBlack = getColor(R.color.sub_black)

        // 글자색 변경 함수
        fun changeTextColor(color: Int, subColor: Int, isWhite: Boolean) {
            // 일괄 처리를 통한 업데이트 지연
            val delayMillis = 100L // 원하는 지연 시간(ms) 설정
            CoroutineScope(Dispatchers.Main).launch {
                changeColorTextViews.forEach { it.setTextColor(color) }
                changeColorSubTextViews.forEach { it.setTextColor(subColor) }
                changeTintLineViews.forEach { it.setBackgroundColor(color) }
                changeTintImageViews.forEach { it.imageTintList = ColorStateList.valueOf(color) }
                delay(delayMillis)
            }

            binding.dailySectionTomorrow.setTextColor(subColor)
            binding.mainTopBarGpsTitle.compoundDrawablesRelative[0].mutate()
                .setTint(color)
            binding.subAirWind.getValue().compoundDrawableTintList =
                ColorStateList.valueOf(color)

            dailyWeatherAdapter.setIsWhite(isWhite)
            weeklyWeatherAdapter.setIsWhite(isWhite)
            uvLegendAdapter.setIsWhite(isWhite)
            uvResponseAdapter.setIsWhite(isWhite)
            airQAdapter.setIsWhite(isWhite)
            airQAdapter.notifyDataSetChanged()
            uvResponseAdapter.notifyDataSetChanged()
            uvLegendAdapter.notifyDataSetChanged()
            weeklyWeatherAdapter.notifyDataSetChanged()
            dailyWeatherAdapter.notifyDataSetChanged()
            dailyWeatherAdapter.submitList(dailyWeatherList)
            warningViewPagerAdapter.changeTextColor(color)
            reportViewPagerItem.addAll(warningList)
            warningViewPagerAdapter.notifyDataSetChanged()
        }

        // 글자색 변경: 텍스트 및 리소스 색상 사용
        fun changeTextToWhite() {
            changeTextColor(colorWhite, colorSubWhite, true)
            changeBoxViews.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10000000"))
            }
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        fun changeTextToBlack() {
            changeTextColor(colorBlack, colorSubBlack, false)
            changeBoxViews.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#40FFFFFF"))
            }
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        // 주어진 조건에 따라 텍스트 색상 변경
        when (bg) {
            R.drawable.main_bg_clear, R.drawable.main_bg_snow -> {
                changeTextToBlack()
                setDrawable(binding.mainTopEye, R.drawable.ico_eye_beta_bk)
            }
            R.drawable.main_bg_night, R.drawable.main_bg_cloudy -> {
                changeTextToWhite()
                setDrawable(binding.mainTopEye, R.drawable.ico_eye_beta_w)
            }
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
        val handler = Handler(Looper.getMainLooper())
        if (warningList.size > 1) {
            vp.currentItem = if (vp.currentItem + 1 < warningList.size) vp.currentItem + 1 else 0
            handler.postDelayed({
                warningSlideAuto()
            }, 5000)
        }
    }

    // 현재 위치가 한국인지 아닌지 구분
    private fun isKorea(lat: Double, lng: Double): Boolean {
        return lng in 125.0..132.0 && lat in 33.0..39.0
    }

    private fun checkLocationAvailability() {
        val locationClass = GetLocation(this)
        if (!locationClass.isNetWorkConnected()) hideAllViews(ERROR_NETWORK)
        else if (locationClass.isGPSConnected()) requestLocationWithGPS()
        else if (locationClass.isNetworkProviderConnected()) requestLocationWithNetworkProvider()
        else hideAllViews(ERROR_GPS_CONNECTED)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationWithGPS() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val onSuccess: (Location?) -> Unit = { location ->
            location?.let { loc ->
                val lat = loc.latitude
                val lng = loc.longitude
                if (isKorea(lat, lng)) {
                    val addr = GetLocation(this@MainActivity).getAddress(lat, lng)
                    processAddress(lat, lng, addr)
                } else {
                    ToastUtils(this).showMessage(getString(R.string.error_not_service_locale))
                    loadSavedViewModelData("서울특별시")
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
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val db = GpsRepository(this@MainActivity).findByName(CURRENT_GPS_ID)
                val lat = db.lat
                val lng = db.lng
                if (lat != null && lng != null) {
                    val mLat = lat.toDouble()
                    val mLng = lat.toDouble()
                    val addr = GetLocation(this@MainActivity).getAddress(mLat, mLng)
                    if (isKorea(mLat, mLng)) {
                        ToastUtils(this@MainActivity)
                            .showMessage(getString(R.string.last_location_call_msg), 1)
                        processAddress(mLat, mLng, addr)
                    } else hideAllViews(ERROR_NOT_SERVICED_LOCATION)
                }
            }
        } catch (e: NumberFormatException) {
            handleLocationFailure(e.stackTraceToString())
            hideAllViews(ERROR_GET_LOCATION_FAILED)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationWithNetworkProvider() {
        CoroutineScope(Dispatchers.Default).launch {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location?.let { loc ->
                loadCurrentViewModelData(loc.latitude, loc.longitude, null)
            }
        }
    }

    private fun handleLocationFailure(errorMessage: String?) {
        hideProgressBar()
        val msg = errorMessage ?: "errorMsg is NULL"
        RDBLogcat.writeErrorANR(
            ERROR_LOCATION_FAILED,
            "handleLocationFailure cause $msg"
        )
    }

    private fun processAddress(lat: Double, lng: Double, address: String?) {
        address?.let { addr ->
            // 주소 정보를 저장하고 업데이트
            CoroutineScope(Dispatchers.IO).launch {
                setUserLastAddr(this@MainActivity, addr)
                updateCurrentAddress(lat, lng, addr)
            }

            // 주소 문자열에서 불필요한 부분을 제거
            val cleanedAddr = addr
                .replaceFirst(" ", "")
                .replace(getString(R.string.korea), "")
                .replace("null", "")

            // 주소 포맷을 정의하거나 필요한 경우 다른 변환 작업을 수행
            val formattedAddr =
                if (getUserLocation(this@MainActivity) == LANG_EN)
                    cleanedAddr.replace("South Korea", "")
                else AddressFromRegex(cleanedAddr).getAddress()

            loadCurrentViewModelData(lat, lng, formattedAddr)
            updateAddress(formattedAddr)
        }
    }

    private fun getR(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, id, null)
    }

    private fun getC(id: Int): Int {
        return ResourcesCompat.getColor(resources, id, null)
    }

    // 뷰 백그라운드 적용
    private fun <T> applyBackground(view: T, res: Int?) {
        res?.let {
            (view as View).background = getR(it)
        } ?: apply {
            (view as View).background = null
        }
    }

    fun recreateMainActivity(addrKr: String?, addrEn: String?) {
        addrKr?.let {
            loadSavedAddr(addrKr,addrEn)
        }
    }
}