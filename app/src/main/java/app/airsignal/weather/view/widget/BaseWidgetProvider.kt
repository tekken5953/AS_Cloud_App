package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.gps.GetLocation
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.retrofit.HttpClient
import app.airsignal.weather.util.AddressFromRegex
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import retrofit2.awaitResponse
import java.time.LocalDateTime

open class BaseWidgetProvider: AppWidgetProvider() {
    init { LoggerUtil().getInstance() }

    companion object {
        const val REFRESH_BUTTON_CLICKED = "app.airsignal.weather.view.widget.REFRESH_DATA"
        const val ENTER_APPLICATION = "app.airsignal.weather.view.widget.ENTER_APP"

        const val REFRESH_BUTTON_CLICKED_42 = "app.airsignal.weather.view.widget.REFRESH_DATA42"
        const val ENTER_APPLICATION_42 = "app.airsignal.weather.view.widget.ENTER_APP42"
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
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
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
    }

    suspend fun requestWeather(context: Context,lat: Double, lng: Double): ApiModel.WidgetData? {
        try {
            val response = HttpClient.getInstance(true).setClientBuilder()
                .getWidgetForecast(lat, lng, 4)
                .awaitResponse().body()
            RDBLogcat.writeWidgetHistory(context, "data", "data is $response")
            return response
        } catch (e: Exception) {
            RDBLogcat.writeWidgetHistory(context, "error", "weather call error cause ${e.localizedMessage}")
        }
        return null
    }

    fun getAddress(context: Context, lat: Double, lng: Double): String {
        val loc = GetLocation(context).getAddress(lat, lng)
        val result = AddressFromRegex(loc).getNotificationAddress()
        return if (result == "") AddressFromRegex(loc).getSecondAddress() else result
    }

    fun checkBackPerm(context: Context): Boolean {
        return RequestPermissionsUtil(context).isBackgroundRequestLocation()
    }

    fun requestPermissions(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val perm = RequestPermissionsUtil(context)
            if (!perm.isBackgroundRequestLocation()) {
                val intent = Intent(context, WidgetPermActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }

    fun currentIsAfterRealtime(currentTime: String, realTime: String?): Boolean {
        val timeFormed = LocalDateTime.parse(currentTime)
        val realtimeFormed = LocalDateTime.parse(realTime)
        return realtimeFormed?.let { timeFormed.isAfter(it) } ?: true
    }

    fun isDeviceInDozeMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
        return powerManager?.isDeviceIdleMode == true
    }
}