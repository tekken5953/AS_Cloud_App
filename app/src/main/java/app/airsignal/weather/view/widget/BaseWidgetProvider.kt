package app.airsignal.weather.view.widget

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import app.airsignal.weather.api.retrofit.ApiModel
import app.airsignal.weather.api.retrofit.HttpClient
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.SetAppInfo
import app.airsignal.weather.utils.DataTypeParser.getCurrentTime
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.awaitResponse
import java.time.LocalDateTime

open class BaseWidgetProvider: AppWidgetProvider(), KoinComponent {

    companion object {
        const val REFRESH_BUTTON_CLICKED = "app.airsignal.weather.view.widget.REFRESH_DATA"
        const val ENTER_APPLICATION = "app.airsignal.weather.view.widget.ENTER_APP"

        const val REFRESH_BUTTON_CLICKED_42 = "app.airsignal.weather.view.widget.REFRESH_DATA42"
        const val ENTER_APPLICATION_42 = "app.airsignal.weather.view.widget.ENTER_APP42"

        const val WIDGET_42 = "42"
        const val WIDGET_22 = "22"
    }

    private val httpClient: HttpClient by inject()

    suspend fun requestWeather(lat: Double, lng: Double, rCount: Int): ApiModel.WidgetData? =
        kotlin.runCatching {
            httpClient.retrofit
                .getWidgetForecast(lat, lng, rCount)
                .awaitResponse().body()
        }.getOrNull()

    fun requestPermissions(context: Context, sort: String, id: Int?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        if (RequestPermissionsUtil(context).isBackgroundRequestLocation()) return

        val intent = Intent(context, WidgetPermActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("sort",sort)
            putExtra("id",id)
        }
        context.startActivity(intent)
    }

    fun currentIsAfterRealtime(currentTime: String, realTime: String?): Boolean =
        LocalDateTime.parse(realTime)?.let {LocalDateTime.parse(currentTime).isAfter(it)} ?: true

    fun isRefreshable(type: String): Boolean {
        val currentTime = getCurrentTime()
        val lastRefresh = when (type) {
            WIDGET_42 -> GetAppInfo.getLastRefreshTime42()
            WIDGET_22 -> GetAppInfo.getLastRefreshTime22()
            else -> currentTime
        }
        return currentTime - lastRefresh >= 1000 * 60
    }

    fun setRefreshTime(type: String) {
        if (type == WIDGET_42) SetAppInfo.setLastRefreshTime42(getCurrentTime())
        else if (type == WIDGET_22) SetAppInfo.setLastRefreshTime22(getCurrentTime())
    }
}