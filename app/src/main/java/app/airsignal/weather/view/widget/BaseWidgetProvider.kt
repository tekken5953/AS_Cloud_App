package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.gps.GetLocation
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.retrofit.HttpClient
import app.airsignal.weather.util.AddressFromRegex
import app.airsignal.weather.util.LoggerUtil
import retrofit2.awaitResponse
import timber.log.Timber

open class BaseWidgetProvider: AppWidgetProvider() {
    init { LoggerUtil().getInstance() }

    companion object {
        const val REFRESH_BUTTON_CLICKED = "app.airsignal.weather.view.widget.REFRESH_DATA"
        const val ENTER_APPLICATION = "app.airsignal.weather.view.widget.ENTER_APP"
        const val ENTER_PERM = "app.airsignal.weather.view.widget.ENTER_PERM"

        const val REFRESH_BUTTON_CLICKED_42 = "app.airsignal.weather.view.widget.REFRESH_DATA42"
        const val ENTER_APPLICATION_42 = "app.airsignal.weather.view.widget.ENTER_APP42"
        const val ENTER_PERM_42 = "app.airsignal.weather.view.widget.ENTER_PERM42"
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Timber.tag(StaticDataObject.TAG_W).i("onDisabled")
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        Timber.tag(StaticDataObject.TAG_W).i("onRestored")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Timber.tag(StaticDataObject.TAG_W).i("onDeleted")
    }

    suspend fun requestWeather(lat: Double, lng: Double): ApiModel.WidgetData? {
        return  HttpClient.getInstance(true).setClientBuilder()
            .getWidgetForecast(lat, lng, 4)
            .awaitResponse().body()
    }

     fun getAddress(context: Context, lat: Double, lng: Double): String {
         val loc = GetLocation(context).getAddress(lat, lng)
         val result = AddressFromRegex(loc).getNotificationAddress()
         return if (result == "") AddressFromRegex(loc).getSecondAddress() else result
    }

    @SuppressLint("BatteryLife")
    fun requestWhitelist(context: Context) {
        val intent = Intent()
        val packageName: String = context.packageName
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
        if (!pm!!.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        } else {
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        }
        context.startActivity(intent)
    }
}