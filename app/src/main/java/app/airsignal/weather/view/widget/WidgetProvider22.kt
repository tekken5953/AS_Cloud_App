package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.airsignal.weather.R
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.location.GeofenceManager
import app.airsignal.weather.api.retrofit.ApiModel
import app.airsignal.weather.utils.VibrateUtil
import app.airsignal.weather.utils.DataTypeParser
import app.airsignal.weather.utils.DataTypeParser.getBackgroundImgWidget
import app.airsignal.weather.utils.DataTypeParser.getSkyImgWidget
import app.airsignal.weather.utils.plain.ToastUtils
import app.airsignal.weather.view.activity.SplashActivity
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import kotlinx.coroutines.*
import kotlin.math.roundToInt


/**
 * @author : Lee Jae Young
 * @since : 2023-07-04 오후 4:27
 **/
open class WidgetProvider22 : BaseWidgetProvider() {
    private var isSuccess = false

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        processUpdate(context, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            kotlin.runCatching {
                processUpdate(context, appWidgetId)
            }.exceptionOrNull()?.stackTraceToString()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (context == null) return

        if (intent == null) return

        if (appWidgetId == null) return

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            processUpdate(context, appWidgetId)
            return
        }

        if (!isRefreshable(WIDGET_22)) {
            ToastUtils(context).showMessage(context.getString(R.string.widget_not_refreshable))
            return
        }

        if (intent.action == REFRESH_BUTTON_CLICKED) {
            if (RequestPermissionsUtil(context).isBackgroundRequestLocation())
                processUpdate(context, appWidgetId)
            else requestPermissions(context,WIDGET_22, appWidgetId)
        }
    }

    fun processUpdate(context: Context, appWidgetId: Int?) {
        appWidgetId?.let {
            CoroutineScope(Dispatchers.Default).launch {
                val views = RemoteViews(context.packageName, R.layout.widget_layout_2x2)
                val refreshBtnIntent =
                    if (RequestPermissionsUtil(context).isBackgroundRequestLocation())
                        Intent(context, WidgetProvider22::class.java).run {
                            this.action = REFRESH_BUTTON_CLICKED
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                    else
                        Intent(context, WidgetPermActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("sort", WIDGET_22)
                            putExtra("id", appWidgetId)
                        }

                val enterPending: PendingIntent = Intent(context, SplashActivity::class.java)
                    .run {
                        this.action = ENTER_APPLICATION
                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        PendingIntent.getActivity(context, appWidgetId, this, PendingIntent.FLAG_IMMUTABLE)
                    }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    refreshBtnIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                views.run {
                    this.setOnClickPendingIntent(R.id.widget2x2Background, enterPending)
                    this.setOnClickPendingIntent(R.id.widget2x2Refresh, pendingIntent)
                }

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    fetch(context, views)
                    VibrateUtil(context).make(10)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetch(context: Context, views: RemoteViews) {
        CoroutineScope(Dispatchers.Default).launch {
            kotlin.runCatching {
                val geofenceLocation = GeofenceManager(context).addGeofence()
                geofenceLocation?.let {
                    val lat = geofenceLocation.latitude
                    val lng = geofenceLocation.longitude
                    val addr = GeofenceManager(context).getSimpleAddress(lat, lng)
                    val data = requestWeather(lat, lng, 1)

                    withContext(Dispatchers.Main) {
                        delay(500)
                        updateUI(context, views, data, addr)
                    }
                    withContext(Dispatchers.IO) {
                        BaseWidgetProvider().setRefreshTime(WIDGET_22)
                    }
                }
            }.exceptionOrNull()?.stackTraceToString()
        }
    }

    private fun updateUI(
        context: Context,
        views: RemoteViews,
        data: ApiModel.WidgetData?,
        addr: String?
    ) {
        kotlin.runCatching {
            isSuccess = true
            val isNight = GetAppInfo.getIsNight(data?.sun?.sunrise ?: "0600",data?.sun?.sunset ?: "1800")

            views.run {
                views.setImageViewResource(R.id.widget2x2Refresh, R.drawable.w_btn_refresh)
                this.setTextViewText(R.id.widget2x2Time, DataTypeParser.currentDateTimeString("HH:mm"))
                data?.let {
                    this.setTextViewText(
                        R.id.widget2x2TempValue,"${it.current.temperature?.roundToInt() ?: 0}˚"
                    )
                    this.setTextViewText(R.id.widget2x2Address, addr ?: "")
                    this.setImageViewResource(
                        R.id.widget2x2SkyImg,
                        getSkyImgWidget(
                            if (currentIsAfterRealtime(it.current.currentTime, it.realtime[0].forecast))
                                it.current.rainType
                            else it.realtime[0].rainType,
                            it.realtime[0].sky, isNight
                        )
                    )
                    val bg = getBackgroundImgWidget(
                        WIDGET_22, rainType =
                        if (currentIsAfterRealtime(it.current.currentTime, it.realtime[0].forecast))
                            it.current.rainType
                        else it.realtime[0].rainType,
                        sky = it.realtime[0].sky, isNight
                    )
                    this.setTextViewText(R.id.widget2x2Pm25Title, "미세먼지")
                    this.setTextViewText(
                        R.id.widget2x2Pm25Value, DataTypeParser.getDataText(
                            context, DataTypeParser.convertValueToGrade(
                                "PM2.5", it.quality.pm25Value24?.toDouble() ?: 0.0)
                        )
                    )
                    setInt(R.id.widget2x2Background, "setBackgroundResource", bg)
                    applyColor(context, views, bg)
                }
            }

            AppWidgetManager.getInstance(context)
                .updateAppWidget(ComponentName(context, this@WidgetProvider22.javaClass), views)
        }.exceptionOrNull()?.stackTraceToString()
    }

    private fun applyColor(context: Context, views: RemoteViews, bg: Int) {
        val textArray = arrayOf(
            R.id.widget2x2TempValue, R.id.widget2x2Time, R.id.widget2x2Address,
            R.id.widget2x2Pm25Title, R.id.widget2x2Pm25Value
        )
        val imgArray = arrayOf(R.id.widget2x2Refresh, R.id.widget2x2LocIv)
        views.run {
            imgArray.forEach {
                this.setInt(
                    it,"setColorFilter", context.applicationContext.getColor(
                        when (bg) {
                            R.drawable.w_bg_sunny, R.drawable.w_bg_snow -> R.color.wblack
                            R.drawable.w_bg_night, R.drawable.w_bg_cloudy -> R.color.white
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