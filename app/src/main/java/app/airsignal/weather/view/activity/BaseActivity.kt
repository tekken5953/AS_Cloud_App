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
    import app.airsignal.weather.db.sp.SpDao.TEXT_SCALE_BIG
    import app.airsignal.weather.db.sp.SpDao.TEXT_SCALE_SMALL
    import kotlinx.coroutines.*

    /**
     * @author : Lee Jae Young
     * @since : 2023-05-04 오후 2:56
     **/
    abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity() {

        protected lateinit var binding: VB
        abstract val resID: Int

        protected fun initBinding() {
            binding = DataBindingUtil.setContentView(this@BaseActivity, resID)
            binding.lifecycleOwner = this@BaseActivity
        }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        val context = newBase ?: applicationContext
        applyUserLanguage(context)
        applyUserFontScale(context)
        applyUserTheme(context)
    }

    // 설정된 언어정보 불러오기
    private fun applyUserLanguage(context: Context) {
        when (getUserLocation(context)) {
            LANG_KR -> {
                SetSystemInfo.setLocaleToKorea(context)
            }
            LANG_EN -> {
                SetSystemInfo.setLocaleToEnglish(context)
            }
            else -> {
                SetSystemInfo.setLocaleToSystem(context)
            }
        }
    }

    // 설정된 폰트크기 불러오기
    private fun applyUserFontScale(context: Context) {
        when (getUserFontScale(context)) {
            TEXT_SCALE_SMALL -> {
                SetSystemInfo.setTextSizeSmall(context)
            }
            TEXT_SCALE_BIG -> {
                SetSystemInfo.setTextSizeLarge(context)
            }
            else -> {
                SetSystemInfo.setTextSizeDefault(context)
            }
        }
    }

    // 설정된 테마 정보 불러오기
    private fun applyUserTheme(context: Context) {
        when (getUserTheme(context)) {
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