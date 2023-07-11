package com.example.airsignal_app.view.widget

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.RemoteViews
import com.example.airsignal_app.R
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.GetAppInfo
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
    @SuppressLint("MissingPermission")
    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.tag("JobServices").d("onStartJob")

        val httpClient = HttpClient.getInstance(true).setClientBuilder()
        val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
        val getLocation = GetLocation(context)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                loc.let { location ->
                    Timber.tag("JobServices").d("addOnSuccess : ${Date(location.time).time}")
                    getLocation.getAddress(location.latitude, location.longitude)?.let { addr ->
                        Timber.tag("JobServices").d("get Address : $addr")
                        RDBLogcat.writeLogCause(
                            "Widget",
                            "Address",
                            addr
                        )
                        changeVisibility(context, views, false)

                        getLocation.updateCurrentAddress(
                            location.latitude,
                            location.longitude,
                            addr
                        )

                        val getDataResponse: Call<ApiModel.Widget4x2Data> =
                            httpClient.mMyAPIImpl.getWidgetForecast(
                                location.latitude,
                                location.longitude,
                                1
                            )

                        getDataResponse.enqueue(object :
                            Callback<ApiModel.Widget4x2Data> {
                            override fun onResponse(
                                call: Call<ApiModel.Widget4x2Data>,
                                response: Response<ApiModel.Widget4x2Data>
                            ) {
                                if (response.isSuccessful) {
                                    try {
                                        RDBLogcat.writeLogCause(
                                            "Widget",
                                            "Success Call Data",
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
                                            current.rainType!!,
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
                                                "${current.temperature!!.roundToInt()}˚"
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
                            }
                        })
                    }
                }
            }
            .addOnFailureListener { e ->
                failToFetchData(context, e, views, "addOnFailureListener")
            }
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Timber.tag("JobServices").d("onStopJob")

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
                    RDBLogcat.writeLogCause(
                        "Widget",
                        "Error - $title",
                        it1
                    )
                }
            }
            is Throwable -> {
                t.printStackTrace()
                t.localizedMessage?.let { it1 ->
                    RDBLogcat.writeLogCause(
                        "ANR 발생",
                        "Error - $title",
                        it1
                    )
                }
            }
            else -> {
                RDBLogcat.writeLogCause(
                    "ANR 발생",
                    "Error - $title",
                    t.toString()
                )
            }
        }
    }

    private fun fetch(context: Context, views: RemoteViews) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName =
            ComponentName(context, WidgetProvider4x2::class.java)
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
}