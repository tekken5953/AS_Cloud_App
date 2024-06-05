package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import app.airsignal.weather.R
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.location.GeofenceManager
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.util.VibrateUtil
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.util.`object`.DataTypeParser.convertValueToGrade
import app.airsignal.weather.util.`object`.DataTypeParser.getDataText
import app.airsignal.weather.view.activity.SplashActivity
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import kotlinx.coroutines.*
import java.time.LocalDateTime
import kotlin.math.roundToInt


/**
 * @author : Lee Jae Young
 * @since : 2023-07-04 오후 4:27
 **/
open class WidgetProvider42 : BaseWidgetProvider() {
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
            kotlin.runCatching { processUpdate(context,appWidgetId) }
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

        if (!isRefreshable(context, WIDGET_42)) {
            Toast.makeText(context.applicationContext,"갱신은 1분 주기로 가능합니다", Toast.LENGTH_SHORT).show()
            return
        }

        if (intent.action == REFRESH_BUTTON_CLICKED_42) {
            if (RequestPermissionsUtil(context).isBackgroundRequestLocation())
                processUpdate(context, appWidgetId)
            else requestPermissions(context, WIDGET_42, appWidgetId)
        }
    }

    fun processUpdate(context: Context, appWidgetId: Int?) {
        appWidgetId?.let {
            CoroutineScope(Dispatchers.Default).launch {
                val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
                val refreshBtnIntent =
                    if (RequestPermissionsUtil(context).isBackgroundRequestLocation())
                        Intent(context, WidgetProvider42::class.java).run {
                            this.action = REFRESH_BUTTON_CLICKED_42
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)}
                    else
                        Intent(context, WidgetPermActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("sort",WIDGET_42)
                            putExtra("id",appWidgetId)
                        }
                val enterPending: PendingIntent = Intent(context, SplashActivity::class.java)
                    .run {
                        this.action = ENTER_APPLICATION_42
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
                    this.setOnClickPendingIntent(R.id.w42Background, enterPending)
                    this.setOnClickPendingIntent(R.id.w42Refresh, pendingIntent)
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
                    val data = requestWeather(lat, lng, 4)

                    withContext(Dispatchers.IO) { BaseWidgetProvider().setRefreshTime(context, WIDGET_42) }

                    withContext(Dispatchers.Main) {
                        delay(500)
                        updateUI(context, views, data, addr)
                    }
                }
            }.exceptionOrNull()?.stackTraceToString()
        }
    }

    private fun updateUI(
        context: Context,
        views: RemoteViews,
        data: ApiModel.WidgetData?,
        addr: String?) {
        kotlin.runCatching {
            isSuccess = true
            val currentTime = DataTypeParser.currentDateTimeString("HH:mm")
            val sunrise = data?.sun?.sunrise ?: "0600"
            val sunset = data?.sun?.sunset ?: "1800"
            val isNight = GetAppInfo.getIsNight(sunrise, sunset)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, this@WidgetProvider42.javaClass)

            views.run {
                views.setImageViewResource(R.id.w42Refresh, R.drawable.w_btn_refresh42)
                this.setTextViewText(R.id.w42Time, currentTime)
                data?.let {
                    this.setTextViewText(R.id.w42Temp, "${it.current.temperature?.roundToInt() ?: 0}˚")
                    this.setTextViewText(R.id.w42Address, addr ?: "")
                    this.setImageViewResource(
                        R.id.w42SkyImg,
                        DataTypeParser.getSkyImgWidget(
                            if (currentIsAfterRealtime(it.current.currentTime, it.realtime[0].forecast))
                                it.current.rainType
                            else it.realtime[0].rainType, it.realtime[0].sky, isNight
                        )
                    )
                    val bg = DataTypeParser.getBackgroundImgWidget(
                        WIDGET_42,
                        rainType =
                        if (currentIsAfterRealtime(it.current.currentTime, it.realtime[0].forecast))
                            it.current.rainType else it.realtime[0].rainType, sky = it.realtime[0].sky, isNight
                    )
                    setInt(R.id.w42Background, "setBackgroundResource", bg)
                    applyColor(context, views, bg)
                    this.setTextViewText(R.id.w42HumidTitle, "습도")
                    this.setTextViewText(R.id.w42Pm10Title, "미세먼지")
                    this.setTextViewText(
                        R.id.w42MinMaxTemp,
                        "${it.today.min?.roundToInt() ?: -1}˚/${it.today.max?.roundToInt() ?: -1}˚"
                    )
                    this.setTextViewText(R.id.w42HumidValue, "${it.current.humidity.roundToInt()}%")
                    this.setTextViewText(
                        R.id.w42Pm10Value,
                        getDataText(context, convertValueToGrade("PM10", it.quality.pm10Value24 ?: 0.0))
                    )
                    for (i in 1..3) {
                        val index = it.realtime[i]
                        val hour = LocalDateTime.parse(index.forecast).hour
                        when (i) {
                            1 -> {
                                this.setTextViewText(R.id.w42DailyTime1, "${hour}시")
                                this.setTextViewText(
                                    R.id.w42DailyTemp1,
                                    "${index.temp?.roundToInt() ?: -1}˚"
                                )
                                this.setImageViewBitmap(
                                    R.id.w42DailySky1, DataTypeParser.applySkyImg(
                                        context,
                                        index.rainType,
                                        index.sky,
                                        it.thunder,
                                        isLarge = false,
                                        isNight = isNight,
                                        lunar = it.lunar?.date ?: -1
                                    )?.toBitmap()
                                )
                            }
                            2 -> {
                                this.setTextViewText(R.id.w42DailyTime2, "${hour}시")
                                this.setTextViewText(
                                    R.id.w42DailyTemp2,
                                    "${index.temp?.roundToInt() ?: -1}˚"
                                )
                                this.setImageViewBitmap(
                                    R.id.w42DailySky2, DataTypeParser.applySkyImg(
                                        context,
                                        index.rainType,
                                        index.sky,
                                        it.thunder,
                                        isLarge = false,
                                        isNight = isNight,
                                        lunar = it.lunar?.date ?: -1
                                    )?.toBitmap()
                                )
                            }
                            3 -> {
                                this.setTextViewText(R.id.w42DailyTime3, "${hour}시")
                                this.setTextViewText(R.id.w42DailyTemp3, "${index.temp?.roundToInt() ?: -1}˚")
                                this.setImageViewBitmap(
                                    R.id.w42DailySky3, DataTypeParser.applySkyImg(
                                        context,
                                        index.rainType,
                                        index.sky,
                                        it.thunder,
                                        isLarge = false,
                                        isNight = isNight,
                                        lunar = it.lunar?.date ?: -1
                                    )?.toBitmap()
                                )
                            }
                        }
                    }
                }
            }

            appWidgetManager.updateAppWidget(componentName, views)
        }.exceptionOrNull()?.stackTraceToString()
    }

    private fun applyColor(context: Context, views: RemoteViews, bg: Int) {
        val textArray = arrayOf(
            R.id.w42Temp,
            R.id.w42Time,
            R.id.w42Address,
            R.id.w42MinMaxTemp,
            R.id.w42DailyTemp1,
            R.id.w42DailyTemp2,
            R.id.w42DailyTemp3,
            R.id.w42DailyTime1,
            R.id.w42DailyTime2,
            R.id.w42DailyTime3,
            R.id.w42HumidTitle,
            R.id.w42HumidValue,
            R.id.w42Pm10Title,
            R.id.w42Pm10Value
        )
        val imgArray = arrayOf(R.id.w42Location, R.id.w42Refresh)
        views.run {
            imgArray.forEach {
                this.setInt(
                    it,"setColorFilter", context.applicationContext.getColor(
                        when (bg) {
                            R.drawable.widget_bg4x2_sunny,
                            R.drawable.widget_bg4x2_snow -> R.color.wblack
                            R.drawable.widget_bg4x2_night,
                            R.drawable.widget_bg4x2_cloud -> R.color.white
                            else -> android.R.color.transparent
                        }
                    )
                )
            }

            textArray.forEach {
                this.setTextColor(
                    it, context.applicationContext.getColor(
                        when (bg) {
                            R.drawable.widget_bg4x2_sunny, R.drawable.widget_bg4x2_snow -> R.color.wblack
                            R.drawable.widget_bg4x2_night, R.drawable.widget_bg4x2_cloud -> R.color.white
                            else -> android.R.color.transparent
                        }
                    )
                )
            }
        }
    }
}