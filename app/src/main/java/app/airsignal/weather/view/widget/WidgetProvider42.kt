package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import app.airsignal.weather.R
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.koin.BaseApplication.Companion.getAppContext
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.util.`object`.DataTypeParser.convertValueToGrade
import app.airsignal.weather.util.`object`.DataTypeParser.getDataText
import app.airsignal.weather.util.`object`.GetAppInfo
import app.airsignal.weather.view.activity.SplashActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
        val appContext = getAppContext()
        val views = RemoteViews(appContext.packageName, R.layout.widget_layout_4x2)
        fetch(appContext,views)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val appContext = getAppContext()
        for (appWidgetId in appWidgetIds) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
                val refreshBtnIntent = Intent(appContext, WidgetProvider42::class.java).run {
                    this.action = REFRESH_BUTTON_CLICKED_42
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }

                val enterPending: PendingIntent = Intent(appContext, SplashActivity::class.java)
                    .run {
                        this.action = ENTER_APPLICATION_42
                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
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
                RDBLogcat.writeWidgetHistory(context, "lifecycle", "onUpdate42")
                views.run {
                    this.setOnClickPendingIntent(R.id.w42Refresh, pendingIntent)
                    this.setOnClickPendingIntent(R.id.w42Background, enterPending)
                    retryFetch(context.applicationContext,appContext,this)
                }
            } catch (e: Exception) {
                RDBLogcat.writeErrorANR(
                    "Error",
                    "onUpdate error42 ${e.stackTraceToString()}"
                )
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val appContext = getAppContext()
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (context != null) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
            if (appWidgetId != null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                if (intent.action == REFRESH_BUTTON_CLICKED_42) {
                    requestPermissions(context)
                    views.setImageViewResource(R.id.w42Refresh, R.drawable.w_refreshing42)
                    retryFetch(context.applicationContext,appContext,views)
                    AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId,views)
                }
            }
        }
    }

    private fun retryFetch(context: Context, appContext: Context, views: RemoteViews) {
        val componentName =
            ComponentName(context, this@WidgetProvider42.javaClass)
        fetch(context.applicationContext, views)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isSuccess) {
                views.setImageViewResource(R.id.widget2x2Refresh, R.drawable.w_btn_refresh)
                fetch(appContext, views)
                RDBLogcat.writeWidgetHistory(context, "retry fetch42", "isSuccess is $isSuccess")
                AppWidgetManager.getInstance(context).updateAppWidget(componentName,views)
            }
        }, 3000)
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

                        RDBLogcat.writeWidgetHistory(context, "위치","data42 is $data")
                        withContext(Dispatchers.Main) {
                            delay(500)
                            updateUI(context, views, data, addr)
                        }
                    }
                }
            }
            val onFailure: (e: Exception) -> Unit = {
                RDBLogcat.writeErrorANR("Error", "widget error42 ${it.localizedMessage}")
            }
            CoroutineScope(Dispatchers.Default).launch {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(onFailure)
                    .addOnCanceledListener {
                        RDBLogcat.writeErrorANR(
                            Thread.currentThread().toString(),
                            "addOnCanceledListener42 is $resultData"
                        )
                    }
            }
        } catch(e: Exception) {
            RDBLogcat.writeErrorANR("Error", "fetch error42 ${e.localizedMessage}")
        }
    }

    private fun updateUI(
        context: Context,
        views: RemoteViews,
        data: ApiModel.WidgetData?,
        addr: String?
    ) {
        try {
            isSuccess = true
            val currentTime = DataTypeParser.currentDateTimeString("HH:mm")
            val sunrise = data?.sun?.sunrise ?: "0000"
            val sunset = data?.sun?.sunset ?: "0000"
            val isNight = GetAppInfo.getIsNight(sunrise, sunset)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName =
                ComponentName(context, this@WidgetProvider42.javaClass)

            views.run {
                views.setImageViewResource(R.id.w42Refresh, R.drawable.w_btn_refresh42)
                this.setTextViewText(R.id.w42Time, currentTime)
                data?.let {
                    this.setTextViewText(R.id.w42Temp, "${it.current.temperature?.roundToInt() ?: 0}˚")
                    this.setTextViewText(R.id.w42Address, addr ?: "")
                    this.setImageViewResource(R.id.w42SkyImg,
                        DataTypeParser.getSkyImgWidget(it.current.rainType, it.realtime[0].sky, isNight)
                    )
                    val bg = DataTypeParser.getBackgroundImgWidget(
                        "42",
                        rainType = it.current.rainType,
                        sky = it.realtime[0].sky, isNight
                    )
                    setInt(R.id.w42Background, "setBackgroundResource", bg)
                    applyColor(context, views, bg)
                    this.setTextViewText(R.id.w42HumidTitle, "습도")
                    this.setTextViewText(R.id.w42Pm10Title, "미세먼지")
                    this.setTextViewText(R.id.w42Pm25Title, "초미세먼지")
                    this.setTextViewText(
                        R.id.w42MinMaxTemp,
                        "${it.today.min?.roundToInt() ?: -1}˚/${it.today.max?.roundToInt() ?: -1}˚"
                    )
                    this.setTextViewText(R.id.w42HumidValue, "${it.current.humidity.roundToInt()}%")
                    this.setTextViewText(
                        R.id.w42Pm10Value,
                        getDataText(
                            context, convertValueToGrade("PM10", it.quality.pm10Value24 ?: 0.0)
                        )
                    )
                    this.setTextViewText(
                        R.id.w42Pm25Value,
                        getDataText(
                            context,
                            convertValueToGrade("PM2.5", it.quality.pm25Value24?.toDouble() ?: 0.0)
                        )
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
                                    )!!.toBitmap()
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
                                    )!!.toBitmap()
                                )
                            }
                            3 -> {
                                this.setTextViewText(R.id.w42DailyTime3, "${hour}시")
                                this.setTextViewText(
                                    R.id.w42DailyTemp3,
                                    "${index.temp?.roundToInt() ?: -1}˚"
                                )
                                this.setImageViewBitmap(
                                    R.id.w42DailySky3, DataTypeParser.applySkyImg(
                                        context,
                                        index.rainType,
                                        index.sky,
                                        it.thunder,
                                        isLarge = false,
                                        isNight = isNight,
                                        lunar = it.lunar?.date ?: -1
                                    )!!.toBitmap()
                                )
                            }
                        }
                    }
                }
            }

            appWidgetManager.updateAppWidget(componentName, views)
        } catch (e: Exception) {
            RDBLogcat.writeErrorANR("Error", "updateUI error42 ${e.localizedMessage}")
        }
    }

    private fun applyColor(context: Context, views: RemoteViews, bg: Int) {
        val textArray = arrayOf(R.id.w42Temp, R.id.w42Time, R.id.w42Address,R.id.w42MinMaxTemp,
            R.id.w42DailyTemp1,R.id.w42DailyTemp2,R.id.w42DailyTemp3,R.id.w42DailyTime1,R.id.w42DailyTime2
            ,R.id.w42DailyTime3,R.id.w42HumidTitle,R.id.w42HumidValue,R.id.w42Pm10Title,R.id.w42Pm10Value
            ,R.id.w42Pm25Title,R.id.w42Pm25Value)
        val imgArray = arrayOf(R.id.w42Location,R.id.w42Refresh)
        views.run {
            imgArray.forEach {
                this.setInt(
                    it, "setColorFilter", context.applicationContext.getColor(
                        when (bg) {
                            R.drawable.widget_bg4x2_sunny, R.drawable.widget_bg4x2_snow -> { R.color.wblack }
                            R.drawable.widget_bg4x2_night, R.drawable.widget_bg4x2_cloud -> { R.color.white }
                            else -> android.R.color.transparent
                        }
                    )
                )
            }

            textArray.forEach {
                this.setTextColor(
                    it, context.applicationContext.getColor(
                        when (bg) {
                            R.drawable.widget_bg4x2_sunny, R.drawable.widget_bg4x2_snow -> { R.color.wblack }
                            R.drawable.widget_bg4x2_night, R.drawable.widget_bg4x2_cloud -> { R.color.white }
                            else -> android.R.color.transparent
                        }
                    )
                )
            }
        }
    }
}