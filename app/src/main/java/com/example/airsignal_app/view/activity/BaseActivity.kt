package com.example.airsignal_app.view.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.dao.StaticDataObject.TAG_L
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.ConvertDataType

/**
 * @author : Lee Jae Young
 * @since : 2023-05-04 오후 2:56
 **/
open class BaseActivity : AppCompatActivity() {

//    private fun updateAdapterItem() {
//        val newDaily = dailyWeatherList
//        val newWeekly = weeklyWeatherList
//        dailyWeatherList.clear()
//        weeklyWeatherList.clear()
//        dailyWeatherList.addAll(newDaily)
//        weeklyWeatherList.addAll(newWeekly)
//        weeklyWeatherAdapter.notifyDataSetChanged()
//        dailyWeatherAdapter.notifyDataSetChanged()
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        Log.d("configurationTest", this.localClassName)
//        Handler(Looper.getMainLooper()).postDelayed({
//            updateAdapterItem()
//            Log.d("configurationTest", "updateAdapterItem")
//        },5000)
//    }

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

        when(sp.getString("scale")) {
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