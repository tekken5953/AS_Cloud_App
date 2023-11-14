package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.widget.RemoteViews
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.dao.StaticDataObject.TAG_W
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.util.`object`.GetAppInfo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-07-04 오후 4:27
 **/
open class WidgetProvider42 : BaseWidgetProvider() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val appContext = context.applicationContext
        Timber.tag(TAG_W).i("onEnabled")
        val views = RemoteViews(appContext.packageName, R.layout.widget_layout_4x2)
//        fetch(appContext,views)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                RDBLogcat.writeWidgetHistory(context.applicationContext,"error", e.stackTraceToString())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetch(context: Context, views: RemoteViews) {
        try {
            val appContext = context.applicationContext
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
            val onSuccess: (Location?) -> Unit = { location ->
                CoroutineScope(Dispatchers.Default).launch {
                    location?.let { loc ->
                        val lat = loc.latitude
                        val lng = loc.longitude
                        val data = requestWeather(lat, lng)
                        val addr = getAddress(appContext, lat, lng)

                        RDBLogcat.writeWidgetHistory(appContext, "위치", "data : $data")

                        withContext(Dispatchers.Main) {
                            delay(1000)
                            updateUI(appContext, views, data, addr)
                        }
                    }
                }
            }
            val onFailure: (e: Exception) -> Unit = {
                Timber.tag(StaticDataObject.TAG_W).e(it.stackTraceToString())
                RDBLogcat.writeWidgetHistory(appContext, "widget error", it.localizedMessage)
            }
            CoroutineScope(Dispatchers.Default).launch {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(onFailure)
                    .addOnCanceledListener {
                        RDBLogcat.writeWidgetHistory(appContext, "addOnCanceledListener", resultData.toString())
                    }
            }
        } catch(e: Exception) {
            RDBLogcat.writeWidgetHistory(context.applicationContext,"fetch error",e.localizedMessage)
        }
    }

    private fun updateUI(
        context: Context,
        views: RemoteViews,
        data: ApiModel.WidgetData?,
        addr: String?
    ) {
        try {
            val appContext = context.applicationContext
            val currentTime = DataTypeParser.currentDateTimeString("HH:mm")
            val sunrise = data?.sun?.sunrise ?: "0000"
            val sunset = data?.sun?.sunset ?: "0000"
            val isNight = GetAppInfo.getIsNight(sunrise,sunset)
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val componentName =
                ComponentName(appContext, this@WidgetProvider42.javaClass)

            views.run {
                views.setImageViewResource(R.id.widget2x2Refresh, R.drawable.w_btn_refresh)
                this.setTextViewText(R.id.widget2x2Time, currentTime)
                data?.let {
                    this.setTextViewText(
                        R.id.widget2x2TempValue,"${it.current.temperature ?: 0}˚")
                    this.setTextViewText(R.id.widget2x2Address, addr ?: "")
                    this.setImageViewResource(R.id.widget2x2SkyImg,
                        DataTypeParser.getSkyImgWidget(
                            it.current.rainType,
                            it.realtime[0].sky,
                            isNight
                        )
                    )
                    val bg = DataTypeParser.getBackgroundImgWidget(
                        rainType = it.current.rainType,
                        sky = it.realtime[0].sky, isNight
                    )
                    setInt(R.id.widget2x2Background, "setBackgroundResource", bg)
                    applyColor(appContext,views,bg)
                }
            }

            appWidgetManager.updateAppWidget(componentName, views)
        } catch (e: Exception) {
            RDBLogcat.writeWidgetHistory(context.applicationContext, "updateUI error", e.stackTraceToString())
            Timber.tag(StaticDataObject.TAG_W).e(e.stackTraceToString())
        }
    }

    private fun applyColor(context: Context,views: RemoteViews, bg: Int) {
        val textArray = arrayOf(R.id.widget2x2TempValue, R.id.widget2x2Time, R.id.widget2x2Address)
        val imgArray = arrayOf(R.id.widget2x2Refresh)
        views.run {
            imgArray.forEach {
                this.setInt(
                    it, "setColorFilter", context.applicationContext.getColor(
                        when (bg) {
                            R.drawable.w_bg_sunny, R.drawable.w_bg_snow -> { R.color.black }
                            R.drawable.w_bg_night, R.drawable.w_bg_cloudy -> { R.color.white }
                            else -> android.R.color.transparent
                        }
                    )
                )
            }

            textArray.forEach {
                this.setTextColor(
                    it, context.applicationContext.getColor(
                        when (bg) {
                            R.drawable.w_bg_sunny, R.drawable.w_bg_snow -> { R.color.black }
                            R.drawable.w_bg_night, R.drawable.w_bg_cloudy -> { R.color.white }
                            else -> android.R.color.transparent
                        }
                    )
                )
            }
        }
    }
}