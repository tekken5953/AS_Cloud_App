package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.dao.IgnoredKeyFile.notiEnable
import app.airsignal.weather.dao.IgnoredKeyFile.notiSound
import app.airsignal.weather.dao.IgnoredKeyFile.notiVibrate
import app.airsignal.weather.dao.StaticDataObject.LANG_EN
import app.airsignal.weather.dao.StaticDataObject.LANG_KR
import app.airsignal.weather.dao.StaticDataObject.LANG_SYS
import app.airsignal.weather.dao.StaticDataObject.TEXT_SCALE_BIG
import app.airsignal.weather.dao.StaticDataObject.TEXT_SCALE_DEFAULT
import app.airsignal.weather.dao.StaticDataObject.TEXT_SCALE_SMALL
import app.airsignal.weather.dao.StaticDataObject.THEME_DARK
import app.airsignal.weather.dao.StaticDataObject.THEME_LIGHT
import app.airsignal.weather.databinding.ActivitySettingBinding
import app.airsignal.weather.firebase.db.RDBLogcat.LOGIN_GOOGLE
import app.airsignal.weather.firebase.db.RDBLogcat.LOGIN_KAKAO
import app.airsignal.weather.firebase.db.RDBLogcat.LOGIN_NAVER
import app.airsignal.weather.firebase.db.RDBLogcat.LOGIN_PHONE
import app.airsignal.weather.login.GoogleLogin
import app.airsignal.weather.login.KakaoLogin
import app.airsignal.weather.login.NaverLogin
import app.airsignal.weather.repo.BaseRepository
import app.airsignal.weather.util.*
import app.airsignal.weather.util.`object`.DataTypeParser.findCharacterIndex
import app.airsignal.weather.util.`object`.GetAppInfo.getUserEmail
import app.airsignal.weather.util.`object`.GetAppInfo.getUserFontScale
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLocation
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLoginPlatform
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiEnable
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiSound
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiVibrate
import app.airsignal.weather.util.`object`.GetAppInfo.getUserTheme
import app.airsignal.weather.util.`object`.GetAppInfo.isPermedBackLoc
import app.airsignal.weather.util.`object`.GetSystemInfo.getApplicationVersionCode
import app.airsignal.weather.util.`object`.GetSystemInfo.getApplicationVersionName
import app.airsignal.weather.util.`object`.GetSystemInfo.goToPlayStore
import app.airsignal.weather.util.`object`.SetAppInfo.removeAllKeys
import app.airsignal.weather.util.`object`.SetAppInfo.setInitBackLocPermission
import app.airsignal.weather.util.`object`.SetAppInfo.setUserFontScale
import app.airsignal.weather.util.`object`.SetAppInfo.setUserLocation
import app.airsignal.weather.util.`object`.SetAppInfo.setUserNoti
import app.airsignal.weather.util.`object`.SetAppInfo.setUserTheme
import app.airsignal.weather.util.`object`.SetSystemInfo.setStatusBar
import app.airsignal.weather.view.BackLocCheckDialog
import app.airsignal.weather.view.ShowDialogClass
import app.airsignal.weather.view.custom_view.CustomerServiceView
import app.airsignal.weather.view.custom_view.SnackBarUtils
import app.airsignal.weather.vmodel.GetAppVersionViewModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class SettingActivity
    : BaseActivity<ActivitySettingBinding>() {
    override val resID: Int get() = R.layout.activity_setting

    private val faqItem = arrayListOf<AdapterModel.FaqItem>()
    private val noticeItem = arrayListOf<AdapterModel.NoticeItem>()
    private var isInit = true
    private val appVersionViewModel by viewModel<GetAppVersionViewModel>()
    private var isBackAllow = false

    override fun onResume() {
        super.onResume()

        applyDeviceTheme()

        applyUserEmail()

        applyUserLanguage()

        applyFontScale()
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()

        setStatusBar(this)

        if (isInit) {
            isInit = false
        }

        val lastLogin = applyLastLogin()

        // 뒤로가기 버튼 클릭
        binding.settingBack.setOnClickListener { goMain() }

        // 로그아웃 버튼 클릭
        binding.settingLogOut.setOnClickListener {
            if (binding.settingLogOut.text == getString(R.string.setting_logout)) {
                val builder = Dialog(this)
                val view = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_alert_double_btn, null)
                builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
                builder.setContentView(view)
                builder.create()

                val cancel = view.findViewById<AppCompatButton>(R.id.alertDoubleCancelBtn)
                val apply = view.findViewById<AppCompatButton>(R.id.alertDoubleApplyBtn)
                val title = view.findViewById<TextView>(R.id.alertDoubleTitle)
                title.text = getString(R.string.want_logout)
                cancel.text = getString(R.string.cancel)
                apply.text = getString(R.string.setting_logout)

                apply.setOnClickListener {
                    builder.dismiss()
                    CoroutineScope(Dispatchers.IO).launch {
                        when (lastLogin) { // 로그인 했던 플랫폼에 따라서 로그아웃 로직 호출
                            LOGIN_KAKAO -> {
//                                KakaoLogin(this@SettingActivity).logout(email)
                                KakaoLogin(this@SettingActivity).disconnectFromKakao(binding.settingPb)
                            }
                            LOGIN_NAVER -> {
//                                NaverLogin(this@SettingActivity).logout()
                                NaverLogin(this@SettingActivity).disconnectFromNaver(binding.settingPb)
                            }
                            LOGIN_GOOGLE -> {
                                GoogleLogin(this@SettingActivity).logout(binding.settingPb)
                            }
                            else -> {}
                        }
                        delay(100)

                        removeAllKeys(this@SettingActivity)
                    }
                }

                cancel.setOnClickListener {
                    builder.dismiss()
                }

                builder.show()
            } else if (binding.settingLogOut.text == getString(R.string.login_title)) {
                EnterPageUtil(this).toLogin()
            }
        }

        // 테마 설정 클릭
        binding.settingSystemTheme.setOnClickListener {
            // 레이아웃 뷰 생성
            val themeView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_change_theme, null)
            val systemTheme: RadioButton = themeView.findViewById(R.id.themeSystemRB)
            val lightTheme: RadioButton = themeView.findViewById(R.id.themeLightRB)
            val darkTheme: RadioButton = themeView.findViewById(R.id.themeDarkRB)
            val radioGroup: RadioGroup = themeView.findViewById(R.id.changeThemeRadioGroup)
            val cancel: ImageView = themeView.findViewById(R.id.changeThemeBack)

            ShowDialogClass(this)
                .setBackPressed(themeView.findViewById(R.id.changeThemeBack))
                .show(themeView, true)

            // 현재 저장된 테마에 따라서 라디오버튼 체크
            when (getUserTheme(this)) {
                THEME_DARK -> {
                    radioGroup.check(darkTheme.id)
                    changeCheckIcon(darkTheme, lightTheme, systemTheme)
                }
                THEME_LIGHT -> {
                    radioGroup.check(lightTheme.id)
                    changeCheckIcon(lightTheme, systemTheme, darkTheme)
                }
                else -> {
                    radioGroup.check(systemTheme.id)
                    changeCheckIcon(systemTheme, lightTheme, darkTheme)
                }
            }

            // 라디오 버튼 클릭 시 이벤트처리
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                // 아이디에 따라 분할
                when (checkedId) {
                    // 시스템 설정
                    systemTheme.id -> {
                        changedThemeRadio(
                            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                            dbData = LANG_SYS,
                            radioGroup = radioGroup,
                            radioButton = systemTheme,
                            cancel
                        )
                        changeCheckIcon(systemTheme, lightTheme, darkTheme)
                    }
                    // 라이트 모드
                    lightTheme.id -> {
                        changedThemeRadio(
                            mode = AppCompatDelegate.MODE_NIGHT_NO,
                            dbData = THEME_LIGHT,
                            radioGroup = radioGroup,
                            radioButton = lightTheme,
                            cancel
                        )
                        changeCheckIcon(lightTheme, systemTheme, darkTheme)
                    }
                    // 다크 모드
                    darkTheme.id -> {
                        changedThemeRadio(
                            mode = AppCompatDelegate.MODE_NIGHT_YES,
                            dbData = THEME_DARK,
                            radioGroup = radioGroup,
                            radioButton = darkTheme,
                            cancel
                        )
                        changeCheckIcon(darkTheme, systemTheme, lightTheme)
                    }
                }
            }
        }

        // 언어 설정 클릭
        binding.settingSystemLang.setOnClickListener {
            // 뷰 레이아웃 생성
            val langView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_change_language, null)
            val systemLang: RadioButton = langView.findViewById(R.id.systemLangRb)
            val koreanLang: RadioButton = langView.findViewById(R.id.koreanRB)
            val englishLang: RadioButton = langView.findViewById(R.id.englishRB)
            val radioGroup: RadioGroup = langView.findViewById(R.id.changeLangRadioGroup)
            val cancelBtn: ImageView = langView.findViewById(R.id.changeLangBack)

            ShowDialogClass(this)
                .setBackPressed(cancelBtn)
                .show(langView, true)

            // 기존에 저장 된 언어로 라디오 버튼 체크
            when (getUserLocation(this)) {
                LANG_KR -> {
                    radioGroup.check(koreanLang.id)
                    changeCheckIcon(koreanLang, englishLang, systemLang)
                }
                LANG_EN -> {
                    radioGroup.check(englishLang.id)
                    changeCheckIcon(englishLang, koreanLang, systemLang)
                }
                else -> {
                    radioGroup.check(systemLang.id)
                    changeCheckIcon(systemLang, englishLang, koreanLang)
                }
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    systemLang.id -> {
                        changedLangRadio(
                            lang = LANG_SYS,
                            radioGroup = radioGroup,
                            radioButton = systemLang,
                            cancelBtn
                        )
                        changeCheckIcon(systemLang, koreanLang, englishLang)
                    }
                    koreanLang.id -> {
                        changedLangRadio(
                            lang = LANG_KR,
                            radioGroup = radioGroup,
                            radioButton = koreanLang,
                            cancelBtn
                        )
                        changeCheckIcon(koreanLang, systemLang, englishLang)
                    }
                    englishLang.id -> {
                        changedLangRadio(
                            lang = LANG_EN,
                            radioGroup = radioGroup,
                            radioButton = englishLang,
                            cancelBtn
                        )
                        changeCheckIcon(englishLang, koreanLang, systemLang)
                    }
                }
            }
        }

        binding.settingSystemFont.setOnClickListener {
            val scaleView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_change_font_scale, null)
            val small = scaleView.findViewById<RadioButton>(R.id.scaleSmallRB)
            val default = scaleView.findViewById<RadioButton>(R.id.scaleDefaultRB)
            val big = scaleView.findViewById<RadioButton>(R.id.scaleBigRB)
            val back = scaleView.findViewById<ImageView>(R.id.changeScaleBack)
            val rg = scaleView.findViewById<RadioGroup>(R.id.changeScaleRadioGroup)

            ShowDialogClass(this)
                .setBackPressed(back)
                .show(scaleView, true)

            // 현재 저장된 텍스트 크기에 따라서 라디오버튼 체크
            when (getUserFontScale(this)) {
                TEXT_SCALE_SMALL -> {
                    rg.check(small.id)
                }
                TEXT_SCALE_BIG -> {
                    rg.check(big.id)
                }
                else -> {
                    rg.check(default.id)
                }
            }

            // 설정 변경시
            rg.setOnCheckedChangeListener { radioGroup, i ->
                when (i) {
                    small.id -> {
                        setUserFontScale(this, TEXT_SCALE_SMALL)
                        radioGroup.check(small.id)
                        this.goMain()
                    }
                    big.id -> {
                        setUserFontScale(this, TEXT_SCALE_BIG)
                        radioGroup.check(big.id)
                        this.goMain()
                    }
                    default.id -> {
                        setUserFontScale(this, TEXT_SCALE_DEFAULT)
                        radioGroup.check(default.id)
                        this.goMain()
                    }
                }
            }
        }

        // 앱 정보 클릭
        binding.settingAppInfo.setOnClickListener {
            applyAppVersionResult()
            appVersionViewModel.loadDataResult()
        }

        // 알림 클릭
        binding.settingNotificationText.setOnClickListener {
            val notificationView: View =
                LayoutInflater.from(this).inflate(
                    R.layout.dialog_notification_setting,
                    null
                )

            val notiVibrateTr: TableRow = notificationView.findViewById(R.id.notiVibrateView)
            val notiSoundTr: TableRow = notificationView.findViewById(R.id.notiSoundView)
            val notiBackTr: TableRow = notificationView.findViewById(R.id.notiBackView)
            val notiSettingTitle: TextView = notificationView.findViewById(R.id.notiSettingTitle)
            val notiBackTitle: TextView = notificationView.findViewById(R.id.notiBackTitle)
            val notiSettingSwitch: SwitchCompat =
                notificationView.findViewById(R.id.notiSettingSwitch)
            val notiVibrateSwitch: SwitchCompat =
                notificationView.findViewById(R.id.notiVibrateSwitch)
            val notiBackContent: TextView = notificationView.findViewById(R.id.notiBackContent)
            val notiSoundSwitch: SwitchCompat = notificationView.findViewById(R.id.notiSoundSwitch)
            val notiLine2: View = notificationView.findViewById(R.id.notificationLine2)
            val notiLine3: View = notificationView.findViewById(R.id.notificationLine3)

            // 알림 미허용시 다른 아이템 숨김
            fun setVisibility(isChecked: Boolean) {
                if (isChecked) {
                    notiVibrateTr.visibility = View.VISIBLE
                    notiSoundTr.visibility = View.VISIBLE
                    notiLine2.visibility = View.VISIBLE
                    notiLine3.visibility = View.VISIBLE
                } else {
                    notiVibrateTr.visibility = View.GONE
                    notiSoundTr.visibility = View.GONE
                    notiLine2.visibility = View.GONE
                    notiLine3.visibility = View.GONE
                }
            }

            // 백그라운드 요청에 따른 적용
            fun applyBack(isChecked: Boolean) {
                if (isChecked) {
                    setInitBackLocPermission(this,true)
                    notiBackTr.visibility = View.VISIBLE
                    // 29 이상
                    if (VERSION.SDK_INT >= 29) {
                        // 백그라운드 허용 여부
                        isBackAllow = RequestPermissionsUtil(this).isBackgroundRequestLocation()
                        notiBackTitle.text = getString(R.string.perm_self_msg)
                        if (isBackAllow) {
                            notiBackContent.text = getString(R.string.allowed)
                        } else {
                            notiBackContent.text = getString(R.string.do_allow)
                        }
                    }
                    // 29 이하
                    else {
                        notiBackTitle.text = getString(R.string.perm_back_setting)
                        if (isPermedBackLoc(this)) {
                            notiBackContent.text = getString(R.string.background_location_active)
                        } else {
                            notiBackContent.text =
                                getString(R.string.background_location_not_active)
                        }
                    }

                    setNightAlertsSpan(notiBackTitle)
                    setNightAlertsSpan(notiBackContent)

                    notiBackTr.setOnClickListener {
                        if (notiBackContent.text.toString() == getString(R.string.do_allow) ||
                            notiBackContent.text.toString() == getString(R.string.background_location_not_active) ||
                            notiBackContent.text.toString() == getString(R.string.background_location_active)
                        ) {
                            BackLocCheckDialog(
                                this,
                                supportFragmentManager,
                                BottomSheetDialogFragment().tag
                            ).show()
                        }
                    }
                } else {
                    notiBackTr.visibility = View.GONE
                }
            }

            setNightAlertsSpan(notiSettingTitle)
            // 개인 설정에 따른 스위치 변환
            notiSettingSwitch.isChecked = getUserNotiEnable(this)
            notiVibrateSwitch.isChecked = getUserNotiVibrate(this)
            notiSoundSwitch.isChecked = getUserNotiSound(this)

            setVisibility(notiSettingSwitch.isChecked)
            applyBack(notiSettingSwitch.isChecked)

            // 알림 설정 스위치 변화
            notiSettingSwitch.setOnCheckedChangeListener { _, isChecked ->
                setUserNoti(this, notiEnable, isChecked)
                showSnackBar(notificationView, isChecked)
                setVisibility(isChecked)
                applyBack(isChecked)
            }
            // 진동 설정 스위치 변화
            notiVibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
                setUserNoti(this, notiVibrate, isChecked)
                showSnackBar(notificationView, isChecked)
            }
            // 소리 설정 스위치 변화
            notiSoundSwitch.setOnCheckedChangeListener { _, isChecked ->
                setUserNoti(this, notiSound, isChecked)
                showSnackBar(notificationView, isChecked)
            }

            ShowDialogClass(this)
                .setBackPressed(notificationView.findViewById(R.id.notificationBack))
                .show(notificationView, true)
        }
    }

    // 알림 커스텀 스낵바 세팅
    private fun showSnackBar(view: View, isAllow: Boolean) {
        val alertOn = ContextCompat.getDrawable(this, R.drawable.alert_on)!!
        val alertOff = ContextCompat.getDrawable(this, R.drawable.alert_off)!!
        alertOn.setTint(getColor(R.color.theme_view_color))
        alertOff.setTint(getColor(R.color.theme_view_color))
        if (isAllow) {
            if (!isInit) {
                SnackBarUtils.make(
                    view,
                    getString(R.string.allowed_noti), alertOn
                ).show()
            }
        } else {
            if (!isInit) {
                SnackBarUtils.make(
                    view,
                    getString(R.string.denied_noti), alertOff
                ).show()
            }
        }
    }

    // 알림 텍스트 색상 설정
    private fun setNightAlertsSpan(textView: TextView) {
        val span = SpannableStringBuilder(textView.text)
        val formatText = textView.text.split(System.lineSeparator())
        formatText.forEach {
            span.setSpan(
                ForegroundColorSpan(getColor(R.color.theme_sub_color)),
                it.length, span.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 크기변경
            span.setSpan(
                AbsoluteSizeSpan(37),
                it.length, span.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (textView.text.contains(getString(R.string.always_allowed))) {
                try {
                    span.setSpan(
                        ForegroundColorSpan(getColor(R.color.main_blue_color)),
                        findCharacterIndex(textView.text as String, '\n'),
                        findCharacterIndex(textView.text as String, '을'),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }

            if (textView.text == getString(R.string.allowed) || textView.text == getString(R.string.background_location_active)) {
                textView.setTextColor(getColor(R.color.main_blue_color))
            }
        }

        textView.text = span
    }

    // 테마 적용
    private fun applyDeviceTheme() {
        // 설정 페이지 테마 항목이름 바꾸기
        when (getUserTheme(this)) {
            THEME_DARK -> {
                binding.settingSystemTheme.fetchData(getString(R.string.theme_dark))
            }
            THEME_LIGHT -> {
                binding.settingSystemTheme.fetchData(getString(R.string.theme_light))
            }
            else -> {
                binding.settingSystemTheme.fetchData(getString(R.string.theme_system))
            }
        }
    }

    // 앱 버전 불러오기
    @SuppressLint("SetTextI18n")
    private fun applyAppVersionResult() {
        val viewAppInfo: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_app_info, null)

        val appInfoVersionValue: TextView = viewAppInfo.findViewById(R.id.appInfoVersionValue)
        val appInfoIsRecent: TextView = viewAppInfo.findViewById(R.id.appInfoIsRecent)
        val appInfoDownBtn: Button = viewAppInfo.findViewById(R.id.appInfoDownBtn)
        val appInfoPB: ProgressBar = viewAppInfo.findViewById(R.id.appInfoPB)
        val appInfoLicense: TextView = viewAppInfo.findViewById(R.id.appInfoLicense)
        val appInfoTermsService: TextView = viewAppInfo.findViewById(R.id.appInfoTermsOfService)
        val appInfoCustomerService: TextView =
            viewAppInfo.findViewById(R.id.appInfoCustomerService)
        val appInfoDataUsage: TextView = viewAppInfo.findViewById(R.id.appInfoDataUsage)


        // 뷰모델 데이터 호출
        appVersionViewModel.fetchData().observe(this) { result ->
            result?.let { ver ->
                when (ver) {
                    is BaseRepository.ApiState.Success -> {
                        val data = ver.data
                        val versionName = getApplicationVersionName(this)
                        val versionCode = getApplicationVersionCode(this)
                        Logger.t("testtest")
                            .i("version App : ${versionName}.${versionCode} version Server : ${data.name}.${data.code}")

                        appInfoVersionValue.text = "${versionName}.${versionCode}"
                        if ("${data.name}.${data.code}" == "${versionName}.${versionCode}") {
                            appInfoIsRecent.text = getString(R.string.last_software)
                            appInfoIsRecent.setTextColor(getColor(R.color.sub_gray_color))
                            appInfoDownBtn.visibility = View.GONE
                            appInfoVersionValue.visibility = View.VISIBLE
                        } else {
                            appInfoIsRecent.text =
                                getString(R.string.not_latest_version)
                            appInfoIsRecent.setTextColor(getColor(R.color.main_blue_color))
                            appInfoDownBtn.visibility = View.VISIBLE
                            appInfoVersionValue.visibility = View.GONE
                        }
                    }
                    is BaseRepository.ApiState.Error -> {
                        Toast.makeText(
                            this@SettingActivity,
                            getString(R.string.fail_to_get_version),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        }

        // 새로운 버전 다운로드 실행
        appInfoDownBtn.setOnClickListener {
            goToPlayStore(this)
        }

        // 오픈소스 라이센스 클릭
        appInfoLicense.setOnClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.list_of_open_source))
        }

        // 이용약관 클릭
        appInfoTermsService.setOnClickListener {
            val intent = Intent(this@SettingActivity, WebURLActivity::class.java)
            intent.putExtra("sort", "termsOfService")
            startActivity(intent)
        }

        // 개인 정보 처리 방침 클릭
        appInfoDataUsage.setOnClickListener {
            val intent = Intent(this@SettingActivity, WebURLActivity::class.java)
            intent.putExtra("sort", "dataUsage")
            startActivity(intent)
        }

        // 고객 센터 클릭
        appInfoCustomerService.setOnClickListener {
            val customerView: View = LayoutInflater.from(this)
                .inflate(R.layout.dialog_customer_service, null)

            val customerCall: CustomerServiceView = customerView.findViewById(R.id.customerCall)
            val customerHomePage: CustomerServiceView =
                customerView.findViewById(R.id.customerHomePage)
            val customerEmail: CustomerServiceView = customerView.findViewById(R.id.customerEmail)

            customerCall.fetchData(R.drawable.ico_cs_phone)
            customerEmail.fetchData(R.drawable.ico_cs_mail)
            customerHomePage.fetchData(R.drawable.ico_cs_web)

            ShowDialogClass(this@SettingActivity)
                .setBackPressed(customerView.findViewById(R.id.customerBack))
                .show(customerView, true)
        }

        appInfoPB.visibility = View.GONE
        appInfoVersionValue.visibility = View.VISIBLE

        ShowDialogClass(this)
            .setBackPressed(viewAppInfo.findViewById(R.id.appInfoBack))
            .show(viewAppInfo, true)
    }

    // 유저 이메일에 따른 로그인 여부 적용
    private fun applyUserEmail() {
        if (getUserEmail(this) != "") {
            binding.settingUserEmail.text = getUserEmail(this)
            binding.settingUserIcon.visibility = View.VISIBLE
        } else {
            binding.settingUserEmail.text = getString(R.string.please_login)
            binding.settingUserIcon.visibility = View.GONE
        }
    }

    // 유저 언어 설정 적용
    private fun applyUserLanguage() {
        // 설정 페이지 언어 항목이름 바꾸기
        when (getUserLocation(this)) {
            LANG_EN -> {
                binding.settingSystemLang.fetchData(getString(R.string.english))
            }
            LANG_KR -> {
                binding.settingSystemLang.fetchData(getString(R.string.korean))
            }
            else -> {
                binding.settingSystemLang.fetchData(getString(R.string.system_lang))
            }
        }
    }

    // 유저 폰트 크기 설정 적용
    private fun applyFontScale() {
        // 설정 페이지 폰트크기 항목이름 바꾸기
        when (getUserFontScale(this)) {
            "small" -> {
                binding.settingSystemFont.fetchData(getString(R.string.font_small))
            }
            "big" -> {
                binding.settingSystemFont.fetchData(getString(R.string.font_large))
            }
            else -> {
                binding.settingSystemFont.fetchData(getString(R.string.font_normal))
            }
        }
    }

    // 마지막 로그인 플랫폼 종류
    private fun applyLastLogin(): String {
        val lastLogin = getUserLoginPlatform(this)
        if (lastLogin != "") {
            binding.settingLogOut.text = getString(R.string.setting_logout)
        } else {
            binding.settingLogOut.text = getString(R.string.login_title)
        }

        // 로그인 플랫폼 아이콘 설정
        when (lastLogin) {
            LOGIN_GOOGLE -> {
                setImageDrawable(binding.settingUserIcon, R.drawable.google_icon)
            }
            LOGIN_KAKAO -> {
                setImageDrawable(binding.settingUserIcon, R.drawable.kakao_icon)
            }
            LOGIN_NAVER -> {
                setImageDrawable(binding.settingUserIcon, R.drawable.naver_icon)
            }
            LOGIN_PHONE -> {
                setImageDrawable(binding.settingUserIcon, R.drawable.phone_icon)
            }
        }

        return lastLogin
    }

    // 이미지 드로어블 할당
    private fun setImageDrawable(imageView: ImageView, src: Int) {
        imageView.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                src, null
            )
        )
    }

    // 메인 액티비티로 이동
    private fun goMain() {
        val intent = Intent(this@SettingActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    // 라디오 버튼 DrawableEnd Tint 변경
    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun changeCheckIcon(rbOn: RadioButton, rbOff1: RadioButton, rbOff2: RadioButton) {
        rbOn.compoundDrawableTintList =
            ColorStateList.valueOf(getColor(R.color.main_blue_color))
        rbOff1.compoundDrawableTintList =
            ColorStateList.valueOf(getColor(android.R.color.transparent))
        rbOff2.compoundDrawableTintList =
            ColorStateList.valueOf(getColor(android.R.color.transparent))
    }

    // 언어 라디오 버튼 클릭 시 이벤트 처리
    private fun changedLangRadio(
        lang: String,
        radioGroup: RadioGroup,
        radioButton: RadioButton,
        cancel: ImageView
    ) {
        if (getUserLocation(this) != lang) { // 현재 설정된 언어인지 필터링
            cancel.isEnabled = false
            setUserLocation(this, lang)  // 다른 언어라면 db 값 변경
            radioGroup.check(radioButton.id) // 라디오 버튼 체크
            Thread.sleep(100)
            saveLanguageChange() // 언어 설정 변경 후 어플리케이션 재시작
        }
    }

    // 테마 라디오 버튼 클릭 시 이벤트 처리
    private fun changedThemeRadio(
        mode: Int,
        dbData: String,
        radioGroup: RadioGroup,
        radioButton: RadioButton,
        cancel: ImageView
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            // 테마모드 변경
            AppCompatDelegate.setDefaultNightMode(mode)
            cancel.isEnabled = false
            // DB에 바뀐 정보 저장
            setUserTheme(this@SettingActivity, dbData)
            // 라디오 버튼 체크
            radioGroup.check(radioButton.id)
            delay(300)
            val intent = Intent(this@SettingActivity, SplashActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    // 언어 설정 변경 후 어플리케이션 재시작
    private fun saveLanguageChange() {
        val builder = Dialog(this)
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_alert_single_btn, null)
        builder.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(view)
            setCancelable(false)
        }

        builder.create()

        val title = view.findViewById<TextView>(R.id.alertSingleTitle)
        val apply = view.findViewById<AppCompatButton>(R.id.alertSingleApplyBtn)

        title.text = getString(R.string.save_change)
        apply.text = getString(R.string.ok)
        apply.setOnClickListener {
            RefreshUtils(this).refreshApplication()
        }
        builder.show()
    }

    // 자주묻는질문 아이템 추가하기
    private fun addFaqItem(title: String, content: String) {
        val item = AdapterModel.FaqItem(title, content)
        faqItem.add(item)
    }

    // 공지사항 아이템 추가하기
    private fun addNoticeItem(
        created: String,
        modified: String,
        title: String,
        content: String
    ) {
        val item = AdapterModel.NoticeItem(created, modified, title, content)
        noticeItem.add(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        @Suppress("DEPRECATION")
        super.onBackPressed()
        goMain()
    }
}