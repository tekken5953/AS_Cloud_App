package app.airsignal.weather.view.widget

import androidx.appcompat.app.AppCompatActivity
import app.airsignal.weather.R
import app.airsignal.weather.utils.plain.ToastUtils
import app.airsignal.weather.view.perm.RequestPermissionsUtil

class WidgetPermActivity : AppCompatActivity() {
    private val perm by lazy {RequestPermissionsUtil(this)}

    override fun onResume() {
        super.onResume()
        if (!perm.isBackgroundRequestLocation()) {
            perm.requestBackgroundLocation()
            ToastUtils(this).showMessage(getString(R.string.widget_perm_denied))
        } else {
            val id = intent.extras?.getInt("id")
            when(intent.extras?.getString("sort")) {
                BaseWidgetProvider.WIDGET_22 -> WidgetProvider22().processUpdate(this, id)
                BaseWidgetProvider.WIDGET_42 -> WidgetProvider42().processUpdate(this, id)
            }

            finish()
        }
    }
}