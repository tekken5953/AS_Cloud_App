package com.example.airsignal_app

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

        val lastLogin = SharedPreferenceManager(this).getString(IgnoredKeyFile.lastLoginPlatform)
        val phoneNumber = SharedPreferenceManager(this).getString(IgnoredKeyFile.lastLoginPhone)

        binding.settingBack.setOnClickListener {
            onBackPressed()
        }

        binding.settingLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("로그아웃").setMessage("${lastLogin}에서 로그아웃 하시겠습니까?")
                .setPositiveButton(
                    "예"
                ) { _, _ ->
                    when (lastLogin) {
                        "kakao" -> {
                            KakaoLogin(this@SettingActivity).logout(phoneNumber)
                        }
                        "naver" -> {
                            NaverLogin(this@SettingActivity).logout(phoneNumber)
                        }
                        "google" -> {
                            GoogleLogin(this@SettingActivity).logout()
                        }
                    }
                }
                .setNegativeButton(
                    "아니오"
                ) { dialog, _ -> dialog?.dismiss() }

            builder.create().show()
        }

        binding.settingThemeTR1.setOnClickListener {
            val build = AlertDialog.Builder(this, R.style.AlertDialog)
            val themeView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_change_theme, null)
            build.setView(themeView)
            val alertDialog: AlertDialog = build.create()
            val backBtn: ImageView = themeView.findViewById(R.id.changeThemeBack)
            val systemTheme: RadioButton = themeView.findViewById(R.id.systemThemeRb)
            val lightTheme: RadioButton = themeView.findViewById(R.id.lightThemeRb)
            val darkTheme: RadioButton = themeView.findViewById(R.id.darkThemeRb)
            val radioGroup: RadioGroup = themeView.findViewById(R.id.changeThemeRadioGroup)
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

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    systemTheme.id -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        sp.setString("theme", "system")
                        radioGroup.check(systemTheme.id)
                    }
                    lightTheme.id -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        sp.setString("theme", "light")
                        radioGroup.check(lightTheme.id)
                    }
                    darkTheme.id -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        sp.setString("theme", "dark")
                        radioGroup.check(darkTheme.id)
                    }
                }
            }
            backBtn.setOnClickListener {
                alertDialog.dismiss()
                onResume()
            }

            alertDialog.show()
        }

        binding.settingThemeTR2.setOnClickListener {
            val build = AlertDialog.Builder(this, R.style.AlertDialog)
            val langView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_change_language, null)
            build.setView(langView)
            val alertDialog: AlertDialog = build.create()
            val back: ImageView = langView.findViewById(R.id.changeLangBack)
            back.setOnClickListener {
                alertDialog.dismiss()
            }
            val systemLang: RadioButton = langView.findViewById(R.id.systemLangRb)
            val koreanLang: RadioButton = langView.findViewById(R.id.koreanRB)
            val englishLang: RadioButton = langView.findViewById(R.id.englishRB)
            val radioGroup: RadioGroup = langView.findViewById(R.id.changeLangRadioGroup)

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
                        if (sp.getString("lang") != "system") {
                            sp.setString("lang", "system")
                            radioGroup.check(systemLang.id)
                            saveLanguageChange()
                        }
                    }
                    koreanLang.id -> {
                        if (sp.getString("lang") != "korean") {
                            sp.setString("lang", "korean")
                            radioGroup.check(koreanLang.id)
                            saveLanguageChange()
                        }
                    }
                    englishLang.id -> {
                        if (sp.getString("lang") != "english") {
                            sp.setString("lang", "english")
                            radioGroup.check(englishLang.id)
                            saveLanguageChange()
                        }
                    }
                }
            }

            alertDialog.show()
        }
    }

    private fun saveLanguageChange() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.save_change))
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ ->
                RefreshUtils().refreshApplication(this)
            }
            .show()
    }
}