package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import app.airsignal.weather.R
import app.airsignal.weather.view.perm.RequestPermissionsUtil

class WidgetPermActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_perm)

        val perm = RequestPermissionsUtil(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!perm.isBackgroundRequestLocation()) {
                perm.requestBackgroundLocation()
            } else {
                requestWhiteList()
            }
            finish()
        } else {
            requestWhiteList()
            finish()
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestWhiteList() {
        val packageName: String = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager?
        if (!pm!!.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }
}