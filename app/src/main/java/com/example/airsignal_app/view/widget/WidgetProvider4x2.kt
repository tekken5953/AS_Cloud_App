package com.example.airsignal_app.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.firebase.db.RDBLogcat.WIDGET_ACTION
import com.example.airsignal_app.firebase.db.RDBLogcat.WIDGET_DOZE_MODE
import com.example.airsignal_app.firebase.db.RDBLogcat.WIDGET_INSTALL
import com.example.airsignal_app.firebase.db.RDBLogcat.WIDGET_UNINSTALL
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient
import com.example.airsignal_app.util.AddressFromRegex
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.util.`object`.SetAppInfo
import com.example.airsignal_app.view.ToastUtils
import com.example.airsignal_app.view.activity.SplashActivity
import com.example.airsignal_app.view.widget.WidgetAction.WIDGET_DELETE
import com.example.airsignal_app.view.widget.WidgetAction.WIDGET_ENABLE
import com.example.airsignal_app.view.widget.WidgetAction.WIDGET_OPTIONS_CHANGED
import com.example.airsignal_app.view.widget.WidgetAction.WIDGET_UPDATE
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import kotlin.math.roundToInt


open class WidgetProvider4x2 : AppWidgetProvider() {

    // 앱 위젯은 여러개가 등록 될 수 있는데, 최초의 앱 위젯이 등록 될 때 호출 됩니다. (각 앱 위젯 인스턴스가 등록 될때마다 호출 되는 것이 아님)
    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Logger.t("testtest").i("On Enable")
    }

    // onEnabled() 와는 반대로 마지막의 최종 앱 위젯 인스턴스가 삭제 될 때 호출 됩니다
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    // android 4.1 에 추가 된 메소드 이며, 앱 위젯이 등록 될 때와 앱 위젯의 크기가 변경 될 때 호출 됩니다.
    // 이때, Bundle 에 위젯 너비/높이의 상한값/하한값 정보를 넘겨주며 이를 통해 컨텐츠를 표시하거나 숨기는 등의 동작을 구현 합니다
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        newOptions.putInt(
            AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
            newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        )
        newOptions.putInt(
            AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
            newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        )
    }

    // 위젯 메타 데이터를 구성 할 때 updatePeriodMillis 라는 업데이트 주기 값을 설정하게 되며, 이 주기에 따라 호출 됩니다.
    // 또한 앱 위젯이 추가 될 떄에도 호출 되므로 Service 와의 상호작용 등의 초기 설정이 필요 할 경우에도 이 메소드를 통해 구현합니다
    @SuppressLint("MissingPermission")
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            Logger.t("testtest").i("On Update")
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)

            // 위젯의 TextView에 업데이트된 텍스트 설정 예시
            if (isRefreshable(context)) {
                if (GetLocation(context).isGPSConnected()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val locationRequest = CurrentLocationRequest.Builder()
                            locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY)

                            val locationResult = withContext(Dispatchers.Default) {
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
            } else {
                ToastUtils(context).showMessage("마지막 갱신 후 1분 뒤에 가능합니다", 1)
            }

            // 위젯을 업데이트합니다.
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // 이 메소드는 앱 데이터가 구글 시스템에 백업 된 이후 복원 될 때 만약 위젯 데이터가 있다면 데이터가 복구 된 이후 호출 됩니다.
    // 일반적으로 사용 될 경우는 흔치 않습니다.
    // 위젯 ID 는 UID 별로 관리 되는데 이때 복원 시점에서 ID 가 변경 될 수 있으므로 백업 시점의 oldID 와 복원 후의 newID 를 전달합니다
    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    // 해당 앱 위젯이 삭제 될 때 호출 됩니다
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
    }

    // 앱의 브로드캐스트를 수신하며 해당 메서드를 통해 각 브로드캐스트에 맞게 메서드를 호출한다.
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Logger.t("testtest").i("On Receive : ${intent.action}")
        intent.action?.let {
            RDBLogcat.writeWidgetPref(
                context,
                sort = WIDGET_ACTION,
                value = intent.action.toString()
            )
            when (it) {
                WIDGET_ENABLE -> {
                    SetAppInfo.setLastRefreshTime(context, 0L)
                    RDBLogcat.writeWidgetPref(context, WIDGET_INSTALL, intent.action.toString() )
                }
                WIDGET_DELETE -> {
                    RDBLogcat.writeWidgetPref(context, WIDGET_UNINSTALL, intent.action.toString() )
                }
                WIDGET_UPDATE -> {
                    val ids = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, WidgetProvider4x2::class.java))
                    onUpdate(context, AppWidgetManager.getInstance(context), ids)
                }
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_SCREEN_ON -> {
                    RDBLogcat.writeWidgetPref(context, WIDGET_DOZE_MODE, intent.action.toString())
                    getWidgetLocation(context)
                }
                else -> {}
            }
        }
    }

    private fun isRefreshable(context: Context): Boolean {
        return getCurrentTime() - GetAppInfo.getLastRefreshTime(context) >= 1000 * 60
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

    private fun <T> failToFetchData(context: Context, t: T, title: String) {

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
        refreshBtnIntent.action = WIDGET_UPDATE

        val backgroundPermissionIntent: PendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
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

    @SuppressLint("MissingPermission")
    fun getWidgetLocation(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            LocationServices.getFusedLocationProviderClient(context).run {
                this.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        Logger.t("testtest").i("get Location : ${Thread.currentThread().name}")
                        loadWidgetData(context, location.latitude, location.longitude)
                    }
                    .addOnFailureListener { e ->
                        RDBLogcat.writeErrorNotANR(
                            context,
                            sort = RDBLogcat.WIDGET_ERROR,
                            msg = e.localizedMessage!!
                        )
                    }
                    .addOnCanceledListener {
                        RDBLogcat.writeErrorNotANR(
                            context,
                            sort = RDBLogcat.WIDGET_ERROR,
                            msg = "Location is Not Available"
                        )
                    }
            }
        }
    }

//    @SuppressLint("MissingPermission")
//    fun getWidgetLocation(context: Context) {
//        if (GetLocation(context).isGPSConnected()) {
//            CoroutineScope(Dispatchers.Main).launch {
//                try {
//                    val locationRequest = CurrentLocationRequest.Builder()
//                    locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
//
//                    val locationResult = withContext(Dispatchers.IO) {
//                        LocationServices.getFusedLocationProviderClient(context)
//                            .getCurrentLocation(locationRequest.build(), null)
//                    }.await()
//
//                    val result = locationResult ?: throw Exception("Location not available")
//                    loadWidgetData(context, result.latitude, result.longitude)
//                } catch (e: Exception) {
//                    RDBLogcat.writeErrorNotANR(
//                        context,
//                        sort = RDBLogcat.WIDGET_ERROR,
//                        msg = "Location is Not Available"
//                    )
//                }
//            }
//        }
//    }

    private fun loadWidgetData(context: Context, lat: Double, lng: Double) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)

        @RequiresApi(Build.VERSION_CODES.Q)
        if (!RequestPermissionsUtil(context).isBackgroundRequestLocation()) {
            changeVisibility(context, views, true)
        } else {
            changeVisibility(context, views, false)
        }

        GetLocation(context).getAddress(lat, lng)?.let { addr ->

            SetAppInfo.setLastRefreshTime(context, getCurrentTime())

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
                                    DataTypeParser.modifyCurrentRainType(
                                        current.rainType,
                                        realtime.rainType
                                    ),
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

                                    SetAppInfo.setLastRefreshTime(context, getCurrentTime())

                                    setTextViewText(
                                        R.id.widget4x2TempValue,
                                        "${
                                            DataTypeParser.modifyCurrentTempType(
                                                current.temperature,
                                                realtime.temp
                                            )
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
                                    val regexAddr = if (getRegexAddr(rawAddr) == StaticDataObject.IN_COMPLETE_ADDRESS) {
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
                                failToFetchData(context, e, "onResponse - catch")
                                return
                            }
                        } else {
                            failToFetchData(context, response.errorBody(), "onResponse - Failed")
                            call.cancel()
                            return
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiModel.Widget4x2Data>,
                        t: Throwable
                    ) {
                        failToFetchData(context, t, "onFailure")
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