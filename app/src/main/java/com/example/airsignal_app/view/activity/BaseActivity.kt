package com.example.airsignal_app.view.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.airsignal_app.R
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserFontScale
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLocation
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserTheme
import com.example.airsignal_app.util.`object`.SetSystemInfo

/**
 * @author : Lee Jae Young
 * @since : 2023-05-04 오후 2:56
 **/
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)

        // 설정된 언어정보 불러오기
        when(getUserLocation(this)) {
            getString(R.string.korean) -> {
                SetSystemInfo.setLocaleToKorea(this)
            }
            getString(R.string.english) -> {
                SetSystemInfo.setLocaleToEnglish(this)
            }
            else -> {
                SetSystemInfo.setLocaleToSystem(this)
            }
        }

        when(getUserFontScale(this)) {
            "small" -> {
                SetSystemInfo.setTextSizeSmall(this)
            }
            "big" -> {
                SetSystemInfo.setTextSizeLarge(this)
            }
            else -> {
                SetSystemInfo.setTextSizeDefault(this)
            }
        }

        // 설정된 테마 정보 불러오기
        when(getUserTheme(this)) {
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