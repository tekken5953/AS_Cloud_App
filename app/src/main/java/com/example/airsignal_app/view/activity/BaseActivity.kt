package com.example.airsignal_app.view.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.ConvertDataType

/**
 * @author : Lee Jae Young
 * @since : 2023-05-04 오후 2:56
 **/
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)

        val sp = SharedPreferenceManager(newBase!!)
        // 설정된 언어정보 불러오기
        when(sp.getString(IgnoredKeyFile.userLocation)) {
            getString(R.string.korean) -> {
                ConvertDataType.setLocaleToKorea(this)
            }
            getString(R.string.english) -> {
                ConvertDataType.setLocaleToEnglish(this)
            }
            else -> {
                ConvertDataType.setLocaleToSystem(this)
            }
        }

        when(sp.getString(IgnoredKeyFile.userFontScale)) {
            "small" -> {
                ConvertDataType.setTextSizeSmall(this)
            }
            "big" -> {
                ConvertDataType.setTextSizeLarge(this)
            }
            else -> {
                ConvertDataType.setTextSizeDefault(this)
            }
        }

        // 설정된 테마 정보 불러오기
        when(sp.getString("theme")) {
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}