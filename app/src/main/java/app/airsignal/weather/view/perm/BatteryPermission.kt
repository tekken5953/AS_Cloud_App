package app.airsignal.weather.view.perm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import app.airsignal.weather.koin.BaseApplication.Companion.getAppContext


class BatteryPermission {
    @SuppressLint("BatteryLife", "InlinedApi")
    fun checkBatteryOptimization() {
        val appContext = getAppContext()
        val packageName: String = appContext.applicationContext.packageName
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val ignoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
        if (ignoringBatteryOptimizations) { // 예외사항에 이미 추가되었는지 확인
            return
        }
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse(String.format("package:%s", packageName))
        appContext.startActivity(intent)
    }

    fun isBatteryOptimizationEnable(): Boolean {
        val appContext = getAppContext()
        val packageName: String = appContext.applicationContext.packageName
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }
}