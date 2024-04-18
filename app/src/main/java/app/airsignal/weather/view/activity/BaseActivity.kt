package app.airsignal.weather.view.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import app.airsignal.weather.dao.StaticDataObject.LANG_EN
import app.airsignal.weather.dao.StaticDataObject.LANG_KR
import app.airsignal.weather.dao.StaticDataObject.THEME_DARK
import app.airsignal.weather.dao.StaticDataObject.THEME_LIGHT
import app.airsignal.weather.db.sp.GetAppInfo.getUserFontScale
import app.airsignal.weather.db.sp.GetAppInfo.getUserLocation
import app.airsignal.weather.db.sp.GetAppInfo.getUserTheme
import app.airsignal.weather.db.sp.SetSystemInfo
import app.airsignal.weather.db.sp.SetSystemInfo.updateConfiguration
import app.airsignal.weather.db.sp.SpDao.TEXT_SCALE_BIG
import app.airsignal.weather.db.sp.SpDao.TEXT_SCALE_SMALL
import java.util.*

abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var binding: VB
    abstract val resID: Int

    protected fun initBinding() {
        binding = DataBindingUtil.setContentView(this@BaseActivity, resID)
        binding.lifecycleOwner = this@BaseActivity
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        applyUserLanguage()
        applyUserFontScale()
        applyUserTheme()
    }

    // 설정된 언어정보 불러오기
    private fun applyUserLanguage() {
        when (getUserLocation(this@BaseActivity)) {
            LANG_KR -> {  updateConfiguration(this@BaseActivity, Locale.KOREA) }
            LANG_EN -> {  updateConfiguration(this@BaseActivity, Locale.ENGLISH) }
            else -> {  updateConfiguration(this@BaseActivity, Locale.getDefault()) }
        }
    }

    // 설정된 폰트크기 불러오기
    private fun applyUserFontScale() {
        when (getUserFontScale(this@BaseActivity)) {
            TEXT_SCALE_SMALL -> { SetSystemInfo.setTextSizeSmall(this@BaseActivity) }
            TEXT_SCALE_BIG -> { SetSystemInfo.setTextSizeLarge(this@BaseActivity) }
            else -> { SetSystemInfo.setTextSizeDefault(this@BaseActivity) }
        }
    }

    // 설정된 테마 정보 불러오기
    private fun applyUserTheme() {
        when (getUserTheme(this@BaseActivity)) {
            THEME_DARK -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
            THEME_LIGHT -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
            else -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
        }
    }
}