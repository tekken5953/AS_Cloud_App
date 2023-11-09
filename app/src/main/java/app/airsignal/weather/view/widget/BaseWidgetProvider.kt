package app.airsignal.weather.view.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
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
        Timber.tag(StaticDataObject.TAG_W).i("onAppWidgetOptionsChanged")
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
            .getWidgetForecast(lat, lng, 1)
            .awaitResponse().body()
    }

     fun getAddress(context: Context, lat: Double, lng: Double): String {
        val loc = GetLocation(context).getAddress(lat, lng)
        return AddressFromRegex(loc).getNotificationAddress()
    }
}