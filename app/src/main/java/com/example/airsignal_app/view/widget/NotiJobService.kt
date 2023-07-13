package com.example.airsignal_app.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import androidx.core.os.HandlerCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.TAG_W
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.DataTypeParser.modifyCurrentRainType
import com.example.airsignal_app.util.`object`.DataTypeParser.modifyCurrentTempType
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.view.activity.RedirectPermissionActivity
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.time.LocalDateTime
import java.util.*
import kotlin.math.roundToInt

/**
 * @author : Lee Jae Young
 * @since : 2023-07-11 오전 9:21
 **/
@SuppressLint("SpecifyJobSchedulerIdRange")
class NotiJobService : JobService() {
    private val context = this@NotiJobService
    private val filter = IntentFilter(Intent.ACTION_SCREEN_ON)

    @SuppressLint("MissingPermission")
    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.tag(TAG_W).d("onStartJob : ${params!!.jobId}")

        HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
            getWidgetLocation(context)

            context.registerReceiver(WidgetProvider4x2.NotiJobScheduler(), filter)
        },2000)
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Timber.tag(TAG_W).d("onStopJob : ${p0?.jobId}")
        writeLog(false, "JobScheduler 정지", "onStopJob : ${p0?.jobId}")
        context.unregisterReceiver(WidgetProvider4x2.NotiJobScheduler())
        if (!WidgetProvider4x2().isJobScheduled(context)) {
            WidgetProvider4x2.NotiJobScheduler().scheduleJob(context)
        }

        return true
    }

    private fun changeVisibility(context: Context, views: RemoteViews, isReload: Boolean) {
        val list = listOf(
            R.id.widget4x2Address,
            R.id.widget4x2PmValue,
            R.id.widget4x2RainPerValue,
            R.id.widget4x2AddressVector,
            R.id.widget4x2RainPer,
            R.id.widget4x2PmIndex,
            R.id.widget4x2Refresh,
            R.id.widget4x2VerticalLine,
            R.id.widget4x2SkyImg,
            R.id.widget4x2TempValue,
            R.id.widget4x2TempIndex
        )
        if (isReload) {
            list.forEach {
                views.setViewVisibility(
                    it,
                    View.GONE
                )
            }
            views.setViewVisibility(
                R.id.widget4x2ReloadLayout,
                View.VISIBLE
            )

            fetch(context, views)
        } else {
            list.forEach {
                views.setViewVisibility(
                    it,
                    View.VISIBLE
                )
            }
            views.setViewVisibility(
                R.id.widget4x2ReloadLayout,
                View.GONE
            )

            fetch(context, views)
        }
    }

    private fun <T> failToFetchData(context: Context, t: T, views: RemoteViews, title: String) {

        changeVisibility(context, views, true)

        when (t) {
            is Exception -> {
                t.printStackTrace()
                t.localizedMessage?.let { it1 ->
                    writeLog(true, "Error - $title", it1)
                }
            }
            is Throwable -> {
                t.printStackTrace()
                t.localizedMessage?.let { it1 ->
                    writeLog(true, "Error - $title", it1)
                }
            }
            else -> {
                writeLog(true, "Error - $title", t.toString())
            }
        }
    }

    fun writeLog(isANR: Boolean, s1: String?, s2: String?) {
        if (isANR) {
            RDBLogcat.writeLogCause(
                "ANR 발생",
                s1!!,
                s2!!
            )
        } else {
            RDBLogcat.writeLogCause(
                "Widget",
                s1!!,
                s2!!
            )
        }
    }

    private fun fetch(context: Context, views: RemoteViews) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName =
            ComponentName(context, WidgetProvider4x2::class.java)

        val refreshBtnIntent = Intent(context, WidgetProvider4x2::class.java)
        refreshBtnIntent.action = WidgetAction.WIDGET_UPDATE
        val pendingRefresh: PendingIntent =
            PendingIntent.getBroadcast(context, 0,
                refreshBtnIntent, PendingIntent.FLAG_IMMUTABLE)

        val pendingIntent: PendingIntent = Intent(context, RedirectPermissionActivity::class.java)
            .let { intent ->
                intent.action = "enterApplication"
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }

        views.apply {
            setOnClickPendingIntent(R.id.widget4x2MainLayout, pendingIntent)
            setOnClickPendingIntent(R.id.widget4x2Refresh, pendingRefresh)
            setOnClickPendingIntent(R.id.widget4x2ReloadLayout, pendingRefresh)
        }

        appWidgetManager.updateAppWidget(componentName, views)
    }

    private fun getIsNight(forecastTime: String, sunRise: String, sunSet: String): Boolean {
        val forecastToday = LocalDateTime.parse(forecastTime)
        val dailyTime =
            DataTypeParser.millsToString(
                DataTypeParser.convertLocalDateTimeToLong(forecastToday),
                "HHmm"
            )
        val dailySunProgress =
            100 * (DataTypeParser.convertTimeToMinutes(dailyTime) - DataTypeParser.convertTimeToMinutes(
                sunRise
            )) / GetAppInfo.getEntireSun(sunRise, sunSet)

        return GetAppInfo.getIsNight(dailySunProgress)
    }

    @SuppressLint("MissingPermission")
    fun getWidgetLocation(context: Context) {
        val locationManager = LocationServices.getFusedLocationProviderClient(context)
        locationManager.getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setDurationMillis(10 * 1000)
                .setMaxUpdateAgeMillis(15 * 60 * 1000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(), null
        )
            .addOnSuccessListener { location ->
                loadWidgetData(context, location.latitude, location.longitude)
            }
            .addOnFailureListener { e ->
                writeLog(false, "addOnFailureListener", e.localizedMessage)
            }
            .addOnCanceledListener {
                writeLog(false, "addOnCanceledListener", "Location is Not Available")
            }
            .addOnCompleteListener { task ->
                writeLog(
                    false, "addOnCompleteListener", "task isSuccess ${task.isSuccessful} " +
                            "result is ${task.result}"
                )
            }
    }

    private fun loadWidgetData(context: Context, lat: Double, lng: Double) {
        val httpClient = HttpClient.getInstance(true).setClientBuilder()
        writeLog(false, "Get Instance", httpClient.toString())
        val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)

        GetLocation(context).getAddress(lat, lng)?.let { addr ->
            writeLog(false, "Address", addr)
            RDBLogcat.writeLogCause(
                "Widget",
                "Address",
                addr
            )

            SharedPreferenceManager(context).setLong("lastWidgetDataCall", getCurrentTime())

            changeVisibility(context, views, false)

            GetLocation(context).updateCurrentAddress(
                lat, lng, addr
            )

            val getDataResponse: Call<ApiModel.Widget4x2Data> =
                httpClient.mMyAPIImpl.getWidgetForecast(
                    lat, lng, 1
                )

            getDataResponse.enqueue(object :
                Callback<ApiModel.Widget4x2Data> {
                override fun onResponse(
                    call: Call<ApiModel.Widget4x2Data>,
                    response: Response<ApiModel.Widget4x2Data>
                ) {
                    if (response.isSuccessful) {
                        try {
                            writeLog(
                                false, "Success Call Data",
                                response.body().toString()
                            )
                            val body = response.body()
                            val data = body!!
                            val current = data.current
                            val thunder = data.thunder
                            val sun = data.sun
                            val realtime = data.realtime[0]
                            val skyText = DataTypeParser.applySkyText(
                                context,
                                modifyCurrentRainType(current.rainType, realtime.rainType),
                                realtime.sky,
                                thunder
                            )

                            views.apply {
                                setViewVisibility(R.id.widget4x2ReloadLayout, View.GONE)

                                setInt(
                                    R.id.widget4x2MainLayout, "setBackgroundResource",
                                    DataTypeParser.getSkyImgWidget(
                                        skyText,
                                        GetAppInfo.getCurrentSun(
                                            sun.sunrise!!,
                                            sun.sunset!!
                                        )
                                    )
                                )

                                setTextViewText(
                                    R.id.widget4x2Time,
                                    DataTypeParser.millsToString(
                                        DataTypeParser.getCurrentTime(),
                                        "HH시 mm분"
                                    )
                                )

                                setTextViewText(
                                    R.id.widget4x2TempValue,
                                    "${
                                        modifyCurrentTempType(current.temperature, realtime.temp)
                                            .roundToInt()
                                    }˚"
                                )

                                setTextViewText(
                                    R.id.widget4x2RainPerValue,
                                    "${realtime.rainP!!.toInt()}%"
                                )

                                setTextViewText(
                                    R.id.widget4x2PmValue,
                                    DataTypeParser.getDataText(data.quality.pm10Grade1h!!)
                                        .trim()
                                )

                                setTextViewText(R.id.widget4x2TempIndex, skyText)

                                setImageViewBitmap(
                                    R.id.widget4x2SkyImg,
                                    (DataTypeParser.getSkyImgLarge(
                                        context, skyText,
                                        getIsNight(
                                            forecastTime = realtime.forecast!!,
                                            sunRise = sun.sunrise,
                                            sunSet = sun.sunset
                                        )
                                    )
                                            as BitmapDrawable).bitmap
                                )

                                setTextViewText(
                                    R.id.widget4x2Address,
                                    GetAppInfo.getNotificationAddress(context).trim()
                                )

                                fetch(context, views)
                            }
                        } catch (e: Exception) {
                            failToFetchData(
                                context,
                                e,
                                views,
                                "onResponse - catch\n${call.request()}"
                            )
                            return
                        }
                    } else {
                        failToFetchData(
                            context,
                            response.errorBody(),
                            views,
                            "onResponse - Failed\n" +
                                    "${call.request()}"
                        )
                        call.cancel()
                        return
                    }
                }

                override fun onFailure(
                    call: Call<ApiModel.Widget4x2Data>,
                    t: Throwable
                ) {
                    failToFetchData(
                        context, t, views, "onFailure\n" +
                                "${call.request()}"
                    )
                    call.cancel()
                    return
                }
            })
        }
    }
}