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
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.firebase.fcm.WidgetBuilder
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.util.`object`.DataTypeParser.currentDateTimeString
import app.airsignal.weather.util.`object`.DataTypeParser.getBackgroundImgWidget
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgWidget
import app.airsignal.weather.util.`object`.GetAppInfo
import app.airsignal.weather.view.activity.SplashActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import org.koin.core.context.GlobalContext
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-07-04 오후 4:27
 **/
open class WidgetProvider : BaseWidgetProvider() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val koinContext = GlobalContext.get()
        val appContext = koinContext.get<Context>()
        Timber.tag(TAG_W).i("onEnabled")
        val views = RemoteViews(appContext.packageName, R.layout.widget_layout_2x2)
        fetch(appContext,views)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val koinContext = GlobalContext.get()
        val appContext = koinContext.get<Context>()
        for (appWidgetId in appWidgetIds) {
            try {
                RDBLogcat.writeWidgetHistory(appContext, "lifecycle", "onUpdate")
                val views = RemoteViews(appContext.packageName, R.layout.widget_layout_2x2)
                CoroutineScope(Dispatchers.Default).launch {
                    WidgetBuilder().sendNotification(appContext).run {
                        val refreshBtnIntent = Intent(appContext, WidgetProvider::class.java).run {
                            this.action = REFRESH_BUTTON_CLICKED
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }

                        val enterPending: PendingIntent = Intent(appContext, SplashActivity::class.java)
                            .run {
                                this.action = ENTER_APPLICATION
                                PendingIntent.getActivity(
                                    appContext,
                                    appWidgetId,
                                    this,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            }

                        val pendingIntent = PendingIntent.getBroadcast(
                            appContext,
                            appWidgetId,
                            refreshBtnIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )

                        views.run {
                            this.setOnClickPendingIntent(R.id.widget2x2Refresh, pendingIntent)
                            this.setOnClickPendingIntent(R.id.widget2x2Background, enterPending)
                            appWidgetManager.updateAppWidget(appWidgetId, this)
                            fetch(appContext, this)
                        }
                    }
                }
            } catch (e: Exception) {
                RDBLogcat.writeErrorANR(Thread.currentThread().toString(), "onUpdate error ${e.stackTraceToString()}")
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val koinContext = GlobalContext.get()
        val appContext = koinContext.get<Context>()
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val views = RemoteViews(appContext.packageName, R.layout.widget_layout_2x2)
        if (appWidgetId != null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
            && intent.action == REFRESH_BUTTON_CLICKED) {
            fetch(appContext, RemoteViews(appContext.packageName, R.layout.widget_layout_2x2))
            views.setImageViewResource(R.id.widget2x2Refresh, R.drawable.refreshing)
            AppWidgetManager.getInstance(appContext).updateAppWidget(appWidgetId,views)
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
                            delay(500)
                            updateUI(context, views, data, addr)
                        }
                    }
                }
            }
            val onFailure: (e: Exception) -> Unit = {
                Timber.tag(TAG_W).e(it.stackTraceToString())
                RDBLogcat.writeErrorANR(Thread.currentThread().toString(), "widget error ${it.localizedMessage}")
            }
            CoroutineScope(Dispatchers.Default).launch {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(onFailure)
                    .addOnCanceledListener {
                        RDBLogcat.writeErrorANR(Thread.currentThread().toString(), "addOnCanceledListener - $resultData")
                    }
            }
        } catch(e: Exception) {
            RDBLogcat.writeErrorANR(Thread.currentThread().toString(), "fetch error ${e.localizedMessage}")
        }
    }

    private fun updateUI(
        context: Context,
        views: RemoteViews,
        data: ApiModel.WidgetData?,
        addr: String?
    ) {
        try {
            val currentTime = currentDateTimeString("HH:mm")
            val sunrise = data?.sun?.sunrise ?: "0000"
            val sunset = data?.sun?.sunset ?: "0000"
            val isNight = GetAppInfo.getIsNight(sunrise,sunset)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName =
                ComponentName(context, this@WidgetProvider.javaClass)

            views.run {
                views.setImageViewResource(R.id.widget2x2Refresh, R.drawable.w_btn_refresh)
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
                    applyColor(context,views,bg)
                }
            }

            appWidgetManager.updateAppWidget(componentName, views)
        } catch (e: Exception) {
            RDBLogcat.writeErrorANR(Thread.currentThread().toString(), "updateUI error ${e.stackTraceToString()}")
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
                            R.drawable.w_bg_sunny, R.drawable.w_bg_snow -> { R.color.wblack }
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
                            R.drawable.w_bg_sunny, R.drawable.w_bg_snow -> { R.color.wblack }
                            R.drawable.w_bg_night, R.drawable.w_bg_cloudy -> { R.color.white }
                            else -> android.R.color.transparent
                        }
                    )
                )
            }
        }
    }
}