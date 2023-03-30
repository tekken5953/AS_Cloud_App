package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.FaqAdapter
import com.example.airsignal_app.adapter.NoticeAdapter
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.dao.IgnoredKeyFile.notiEvent
import com.example.airsignal_app.dao.IgnoredKeyFile.notiNight
import com.example.airsignal_app.dao.IgnoredKeyFile.notiPM
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.databinding.ActivitySettingBinding
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.util.CustomSnackBar
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.ShowDialogClass
import com.example.airsignal_app.view.test.TestDesignActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val sp by lazy { SharedPreferenceManager(this) }
    private val faqItem = arrayListOf<String>()
    private val noticeItem = arrayListOf<AdapterModel.NoticeItem>()
    private var isInit = true

    override fun onResume() {
        super.onResume()

        binding.settingUserEmail.text =
            sp.getString(userEmail)

        // 설정 페이지 테마 항목이름 바꾸기
        when (sp.getString("theme")) {
            "dark" -> {
                binding.settingThemeThemeRight.text = getString(R.string.theme_dark)
            }
            "light" -> {
                binding.settingThemeThemeRight.text = getString(R.string.theme_light)
            }
            else -> {
                binding.settingThemeThemeRight.text = getString(R.string.theme_system)
            }
        }

        // 설정 페이지 언어 항목이름 바꾸기
        when (sp.getString("lang")) {
            "english" -> {
                binding.settingThemeLangRight.text = getString(R.string.english)
            }
            "korean" -> {
                binding.settingThemeLangRight.text = getString(R.string.korean)
            }
            else -> {
                binding.settingThemeLangRight.text = getString(R.string.system_lang)
            }
        }

        settingAlarmRadio(switch = binding.settingNotiPMRight, checked = sp.getBoolean(notiPM))
        settingAlarmRadio(
            switch = binding.settingNotiEventRight,
            checked = sp.getBoolean(notiEvent)
        )
        settingAlarmRadio(
            switch = binding.settingNotiNightRight,
            checked = sp.getBoolean(notiNight)
        )

        // 야간 알림 허용 텍스트 설정
        setNightAlertsSpan(binding.settingNotiNightLeft)

        // 알림 스위치 이벤트 리스너
        checkNotification(binding.settingNotiPMRight, notiPM,"미세먼지")
        checkNotification(binding.settingNotiEventRight, notiEvent,"이벤트")
        checkNotification(binding.settingNotiNightRight, notiNight,"야간")
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SettingActivity, R.layout.activity_setting)

        // 마지막 로그인 플랫폼 종류
        val lastLogin = sp.getString(lastLoginPlatform)
        // 로그인 시 저장된 핸드폰 번호
        val email = sp.getString(userEmail)

        when(lastLogin) {
            "google" -> { binding.settingUserIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,
                R.drawable.google_icon,null))}
            "kakao" -> { binding.settingUserIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,
                    R.drawable.kakao_icon,null))}
            "naver" -> { binding.settingUserIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,
                R.drawable.naver_icon,null))}
        }

        isInit = false

        // 뒤로가기 버튼 클릭
        binding.settingBack.setOnClickListener { onBackPressed() }

        // 로그아웃 버튼 클릭
        binding.settingLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.setting_logout))
                .setMessage(getString(R.string.logout_msg))
                .setPositiveButton(
                    getString(R.string.ok)
                ) { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        when (lastLogin) { // 로그인 했던 플랫폼에 따라서 로그아웃 로직 호출
                            "kakao" -> {
                                KakaoLogin(this@SettingActivity).logout(email)
//                            KakaoLogin(this).disconnectFromKakao()
                            }
                            "naver" -> {
                                NaverLogin(this@SettingActivity).logout(email)
                            }
                            "google" -> {
                                GoogleLogin(this@SettingActivity).logout()
                            }
                        }
                        delay(100)
                        sp.run {
                            removeKey("user_id")
                            removeKey("user_profile")
                            removeKey("lastLoginPhone")
                            removeKey("lastLoginPlatform")
                            removeKey("user_email")
                        }
                    }
                }
                .setNegativeButton(
                    getString(R.string.no)
                ) { dialog, _ -> dialog?.dismiss() }

            builder.create().show()
        }

        // 테마 설정 클릭
        binding.settingThemeTR1.setOnClickListener {
            val build = AlertDialog.Builder(this, R.style.AlertDialog)
            // 레이아웃 뷰 생성
            val themeView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_change_theme, null)
            build.setView(themeView)
            val alertDialog: AlertDialog = build.create()
            val backBtn: ImageView = themeView.findViewById(R.id.changeThemeBack)
            val systemTheme: RadioButton = themeView.findViewById(R.id.systemThemeRb)
            val lightTheme: RadioButton = themeView.findViewById(R.id.lightThemeRb)
            val darkTheme: RadioButton = themeView.findViewById(R.id.darkThemeRb)
            val radioGroup: RadioGroup = themeView.findViewById(R.id.changeThemeRadioGroup)

            // 현재 저장된 테마에 따라서 라디오버튼 체크
            when (sp.getString("theme")) {
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
                            radioButton = systemTheme
                        )
                        changeCheckIcon(systemTheme, lightTheme, darkTheme)
                    }
                    // 라이트 모드
                    lightTheme.id -> {
                        changedThemeRadio(
                            mode = AppCompatDelegate.MODE_NIGHT_NO,
                            dbData = "light",
                            radioGroup = radioGroup,
                            radioButton = lightTheme
                        )
                        changeCheckIcon(lightTheme, systemTheme, darkTheme)
                    }
                    // 다크 모드
                    darkTheme.id -> {
                        changedThemeRadio(
                            mode = AppCompatDelegate.MODE_NIGHT_YES,
                            dbData = "dark",
                            radioGroup = radioGroup,
                            radioButton = darkTheme
                        )
                        changeCheckIcon(darkTheme, systemTheme, lightTheme)
                    }
                }
            }
            backBtn.setOnClickListener {
                alertDialog.dismiss()
                onResume()
            }

            alertDialog.show()
        }

        // 언어 설정 클릭
        binding.settingThemeTR2.setOnClickListener {
            val build = AlertDialog.Builder(this, R.style.AlertDialog)
            // 뷰 레이아웃 생성
            val langView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_change_language, null)
            build.setView(langView)
            val alertDialog: AlertDialog = build.create()
            // 뒤로가기 버튼
            val back: ImageView = langView.findViewById(R.id.changeLangBack)
            back.setOnClickListener {
                alertDialog.dismiss()
            }
            val systemLang: RadioButton = langView.findViewById(R.id.systemLangRb)
            val koreanLang: RadioButton = langView.findViewById(R.id.koreanRB)
            val englishLang: RadioButton = langView.findViewById(R.id.englishRB)
            val radioGroup: RadioGroup = langView.findViewById(R.id.changeLangRadioGroup)

            // 기존에 저장 된 언어로 라디오 버튼 체크
            when (sp.getString("lang")) {
                "korean" -> {
                    radioGroup.check(koreanLang.id)
                    changeCheckIcon(koreanLang, englishLang, systemLang)
                }
                "english" -> {
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
                            lang = "system",
                            radioGroup = radioGroup,
                            radioButton = systemLang
                        )
                        changeCheckIcon(systemLang, koreanLang, englishLang)
                    }
                    koreanLang.id -> {
                        changedLangRadio(
                            lang = "korean",
                            radioGroup = radioGroup,
                            radioButton = koreanLang
                        )
                        changeCheckIcon(koreanLang, systemLang, englishLang)
                    }
                    englishLang.id -> {
                        changedLangRadio(
                            lang = "english",
                            radioGroup = radioGroup,
                            radioButton = englishLang
                        )
                        changeCheckIcon(englishLang, koreanLang, systemLang)
                    }
                }
            }

            alertDialog.show()
        }

        val detailView: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_detail, null)
        val detailDate: TextView = detailView.findViewById(R.id.detailNoticeDate)
        val detailTitle: TextView = detailView.findViewById(R.id.detailTitle)
        val backDetail: ImageView = detailView.findViewById(R.id.detailBack)
        backDetail.setOnClickListener { onBackPressed() }

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

            val backNotice: ImageView = noticeMainView.findViewById(R.id.noticeBack)
            backNotice.setOnClickListener { onBackPressed() }

            ShowDialogClass(this).show(noticeMainView, true)

            noticeAdapter.setOnItemClickListener(object : NoticeAdapter.OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    detailDate.text = noticeItem[position].date
                    detailDate.visibility = View.VISIBLE
                    detailTitle.text = noticeTitle.text.toString()
                    ShowDialogClass(this@SettingActivity).show(detailView, true)
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

            val backFaq: ImageView = faqMainView.findViewById(R.id.faqBack)
            backFaq.setOnClickListener { onBackPressed() }

            ShowDialogClass(this).show(faqMainView, true)

            faqAdapter.setOnItemClickListener(object : FaqAdapter.OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    detailDate.visibility = View.GONE
                    detailTitle.text = faqTitle.text.toString()
                    ShowDialogClass(this@SettingActivity).show(detailView, true)
                }
            })
        }

        binding.settingTest.setOnClickListener {
            val intent = Intent(this, TestDesignActivity::class.java)
            startActivity(intent)
        }

        binding.settingAppInfo.setOnClickListener {
            val viewAppInfo: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_app_info, null)
            val backAppInfo: ImageView = viewAppInfo.findViewById(R.id.appInfoBack)
            backAppInfo.setOnClickListener { onBackPressed() }
            ShowDialogClass(this@SettingActivity).show(viewAppInfo, true)
        }
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
    private fun changedLangRadio(lang: String, radioGroup: RadioGroup, radioButton: RadioButton) {
        if (sp.getString("lang") != lang) { // 현재 설정된 언어인지 필터링
            sp.setString("lang", lang)  // 다른 언어라면 db 값 변경
            radioGroup.check(radioButton.id) // 라디오 버튼 체크
            saveLanguageChange() // 언어 설정 변경 후 어플리케이션 재시작
        }
    }

    /** 테마 라디오 버튼 클릭 시 이벤트 처리 **/
    private fun changedThemeRadio(
        mode: Int,
        dbData: String,
        radioGroup: RadioGroup,
        radioButton: RadioButton
    ) {
        // 테마모드 변경
        AppCompatDelegate.setDefaultNightMode(mode)
        // DB에 바뀐 정보 저장
        sp.setString("theme", dbData)
        // 라디오 버튼 체크
        radioGroup.check(radioButton.id)
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

    /** 알림 권한을 체크하고 상태저장 **/
    private fun checkNotification(switch: SwitchCompat, tag: String, title: String) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            val permission = RequestPermissionsUtil(this@SettingActivity)
            if (!permission.isNotificationPermitted()) {
                permission.requestNotification()
                showSnackBar(isChecked,title)
                sp.setBoolean(tag, isChecked)
            } else {
                showSnackBar(isChecked,title)
                sp.setBoolean(tag, isChecked)
            }
        }
    }

    /** 알림 설정 불러오기 **/
    private fun settingAlarmRadio(switch: SwitchCompat, checked: Boolean) {
        switch.isChecked = checked
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

    /** 야간 알림 허용 텍스트 설정 **/
    private fun setNightAlertsSpan(textView: TextView) {
        val span = SpannableStringBuilder(textView.text)
        val formatText = textView.text.split(System.lineSeparator())
        // 색상변경
        span.setSpan(
            ForegroundColorSpan(getColor(R.color.main_gray_color)),
            formatText[0].length, span.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // 크기변경
        span.setSpan(
            RelativeSizeSpan(0.8f),
            formatText[0].length, span.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = span
    }

    private fun showSnackBar(isAllow: Boolean, title: String) {
        val img = ContextCompat.getDrawable(this@SettingActivity, R.drawable.alert)!!
        img.setTint(getColor(R.color.mode_color_view))
        if (isAllow) {
            if (!isInit) { CustomSnackBar.make(binding.root, "$title 알림을 허용하였습니다", img).show() }
        } else {
            if (!isInit) { CustomSnackBar.make(binding.root, "$title 알림을 거부하였습니다", img).show() }
        }
    }
}