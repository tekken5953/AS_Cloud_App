package app.airsignal.weather.as_eye.activity

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * @author : Lee Jae Young
 * @since : 2023-05-04 오후 2:56
 **/
abstract class BaseEyeActivity<VB : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var binding: VB
    abstract val resID: Int

    protected fun initBinding() {
        binding = DataBindingUtil.setContentView(this@BaseEyeActivity, resID)
        binding.lifecycleOwner = this@BaseEyeActivity
        applyStatusBar()
        window.statusBarColor = Color.parseColor("#F6F6F6")
    }

    @Suppress("DEPRECATION")
    private fun applyStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            val decorView = window.decorView
            var systemUiVisibility = decorView.systemUiVisibility

            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            decorView.systemUiVisibility = systemUiVisibility
        }
    }
}