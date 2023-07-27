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
import android.location.Location
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.IN_COMPLETE_ADDRESS
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient
import com.example.airsignal_app.util.AddressFromRegex
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.DataTypeParser.modifyCurrentRainType
import com.example.airsignal_app.util.`object`.DataTypeParser.modifyCurrentTempType
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.util.`object`.SetAppInfo.setLastRefreshTime
import com.example.airsignal_app.view.activity.SplashActivity
import com.example.airsignal_app.view.widget.WidgetAction.ACTION_DOZE_MODE_CHANGED
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    private val dozeMode = IntentFilter(ACTION_DOZE_MODE_CHANGED)

    @SuppressLint("MissingPermission")
    override fun onStartJob(params: JobParameters?): Boolean {
        getWidgetLocation(context)

        context.registerReceiver(WidgetProvider4x2.NotiJobScheduler(), filter)
        context.registerReceiver(WidgetProvider4x2.NotiJobScheduler(),dozeMode)
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        try {
            context.unregisterReceiver(WidgetProvider4x2.NotiJobScheduler())
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        if (!WidgetProvider4x2.NotiJobScheduler().isJobScheduled(context)) {
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

    private fun <T> failToFetchData(t: T, title: String) {

        when (t) {
            is Exception -> {
                t.printStackTrace()
                t.localizedMessage?.let { it1 ->
                    RDBLogcat.writeErrorNotANR(context,
                    sort = title, msg = it1)
                }
            }
            is Throwable -> {
                t.printStackTrace()
                t.localizedMessage?.let { it1 ->
                    RDBLogcat.writeErrorNotANR(context,
                        sort = title, msg = it1)
                }
            }
            else -> {
                RDBLogcat.writeErrorNotANR(context,
                    sort = title, msg = t.toString())
            }
        }
    }

    private fun fetch(context: Context, views: RemoteViews) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName =
            ComponentName(context, WidgetProvider4x2::class.java)

        val refreshBtnIntent = Intent(context, WidgetProvider4x2::class.java)
        refreshBtnIntent.action = WidgetAction.WIDGET_UPDATE

        val backgroundPermissionIntent: PendingIntent =
            if (VERSION.SDK_INT >= VERSION_CODES.Q &&
                !RequestPermissionsUtil(context).isBackgroundRequestLocation()
            ) {
                Intent(
                    context,
                    BackgroundPermissionActivity::class.java
                )
                    .let { intent ->
                        views.setViewVisibility(R.id.widget4x2Refresh, View.VISIBLE)
                        intent.action = "backgroundPermissionRequest"
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                    }
            } else {
                PendingIntent.getBroadcast(
                    context, 0,
                    refreshBtnIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

        val pendingRefresh: PendingIntent =
            PendingIntent.getBroadcast(
                context, 0,
                refreshBtnIntent, PendingIntent.FLAG_IMMUTABLE
            )

        val pendingIntent: PendingIntent = Intent(context, SplashActivity::class.java)
            .let { intent ->
                intent.action = "enterApplication"
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }

        views.apply {
            setOnClickPendingIntent(R.id.widget4x2MainLayout, pendingIntent)
            setOnClickPendingIntent(R.id.widget4x2Refresh, pendingRefresh)
            setOnClickPendingIntent(R.id.widget4x2ReloadLayout, backgroundPermissionIntent)
        }

        appWidgetManager.updateAppWidget(componentName, views)
    }

    private fun getIsNight(forecastTime: String, sunRise: String, sunSet: String): Boolean {
        val forecastToday = LocalDateTime.parse(forecastTime)
        val dailyTime =
            DataTypeParser.millsToString (
                DataTypeParser.convertLocalDateTimeToLong(forecastToday),
                "HHmm"
            )
        val dailySunProgress =
            100 * (DataTypeParser.convertTimeToMinutes(dailyTime) - DataTypeParser.convertTimeToMinutes(
                sunRise
            )) / GetAppInfo.getEntireSun(sunRise, sunSet)

        return GetAppInfo.getIsNight(dailySunProgress)
    }

//    @SuppressLint("MissingPermission")
//    fun getWidgetLocation(context: Context) {
//        CoroutineScope(Dispatchers.Default).launch {
//            LocationServices.getFusedLocationProviderClient(context).run {
//                this.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
//                    .addOnSuccessListener { location ->
//                        Logger.t("testtest").i("get Location : ${Thread.currentThread().name}")
//                        loadWidgetData(context, location.latitude, location.longitude)
//                    }
//                    .addOnFailureListener { e ->
//                        RDBLogcat.writeErrorNotANR(
//                            context,
//                            sort = RDBLogcat.WIDGET_ERROR,
//                            msg = e.localizedMessage!!
//                        )
//                    }
//                    .addOnCanceledListener {
//                        RDBLogcat.writeErrorNotANR(
//                            context,
//                            sort = RDBLogcat.WIDGET_ERROR,
//                            msg = "Location is Not Available"
//                        )
//                    }
//            }
//        }
//    }

    @SuppressLint("MissingPermission")
    fun getWidgetLocation(context: Context) {
        if (GetLocation(context).isGPSConnected()) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val locationRequest = CurrentLocationRequest.Builder()
                    locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY)

                    val locationResult = withContext(Dispatchers.IO) {
                        LocationServices.getFusedLocationProviderClient(context)
                            .getCurrentLocation(locationRequest.build(), null)
                    }.await()

                    val result = locationResult ?: throw Exception("Location not available")
                    loadWidgetData(context, result.latitude, result.longitude)
                } catch (e: Exception) {
                    RDBLogcat.writeErrorNotANR(
                        context,
                        sort = RDBLogcat.WIDGET_ERROR,
                        msg = "Location is Not Available"
                    )
                }
            }
        }
    }

    private fun loadWidgetData(context: Context, lat: Double, lng: Double) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)

        @RequiresApi(VERSION_CODES.Q)
        if (!RequestPermissionsUtil(context).isBackgroundRequestLocation()) {
            changeVisibility(context, views, true)
        } else {
            changeVisibility(context, views, false)
        }

        GetLocation(context).getAddress(lat, lng)?.let { addr ->

            setLastRefreshTime(context,getCurrentTime())

            GetLocation(context).updateCurrentAddress(
                lat, lng, addr
            )

            CoroutineScope(Dispatchers.Default).launch {

                val getDataResponse: Call<ApiModel.Widget4x2Data> =
                    HttpClient.getInstance(true)
                        .setClientBuilder()
                        .mMyAPIImpl
                        .getWidgetForecast(lat, lng, 1)

                getDataResponse.enqueue(object :
                    Callback<ApiModel.Widget4x2Data> {
                    override fun onResponse(
                        call: Call<ApiModel.Widget4x2Data>,
                        response: Response<ApiModel.Widget4x2Data>
                    ) {
                        if (response.isSuccessful) {
                            try {
                                RDBLogcat.writeWidgetHistory(context,
                                    address = addr, response = response.body().toString())

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
//                                setViewVisibility(R.id.widget4x2ReloadLayout, View.GONE)

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
                                            getCurrentTime(),
                                            "HH시 mm분"
                                        )
                                    )

                                    setLastRefreshTime(context, getCurrentTime())

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

                                    val rawAddr = addr.replace("대한민국", "")
                                    val regexAddr = if (getRegexAddr(rawAddr) == IN_COMPLETE_ADDRESS) {
                                        rawAddr
                                    } else {
                                        getRegexAddr(rawAddr)
                                    }

                                    setTextViewText(
                                        R.id.widget4x2Address,
                                        regexAddr
                                    )

                                    fetch(context, views)
                                }
                            } catch (e: Exception) {
//                            changeVisibility(context, views, true)
                                failToFetchData(e, "onResponse - catch")
                                return
                            }
                        } else {
                            failToFetchData(response.errorBody(), "onResponse - Failed")
                            call.cancel()
                            return
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiModel.Widget4x2Data>,
                        t: Throwable
                    ) {
                        failToFetchData(t, "onFailure")
                        call.cancel()
                        return
                    }
                })
            }
        }
    }

    private fun getRegexAddr(rawAddr: String): String {
        val list = AddressFromRegex(rawAddr).getAddress()?.trim()?.split(" ")

        list?.let {
            if (it.size >= 2) {
                val sb = StringBuilder()
                for (i: Int in it.lastIndex - 1 ..it.lastIndex) {
                    sb.append(it[i]).append(" ")
                    if (i == it.lastIndex) {
                        return sb.toString()
                    }
                }
            } else {
                val sb = StringBuilder()
                sb.append(rawAddr)
                return sb.toString()
            }
        }

        return rawAddr
    }
}