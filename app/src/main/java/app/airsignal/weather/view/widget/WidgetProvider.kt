package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.widget.RemoteViews
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject.TAG_W
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.util.`object`.DataTypeParser.currentDateTimeString
import app.airsignal.weather.util.`object`.DataTypeParser.getBackgroundImgWidget
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgWidget
import app.airsignal.weather.util.`object`.GetAppInfo
import app.airsignal.weather.view.activity.SplashActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-07-04 오후 4:27
 **/
open class WidgetProvider : BaseWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            try {
                RDBLogcat.writeWidgetHistory(context.applicationContext, "lifecycle", "onUpdate")
                refresh(context.applicationContext, appWidgetId)
            } catch (e: Exception) {
                RDBLogcat.writeWidgetHistory(context.applicationContext,"error", e.stackTraceToString())
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId != null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
            && intent.action == REFRESH_BUTTON_CLICKED) {
            if (context != null) {
                refresh(context.applicationContext,appWidgetId)
            } else {
                Timber.tag(TAG_W).e("context is null")
            }
        }
    }

    private fun refresh(context: Context, appWidgetId: Int) {
        try {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_2x2)

            val refreshBtnIntent = Intent(context, WidgetProvider::class.java).run {
                this.action = REFRESH_BUTTON_CLICKED
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val enterPending: PendingIntent = Intent(context, SplashActivity::class.java)
                .run {
                    this.action = ENTER_APPLICATION
                    PendingIntent.getActivity(context, appWidgetId, this, PendingIntent.FLAG_IMMUTABLE)
                }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshBtnIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            views.run {
                this.setOnClickPendingIntent(R.id.widget2x2Refresh, pendingIntent)
                this.setOnClickPendingIntent(R.id.widget2x2Background, enterPending)
                fetch(context, this@run)
            }
        } catch (e: Exception) {
            RDBLogcat.writeWidgetHistory(context.applicationContext, "refresh failed", e.stackTraceToString())
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetch(context: Context, views: RemoteViews) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val onSuccess: (Location?) -> Unit = { location ->
                CoroutineScope(Dispatchers.Default).launch {
                    location?.let { loc ->
                        val lat = loc.latitude
                        val lng = loc.longitude
                        val data = requestWeather(lat, lng)
                        val addr = getAddress(context, lat, lng)

                        RDBLogcat.writeWidgetHistory(context, "위치", "data : $data")

                        withContext(Dispatchers.Main) {
                            updateUI(context, views, data, addr)
                        }
                    }
                }
            }
            val onFailure: (e: Exception) -> Unit = {
                Timber.tag(TAG_W).e(it.stackTraceToString())
                RDBLogcat.writeWidgetHistory(context, "widget error", it.localizedMessage)
            }
            CoroutineScope(Dispatchers.Default).launch {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(onFailure)
                    .addOnCanceledListener {
                        RDBLogcat.writeWidgetHistory(context, "addOnCanceledListener", resultData.toString())
                    }
            }
        } catch(e: Exception) {
            RDBLogcat.writeWidgetHistory(context,"fetch error",e.localizedMessage)
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
            val currentTime = currentDateTimeString("HH:mm")
            val sunrise = data?.sun?.sunrise ?: "0000"
            val sunset = data?.sun?.sunset ?: "0000"
            val isNight = GetAppInfo.getIsNight(sunrise,sunset)
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val componentName =
                ComponentName(appContext, this@WidgetProvider.javaClass)

            views.run {
                this.setTextViewText(R.id.widget2x2Time, currentTime)
                data?.let {
                    this.setTextViewText(
                        R.id.widget2x2TempValue,"${it.current.temperature ?: 0}˚")
                    this.setTextViewText(R.id.widget2x2Address, addr ?: "")
                    this.setImageViewResource(R.id.widget2x2SkyImg,
                        getSkyImgWidget(it.current.rainType, it.realtime[0].sky, isNight)
                    )
                    val bg = getBackgroundImgWidget(rainType = it.current.rainType,
                        sky = it.realtime[0].sky, isNight)
                    setInt(R.id.widget2x2Background, "setBackgroundResource", bg)
                    applyColor(appContext,views,bg)
                }
            }

            appWidgetManager.updateAppWidget(componentName, views)
        } catch (e: Exception) {
            RDBLogcat.writeWidgetHistory(context, "updateUI error", e.stackTraceToString())
            Timber.tag(TAG_W).e(e.stackTraceToString())
        }
    }

    private fun applyColor(context: Context,views: RemoteViews, bg: Int) {
        val textArray = arrayOf(R.id.widget2x2TempValue, R.id.widget2x2Time, R.id.widget2x2Address)
        views.run {
            this.setInt(
                R.id.widget2x2Refresh, "setColorFilter", context.getColor(
                    when (bg) {
                        R.drawable.w_bg_sunny, R.drawable.w_bg_snow -> { R.color.black }
                        R.drawable.w_bg_night, R.drawable.w_bg_cloudy -> { R.color.white }
                        else -> android.R.color.transparent
                    }
                )
            )

            textArray.forEach {
                this.setTextColor(
                    it, context.getColor(
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