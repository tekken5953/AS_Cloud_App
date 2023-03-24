package com.example.airsignal_app.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivitySettingBinding
import com.example.airsignal_app.login.GoogleLogin
import com.example.airsignal_app.login.KakaoLogin
import com.example.airsignal_app.login.NaverLogin
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.SharedPreferenceManager

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val sp by lazy { SharedPreferenceManager(this) }

    override fun onResume() {
        super.onResume()

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SettingActivity, R.layout.activity_setting)

        // 마지막 로그인 플랫폼 종류
        val lastLogin = SharedPreferenceManager(this).getString(IgnoredKeyFile.lastLoginPlatform)
        // 로그인 시 저장된 핸드폰 번호
        val phoneNumber = SharedPreferenceManager(this).getString(IgnoredKeyFile.lastLoginPhone)

        // 뒤로가기 버튼 클릭
        binding.settingBack.setOnClickListener { onBackPressed() }

        // 로그아웃 버튼 클릭
        binding.settingLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("로그아웃").setMessage("${lastLogin}에서 로그아웃 하시겠습니까?")
                .setPositiveButton(
                    "예"
                ) { _, _ ->
                    sp.clear()  // LocalDB Clear
                    when (lastLogin) { // 로그인 했던 플랫폼에 따라서 로그아웃 로직 호출
                        "kakao" -> {
                            KakaoLogin(this).logout(phoneNumber)
                        }
                        "naver" -> {
                            NaverLogin(this).logout(phoneNumber)
                        }
                        "google" -> {
                            GoogleLogin(this).logout()
                        }
                    }
                }
                .setNegativeButton(
                    "아니오"
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
                }
                "light" -> {
                    radioGroup.check(lightTheme.id)
                }
                else -> {
                    radioGroup.check(systemTheme.id)
                }
            }

            // 라디오 버튼 클릭 시 이벤트처리
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                // 아이디에 따라 분할
                when (checkedId) {
                    // 시스템 설정
                    systemTheme.id -> {
                        changedThemeRadio(mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                            dbData = "system",
                            radioGroup = radioGroup,
                            radioButton = systemTheme)
                    }
                    // 라이트 모드
                    lightTheme.id -> {
                        changedThemeRadio(mode = AppCompatDelegate.MODE_NIGHT_NO,
                            dbData = "light",
                            radioGroup = radioGroup,
                            radioButton = lightTheme)
                    }
                    // 다크 모드
                    darkTheme.id -> {
                        changedThemeRadio(mode = AppCompatDelegate.MODE_NIGHT_YES,
                            dbData = "dark",
                            radioGroup = radioGroup,
                            radioButton = darkTheme)
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
                }
                "english" -> {
                    radioGroup.check(englishLang.id)
                }
                else -> {
                    radioGroup.check(systemLang.id)
                }
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    systemLang.id -> {
                        changedLangRadio(lang = "system",
                            radioGroup = radioGroup,
                            radioButton = systemLang)
                    }
                    koreanLang.id -> {
                        changedLangRadio(lang = "korean",
                            radioGroup = radioGroup,
                            radioButton = koreanLang)
                    }
                    englishLang.id -> {
                        changedLangRadio(lang = "english",
                            radioGroup = radioGroup,
                            radioButton = englishLang)
                    }
                }
            }

            alertDialog.show()
        }
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
    private fun changedThemeRadio(mode: Int,dbData: String, radioGroup: RadioGroup, radioButton: RadioButton) {
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
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ ->
                RefreshUtils(this).refreshApplication()
            }
            .show()
    }
}