package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.FaqAdapter
import com.example.airsignal_app.adapter.NoticeAdapter
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.dao.IgnoredKeyFile.notiEvent
import com.example.airsignal_app.dao.IgnoredKeyFile.notiNight
import com.example.airsignal_app.dao.IgnoredKeyFile.notiPM
import com.example.airsignal_app.dao.StaticDataObject.EVENT_ALL_NOTI
import com.example.airsignal_app.dao.StaticDataObject.NIGHT_EVENT_NOTI
import com.example.airsignal_app.databinding.ActivitySettingBinding
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.login.PhoneLogin
import com.example.airsignal_app.util.*
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserFontScale
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLocation
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserNotiEvent
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserNotiNight
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserNotiPM
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserTheme
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.util.`object`.SetAppInfo.removeAllKeys
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserFontScale
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserLocation
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserNoti
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserTheme
import com.example.airsignal_app.view.ShowDialogClass
import com.example.airsignal_app.view.custom_view.NotiSwitchView
import com.example.airsignal_app.view.custom_view.SettingSystemView
import com.example.airsignal_app.view.custom_view.SnackBarUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class SettingActivity
    : BaseActivity<ActivitySettingBinding>() {
    override val resID: Int get() = R.layout.activity_setting

    private val faqItem = arrayListOf<String>()
    private val noticeItem = arrayListOf<AdapterModel.NoticeItem>()
    private var isInit = true
    private val dialog by lazy { ShowDialogClass().getInstance(this) }

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

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            window.statusBarColor = Color.BLACK
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }

        // 로그인 시 저장된 핸드폰 번호
        val email = getUserEmail(this)

        isInit = false

        val lastLogin = applyLastLogin()

        // 뒤로가기 버튼 클릭
        binding.settingBack.setOnClickListener {
//            VibrateUtil(this).make(20)
            onBackPressedDispatcher.onBackPressed()
        }

        // 로그아웃 버튼 클릭
        binding.settingLogOut.setOnClickListener {
            if (binding.settingLogOut.text == getString(R.string.setting_logout)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.setting_logout))
                    .setMessage(getString(R.string.logout_msg))
                    .setPositiveButton(
                        getString(R.string.yes)
                    ) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            when (lastLogin) { // 로그인 했던 플랫폼에 따라서 로그아웃 로직 호출
                                "kakao" -> {
                                    KakaoLogin(this@SettingActivity).logout(email)
                                }
                                "naver" -> {
                                    NaverLogin(this@SettingActivity).logout(email)
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
                    .setNegativeButton(
                        getString(R.string.no)
                    ) { dialog, _ -> dialog?.dismiss() }

                builder.create().show()
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

            dialog
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
                            dbData = "system",
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

            dialog
                .setBackPressed(cancelBtn)
                .show(langView, true)

            // 기존에 저장 된 언어로 라디오 버튼 체크
            when (getUserLocation(this)) {
                getString(R.string.korean) -> {
                    radioGroup.check(koreanLang.id)
                    changeCheckIcon(koreanLang, englishLang, systemLang)
                }
                getString(R.string.english) -> {
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
                            lang = getString(R.string.system_lang),
                            radioGroup = radioGroup,
                            radioButton = systemLang,
                            cancelBtn
                        )
                        changeCheckIcon(systemLang, koreanLang, englishLang)
                    }
                    koreanLang.id -> {
                        changedLangRadio(
                            lang = getString(R.string.korean),
                            radioGroup = radioGroup,
                            radioButton = koreanLang,
                            cancelBtn
                        )
                        changeCheckIcon(koreanLang, systemLang, englishLang)
                    }
                    englishLang.id -> {
                        changedLangRadio(
                            lang = getString(R.string.english),
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

            dialog
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
                        setUserFontScale(this,"small")
                        radioGroup.check(small.id)
                        this.goMain()
                    }
                    big.id -> {
                        setUserFontScale(this,"big")
                        radioGroup.check(big.id)
                        this.goMain()
                    }
                    default.id -> {
                        setUserFontScale(this,"default")
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

        // 공지사항 클릭
        binding.settingNotice.setOnClickListener {
            val noticeMainView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_notice, null)
            val noticeAdapter = NoticeAdapter(this, noticeItem)
            val recyclerView: RecyclerView = noticeMainView.findViewById(R.id.noticeRv)
            val noticeTitle: TextView = noticeMainView.findViewById(R.id.noticeTitle)
            recyclerView.adapter = noticeAdapter
            noticeItem.clear()
            for (i: Int in 5 downTo 0) {
                addNoticeItem("01.1${i}", "$i 가나다 라마바사아 자차 카 타파하 가나다 라마바사아 자차 카 타파하")
                noticeAdapter.notifyItemInserted(i)
            }

            dialog
                .setBackPressed(noticeMainView.findViewById(R.id.noticeBack))
                .show(noticeMainView, true)

            noticeAdapter.setOnItemClickListener(object : NoticeAdapter.OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    detailDate.text = noticeItem[position].date
                    detailDate.visibility = View.VISIBLE
                    detailTitle.text = noticeTitle.text.toString()
                    dialog
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
            faqItem.clear()
            recyclerView.adapter = faqAdapter
            for (i: Int in 0..5) {
                addFaqItem("$i 가나다 라마바사아 자차 카 타파하")
                faqAdapter.notifyItemInserted(i)
            }

            dialog
                .setBackPressed(faqMainView.findViewById(R.id.faqBack))
                .show(faqMainView, true)

            faqAdapter.setOnItemClickListener(object : FaqAdapter.OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    detailDate.visibility = View.GONE
                    detailTitle.text = faqTitle.text.toString()
                    dialog.show(detailView, true)
                }
            })
        }

        // 앱 정보 클릭
        binding.settingAppInfo.setOnClickListener {
            val viewAppInfo: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_app_info, null)

            dialog.setBackPressed(viewAppInfo.findViewById(R.id.appInfoBack))
                .show(viewAppInfo, true)
        }}

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
//                binding.settingSystemTheme.fetchData(getString(R.string.theme_dark))
            }
            "light" -> {
                binding.settingSystemTheme.fetchData(getString(R.string.theme_light))
//                binding.settingSystemTheme.fetchData(getString(R.string.theme_light))
            }
            else -> {
                binding.settingSystemTheme.fetchData(getString(R.string.theme_system))
//                binding.settingSystemFont.fetchData(getString(R.string.theme_system))
            }
        }
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
            getString(R.string.english) -> {
                binding.settingSystemLang.fetchData(getString(R.string.english))
            }
            getString(R.string.korean) -> {
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
            "google" -> {
                setImageDrawable(binding.settingUserIcon, R.drawable.google_icon)
            }
            "kakao" -> {
                setImageDrawable(binding.settingUserIcon, R.drawable.kakao_icon)
            }
            "naver" -> {
                setImageDrawable(binding.settingUserIcon, R.drawable.naver_icon)
            }
            "phone" -> {
                setImageDrawable(binding.settingUserIcon, R.drawable.phone_icon)
            }
        }

        return lastLogin
    }

    private fun applyNotification() {
        binding.notiPM.fetchData(getUserNotiPM(this))
        binding.notiEvent.fetchData(getUserNotiEvent(this))
        binding.notiNight.fetchData(getUserNotiNight(this))
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
    private fun changedLangRadio(lang: String, radioGroup: RadioGroup, radioButton: RadioButton, cancel: ImageView) {
        if (getUserLocation(this) != lang) { // 현재 설정된 언어인지 필터링
            cancel.isEnabled = false
            setUserLocation(this,lang)  // 다른 언어라면 db 값 변경
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
            val intent = Intent(this@SettingActivity, RedirectPermissionActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    /** 언어 설정 변경 후 어플리케이션 재시작 **/
    private fun saveLanguageChange() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.save_change))
            .setPositiveButton(
                getString(R.string.apply)
            ) { _, _ ->
                RefreshUtils(this).refreshApplication()
            }
            .show()
    }

    /** 자주묻는질문 아이템 추가하기 **/
    private fun addFaqItem(text: String) {
        faqItem.add(text)
    }

    /** 공지사항 아이템 추가하기 **/
    private fun addNoticeItem(date: String, title: String) {
        val item = AdapterModel.NoticeItem(date, title)
        noticeItem.add(item)
    }
}