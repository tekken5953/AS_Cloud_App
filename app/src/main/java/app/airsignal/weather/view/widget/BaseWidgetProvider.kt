package app.airsignal.weather.view.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import app.address.AddressFromRegex
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.koin.BaseApplication
import app.airsignal.weather.util.`object`.DataTypeParser.getCurrentTime
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import app.core_databse.db.sp.GetAppInfo
import app.core_databse.db.sp.SetAppInfo
import app.location.GetLocation
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import retrofit2.awaitResponse
import java.time.LocalDateTime

open class BaseWidgetProvider: AppWidgetProvider() {

    companion object {
        const val REFRESH_BUTTON_CLICKED = "app.airsignal.weather.view.widget.REFRESH_DATA"
        const val ENTER_APPLICATION = "app.airsignal.weather.view.widget.ENTER_APP"

        const val REFRESH_BUTTON_CLICKED_42 = "app.airsignal.weather.view.widget.REFRESH_DATA42"
        const val ENTER_APPLICATION_42 = "app.airsignal.weather.view.widget.ENTER_APP42"

    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
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


    suspend fun requestWeather(context: Context,lat: Double, lng: Double, rCount: Int): app.airsignal.core_network.retrofit.ApiModel.WidgetData? {
        try {
            return app.airsignal.core_network.retrofit.HttpClient.getInstance(true).setClientBuilder()
                .getWidgetForecast(lat, lng, rCount)
                .awaitResponse().body()
        } catch (e: Exception) {
            RDBLogcat.writeWidgetHistory(
                context,
                "error",
                "weather call error cause ${e.localizedMessage}"
            )
        }
        return null
    }

    fun checkBackPerm(context: Context): Boolean {
        return RequestPermissionsUtil(context).isBackgroundRequestLocation()
    }

    fun requestPermissions(context: Context, sort: String, id: Int?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val perm = RequestPermissionsUtil(context)
            if (!perm.isBackgroundRequestLocation()) {
                val intent = Intent(context, WidgetPermActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("sort",sort)
                intent.putExtra("id",id)
                context.startActivity(intent)
            }
        }
    }

    fun currentIsAfterRealtime(currentTime: String, realTime: String?): Boolean {
        val timeFormed = LocalDateTime.parse(currentTime)
        val realtimeFormed = LocalDateTime.parse(realTime)
        return realtimeFormed?.let { timeFormed.isAfter(it) } ?: true
    }

    fun isRefreshable(context: Context, type: String): Boolean {
        val currentTime = getCurrentTime()
        val lastRefresh = when (type) {
            "42" -> GetAppInfo.getLastRefreshTime42(context)
            "22" -> GetAppInfo.getLastRefreshTime22(context)
            else -> currentTime
        }
        return currentTime - lastRefresh >= 1000 * 60
    }

    fun setRefreshTime(context: Context, type: String) {
        if (type == "42") {
            SetAppInfo.setLastRefreshTime42(context, getCurrentTime())
        } else if (type == "22") {
            SetAppInfo.setLastRefreshTime22(context, getCurrentTime())
        }
    }

    fun isDeviceInDozeMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
        return powerManager?.isDeviceIdleMode == true
    }
}