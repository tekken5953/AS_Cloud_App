package app.airsignal.weather.view.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import app.airsignal.weather.dao.StaticDataObject.LANG_EN
import app.airsignal.weather.dao.StaticDataObject.LANG_KR
import app.airsignal.weather.dao.StaticDataObject.TEXT_SCALE_BIG
import app.airsignal.weather.dao.StaticDataObject.TEXT_SCALE_SMALL
import app.airsignal.weather.dao.StaticDataObject.THEME_DARK
import app.airsignal.weather.dao.StaticDataObject.THEME_LIGHT
import app.airsignal.weather.util.`object`.GetAppInfo.getUserFontScale
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLocation
import app.airsignal.weather.util.`object`.GetAppInfo.getUserTheme
import app.airsignal.weather.util.`object`.SetSystemInfo

/**
 * @author : Lee Jae Young
 * @since : 2023-05-04 오후 2:56
 **/
abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
    abstract val resID: Int

    protected fun initBinding() {
        binding = DataBindingUtil.setContentView(this, resID)
        binding.lifecycleOwner = this
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)

        // 설정된 언어정보 불러오기
        when (getUserLocation(this)) {
            LANG_KR -> {
                SetSystemInfo.setLocaleToKorea(this)
            }
            LANG_EN -> {
                SetSystemInfo.setLocaleToEnglish(this)
            }
            else -> {
                SetSystemInfo.setLocaleToSystem(this)
            }
        }

        // 설정된 폰트크기 불러오기
        when (getUserFontScale(this)) {
            TEXT_SCALE_SMALL -> {
                SetSystemInfo.setTextSizeSmall(this)
            }
            TEXT_SCALE_BIG -> {
                SetSystemInfo.setTextSizeLarge(this)
            }
            else -> {
                SetSystemInfo.setTextSizeDefault(this)
            }
        }

        // 설정된 테마 정보 불러오기
        when (getUserTheme(this)) {
            THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}