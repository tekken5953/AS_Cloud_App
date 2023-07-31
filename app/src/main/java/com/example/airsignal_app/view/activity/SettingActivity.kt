package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.FaqAdapter
import com.example.airsignal_app.adapter.NoticeAdapter
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.dao.IgnoredKeyFile.notiEvent
import com.example.airsignal_app.dao.IgnoredKeyFile.notiNight
import com.example.airsignal_app.dao.IgnoredKeyFile.notiPM
import com.example.airsignal_app.dao.StaticDataObject.LANG_EN
import com.example.airsignal_app.dao.StaticDataObject.LANG_KR
import com.example.airsignal_app.dao.StaticDataObject.LANG_SYS
import com.example.airsignal_app.databinding.ActivitySettingBinding
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_GOOGLE
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_KAKAO
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_NAVER
import com.example.airsignal_app.firebase.db.RDBLogcat.LOGIN_PHONE
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.login.PhoneLogin
import com.example.airsignal_app.repo.BaseRepository
import com.example.airsignal_app.retrofit.HttpClient
import com.example.airsignal_app.util.*
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserFontScale
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLocation
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserTheme
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.util.`object`.GetSystemInfo.getApplicationVersion
import com.example.airsignal_app.util.`object`.GetSystemInfo.goToPlayStore
import com.example.airsignal_app.util.`object`.SetAppInfo.removeAllKeys
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserFontScale
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserLocation
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserNoti
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserTheme
import com.example.airsignal_app.view.ShowDialogClass
import com.example.airsignal_app.vmodel.GetAppVersionViewModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*

class SettingActivity
    : BaseActivity<ActivitySettingBinding>() {
    override val resID: Int get() = R.layout.activity_setting

    private val faqItem = arrayListOf<AdapterModel.FaqItem>()
    private val noticeItem = arrayListOf<AdapterModel.NoticeItem>()
    private var isInit = true
    private val appVersionViewModel by viewModel<GetAppVersionViewModel>()

    override fun onResume() {
        super.onResume()

        applyDeviceTheme()

        applyUserEmail()

        applyUserLanguage()

        applyFontScale()

        applyNotification()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()

        window.statusBarColor = getColor(R.color.theme_view_color)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // 로그인 시 저장된 핸드폰 번호
        val email = getUserEmail(this)

        if (isInit) {
            isInit = false
        }

        val lastLogin = applyLastLogin()

        // 뒤로가기 버튼 클릭
        binding.settingBack.setOnClickListener {
//            VibrateUtil(this).make(20)
            onBackPressedDispatcher.onBackPressed()
        }

        // 로그아웃 버튼 클릭
        binding.settingLogOut.setOnClickListener {
            if (binding.settingLogOut.text == getString(R.string.setting_logout)) {
                val builder = Dialog(this)
                val view = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_alert_double_btn,null)
                builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.setContentView(view)
                builder.create()

                val cancel = view.findViewById<AppCompatButton>(R.id.alertDoubleCancelBtn)
                val apply = view.findViewById<AppCompatButton>(R.id.alertDoubleApplyBtn)
                val title = view.findViewById<TextView>(R.id.alertDoubleTitle)
                title.text = "로그아웃 하시겠습니까?"
                cancel.text = getString(R.string.cancel)
                apply.text = getString(R.string.setting_logout)

                apply.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        when (lastLogin) { // 로그인 했던 플랫폼에 따라서 로그아웃 로직 호출
                            "kakao" -> {
                                KakaoLogin(this@SettingActivity).logout(email)
                            }
                            "naver" -> {
                                NaverLogin(this@SettingActivity).logout()
                            }
                            "google" -> {
                                GoogleLogin(this@SettingActivity).logout()
                            }
                            "phone" -> {
                                PhoneLogin(this@SettingActivity, null, null)
                            }
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
                .setBackPressRefresh(themeView.findViewById(R.id.changeThemeBack))
                .show(themeView, true)

            // 현재 저장된 테마에 따라서 라디오버튼 체크
            when (getUserTheme(this)) {
                "dark" -> {
                    radioGroup.check(darkTheme.id)
                    changeCheckIcon(darkTheme, lightTheme, systemTheme)
                }
                "light" -> {
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
                            dbData = "light",
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
                            dbData = "dark",
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
                .setBackPressRefresh(back)
                .show(scaleView, true)

            // 현재 저장된 텍스트 크기에 따라서 라디오버튼 체크
            when (getUserFontScale(this)) {
                "small" -> {
                    rg.check(small.id)
                }
                "big" -> {
                    rg.check(big.id)
                }
                else -> {
                    rg.check(default.id)
                }
            }

            rg.setOnCheckedChangeListener { radioGroup, i ->
                when (i) {
                    small.id -> {
                        setUserFontScale(this, "small")
                        radioGroup.check(small.id)
                        this.goMain()
                    }
                    big.id -> {
                        setUserFontScale(this, "big")
                        radioGroup.check(big.id)
                        this.goMain()
                    }
                    default.id -> {
                        setUserFontScale(this, "default")
                        radioGroup.check(default.id)
                        this.goMain()
                    }
                }
            }
        }

        val detailView: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_detail, null)
        val detailDate: TextView = detailView.findViewById(R.id.detailNoticeDate)
        val detailTitle: TextView = detailView.findViewById(R.id.detailTitle)
        val detailContent: TextView = detailView.findViewById(R.id.detailContent)
        val detailHeadLine: TextView = detailView.findViewById(R.id.detailHeadLine)

        // 공지사항 클릭
        binding.settingNotice.setOnClickListener {
            val noticeMainView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_notice, null)
            val noticeAdapter = NoticeAdapter(this, noticeItem)
            val recyclerView: RecyclerView = noticeMainView.findViewById(R.id.noticeRv)
            val noticeTitle: TextView = noticeMainView.findViewById(R.id.noticeTitle)
            val nullText = noticeMainView.findViewById<TextView>(R.id.noticeNullText)

            recyclerView.adapter = noticeAdapter
            noticeItem.clear()

            CoroutineScope(Dispatchers.IO).launch {
                HttpClient
                    .getInstance(false)
                    .setClientBuilder()
                    .mMyAPIImpl
                    .notice.enqueue(object : Callback<List<AdapterModel.NoticeItem>> {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onResponse(
                            call: Call<List<AdapterModel.NoticeItem>>,
                            response: Response<List<AdapterModel.NoticeItem>>
                        ) {
                            try {
                                val list = response.body()!!
                                list.forEach {
                                    addNoticeItem(convertDateFormat(it.created),
                                        convertDateFormat(it.modified),
                                        it.title,
                                        it.content)
                                }

                                noticeAdapter.notifyDataSetChanged()
                                if (list.isEmpty()) {
                                    nullText.visibility = View.VISIBLE
                                } else {
                                    nullText.visibility = View.GONE
                                }
                            } catch(e: Exception) {
                                nullText.visibility = View.VISIBLE
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(
                            call: Call<List<AdapterModel.NoticeItem>>,
                            t: Throwable
                        ) {
                            nullText.visibility = View.VISIBLE
                            Toast.makeText(this@SettingActivity,
                                "공지사항을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()

                            t.printStackTrace()
                        }
                    })
            }

            ShowDialogClass(this)
                .setBackPressed(noticeMainView.findViewById(R.id.noticeBack))
                .show(noticeMainView, true)

            noticeAdapter.setOnItemClickListener(object : NoticeAdapter.OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    detailDate.text = noticeItem[position].created
                    detailDate.visibility = View.VISIBLE
                    detailTitle.text = noticeTitle.text.toString()
                    detailContent.text = noticeItem[position].content
                    detailHeadLine.text = noticeItem[position].title
                    ShowDialogClass(this@SettingActivity)
                        .setBackPressed(detailView.findViewById(R.id.detailBack))
                        .show(detailView, true)
                }
            })
        }

        // 자주묻는질문 클릭
        binding.settingFaq.setOnClickListener {
            val faqMainView: View = LayoutInflater.from(this).inflate(R.layout.dialog_faq, null)
            val faqAdapter = FaqAdapter(this, faqItem)
            val recyclerView = faqMainView.findViewById<RecyclerView>(R.id.faqRv)
            val faqTitle: TextView = faqMainView.findViewById(R.id.faqTitle)
            val faqNullText: TextView = faqMainView.findViewById(R.id.faqNullText)
            faqItem.clear()
            recyclerView.adapter = faqAdapter

            CoroutineScope(Dispatchers.IO).launch {
                HttpClient.getInstance(false)
                    .setClientBuilder()
                    .mMyAPIImpl.faq.enqueue(object : Callback<List<AdapterModel.FaqItem>>{
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onResponse(
                            call: Call<List<AdapterModel.FaqItem>>,
                            response: Response<List<AdapterModel.FaqItem>>
                        ) {
                            try {
                                val list = response.body()!!
                                list.forEach {
                                    addFaqItem(it.title,it.content)
                                }

                                faqAdapter.notifyDataSetChanged()

                                if (list.isEmpty()) {
                                    faqNullText.visibility = View.VISIBLE
                                } else {
                                    faqNullText.visibility = View.GONE
                                }
                            } catch (e: Exception) {
                                faqNullText.visibility = View.VISIBLE
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(
                            call: Call<List<AdapterModel.FaqItem>>,
                            t: Throwable
                        ) {
                            faqNullText.visibility = View.VISIBLE
                            Toast.makeText(this@SettingActivity,
                                "자주 묻는 질문을 불러오는데 실패했습니다",
                                Toast.LENGTH_SHORT).show()

                            t.printStackTrace()
                        }
                    })
            }

            ShowDialogClass(this)
                .setBackPressed(faqMainView.findViewById(R.id.faqBack))
                .show(faqMainView, true)

            faqAdapter.setOnItemClickListener(object : FaqAdapter.OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    detailDate.visibility = View.GONE
                    detailTitle.text = faqTitle.text.toString()
                    detailContent.text = faqItem[position].content
                    detailHeadLine.text = faqItem[position].title
                    ShowDialogClass(this@SettingActivity)
                        .setBackPressed(detailView.findViewById(R.id.detailBack))
                        .show(detailView, true)
                }
            })
        }

        // 앱 정보 클릭
        binding.settingAppInfo.setOnClickListener {
            applyAppVersionResult()
            appVersionViewModel.loadDataResult()

        }

        binding.settingNotificationText.setOnClickListener {
            val notificationView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_notification_setting,
                    null)

            ShowDialogClass(this)
                .setBackPressed(notificationView.findViewById(R.id.notificationBack))
                .show(notificationView, true)
        }
    }

    private fun applyDeviceTheme() {
        @Suppress("DEPRECATION")
        if (GetSystemInfo.isThemeNight(this)) {
            window.statusBarColor = Color.BLACK
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            window.statusBarColor = Color.WHITE
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // 설정 페이지 테마 항목이름 바꾸기
        when (getUserTheme(this)) {
            "dark" -> {
                binding.settingSystemTheme.fetchData(getString(R.string.theme_dark))
            }
            "light" -> {
                binding.settingSystemTheme.fetchData(getString(R.string.theme_light))
            }
            else -> {
                binding.settingSystemTheme.fetchData(getString(R.string.theme_system))
            }
        }
    }

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

        appVersionViewModel.fetchData().observe(this) { result ->
            result?.let { ver ->
                when (ver) {
                    is BaseRepository.ApiState.Success -> {
                        val data = ver.data
                        val versionInfo = getApplicationVersion(this)

                        appInfoVersionValue.text = versionInfo
                        if (data.version == versionInfo) {
                            appInfoIsRecent.text = "최신 소프트웨어입니다."
                            appInfoIsRecent.setTextColor(getColor(R.color.sub_gray_color))
                            appInfoDownBtn.visibility = View.GONE
                            appInfoVersionValue.visibility = View.VISIBLE
                        } else {
                            appInfoIsRecent.text =
                                "최신 버전이 있습니다"
                            appInfoIsRecent.setTextColor(getColor(R.color.main_blue_color))
                            appInfoDownBtn.visibility = View.VISIBLE
                            appInfoVersionValue.visibility = View.GONE
                        }
                    }
                    is BaseRepository.ApiState.Error -> {
                        Toast.makeText(
                            this@SettingActivity,
                            "앱 정보를 불러오는데 실패했습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        }

        appInfoDownBtn.setOnClickListener {
            goToPlayStore(this)
        }

        appInfoLicense.setOnClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            OssLicensesMenuActivity.setActivityTitle("오픈소스 라이센스 목록")
        }

        appInfoTermsService.setOnClickListener {
            val intent = Intent(this@SettingActivity, TermsOfServiceActivity::class.java)
            startActivity(intent)
        }

        appInfoPB.visibility = View.GONE
        appInfoVersionValue.visibility = View.VISIBLE

        ShowDialogClass(this)
            .setBackPressed(viewAppInfo.findViewById(R.id.appInfoBack))
            .show(viewAppInfo, true)
    }

    private fun convertDateFormat(s: String): String {
        val fs = LocalDateTime.parse(s)
        return "${fs.year}.${fs.monthValue}.${fs.dayOfMonth}"
    }

    private fun applyUserEmail() {
        if (getUserEmail(this) != "") {
            binding.settingUserEmail.text = getUserEmail(this)
            binding.settingUserIcon.visibility = View.VISIBLE
        } else {
            binding.settingUserEmail.text = getString(R.string.please_login)
            binding.settingUserIcon.visibility = View.GONE
        }
    }

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

    private fun applyLastLogin(): String {
        // 마지막 로그인 플랫폼 종류
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

    private fun applyNotification() {
        if (!RequestPermissionsUtil(this).isNotificationPermitted()) {
            setUserNoti(this,notiPM,false)
            setUserNoti(this,notiEvent,false)
            setUserNoti(this,notiNight,false)
        }
    }

    /** 이미지 드로어블 할당 **/
    private fun setImageDrawable(imageView: ImageView, src: Int) {
        imageView.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                src, null
            )
        )
    }

    /** 메인 액티비티로 이동 **/
    private fun goMain() {
        val intent = Intent(this@SettingActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    /** 라디오 버튼 DrawableEnd Tint 변경 **/
    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun changeCheckIcon(rbOn: RadioButton, rbOff1: RadioButton, rbOff2: RadioButton) {
        rbOn.compoundDrawableTintList =
            ColorStateList.valueOf(getColor(R.color.main_blue_color))
        rbOff1.compoundDrawableTintList =
            ColorStateList.valueOf(getColor(android.R.color.transparent))
        rbOff2.compoundDrawableTintList =
            ColorStateList.valueOf(getColor(android.R.color.transparent))
    }

    /** 언어 라디오 버튼 클릭 시 이벤트 처리 **/
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
            saveLanguageChange() // 언어 설정 변경 후 어플리케이션 재시작
        }
    }

    /** 테마 라디오 버튼 클릭 시 이벤트 처리 **/
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

    /** 언어 설정 변경 후 어플리케이션 재시작 **/
    private fun saveLanguageChange() {
        val builder = Dialog(this)
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_alert_single_btn,null)
        builder.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(view)
            setCancelable(false)
        }

        builder.create()

        val title = view.findViewById<TextView>(R.id.alertSingleTitle)
        val apply = view.findViewById<AppCompatButton>(R.id.alertSingleApplyBtn)

        title.text = "설정을 저장하였습니다.\n앱을 다시 시작합니다."
        apply.text = getString(R.string.ok)
        apply.setOnClickListener {
            RefreshUtils(this).refreshApplication()
        }
        builder.show()
    }

    /** 자주묻는질문 아이템 추가하기 **/
    private fun addFaqItem(title: String, content: String) {
        val item = AdapterModel.FaqItem(title, content)
        faqItem.add(item)
    }

    /** 공지사항 아이템 추가하기 **/
    private fun addNoticeItem(created: String, modified: String, title: String, content: String) {
        val item = AdapterModel.NoticeItem(created,modified,title,content)
        noticeItem.add(item)
    }
}