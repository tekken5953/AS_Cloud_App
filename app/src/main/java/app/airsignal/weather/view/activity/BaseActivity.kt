package app.airsignal.weather.view.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.SetSystemInfo
import app.airsignal.weather.db.sp.SpDao
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
        when (GetAppInfo.getUserLocation()) {
            StaticDataObject.LANG_KR -> SetSystemInfo.updateConfiguration(this@BaseActivity, Locale.KOREA)
            StaticDataObject.LANG_EN -> SetSystemInfo.updateConfiguration(this@BaseActivity, Locale.ENGLISH)
            else -> SetSystemInfo.updateConfiguration(this@BaseActivity, Locale.getDefault())
        }
    }

    // 설정된 폰트크기 불러오기
    private fun applyUserFontScale() {
        when (GetAppInfo.getUserFontScale()) {
            SpDao.TEXT_SCALE_SMALL -> SetSystemInfo.setTextSizeSmall(this@BaseActivity)
            SpDao.TEXT_SCALE_BIG -> SetSystemInfo.setTextSizeLarge(this@BaseActivity)
            else -> SetSystemInfo.setTextSizeDefault(this@BaseActivity)
        }
    }

    // 설정된 테마 정보 불러오기
    private fun applyUserTheme() {
        when (GetAppInfo.getUserTheme()) {
            StaticDataObject.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            StaticDataObject.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}