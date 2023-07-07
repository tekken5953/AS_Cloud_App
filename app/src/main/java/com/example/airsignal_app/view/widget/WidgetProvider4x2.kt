package com.example.airsignal_app.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.TAG_W
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.DataTypeParser.applySkyText
import com.example.airsignal_app.util.`object`.DataTypeParser.currentDateTimeString
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.DataTypeParser.getDataText
import com.example.airsignal_app.util.`object`.DataTypeParser.getSkyImgLarge
import com.example.airsignal_app.util.`object`.DataTypeParser.getSkyImgWidget
import com.example.airsignal_app.util.`object`.GetAppInfo.getCurrentSun
import com.example.airsignal_app.util.`object`.GetAppInfo.getNotificationAddress
import com.example.airsignal_app.util.`object`.GetSystemInfo.getDeviceWidth
import com.example.airsignal_app.view.activity.MainActivity
import com.example.airsignal_app.view.widget.WidgetAction.WIDGET_ENABLE
import com.example.airsignal_app.view.widget.WidgetAction.WIDGET_UPDATE
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import kotlin.math.roundToInt


open class WidgetProvider4x2 : AppWidgetProvider() {
    private var httpClient = HttpClient

    init {
        httpClient = HttpClient.getInstance()
    }
    // 앱 위젯은 여러개가 등록 될 수 있는데, 최초의 앱 위젯이 등록 될 때 호출 됩니다. (각 앱 위젯 인스턴스가 등록 될때마다 호출 되는 것이 아님)
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Timber.tag(TAG_W).i("onEnabled")
    }

    // onEnabled() 와는 반대로 마지막의 최종 앱 위젯 인스턴스가 삭제 될 때 호출 됩니다
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Timber.tag(TAG_W).i("onDisabled")
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
        Timber.tag(TAG_W).i("onAppWidgetOptionsChanged")
    }

    // 위젯 메타 데이터를 구성 할 때 updatePeriodMillis 라는 업데이트 주기 값을 설정하게 되며, 이 주기에 따라 호출 됩니다.
    // 또한 앱 위젯이 추가 될 떄에도 호출 되므로 Service 와의 상호작용 등의 초기 설정이 필요 할 경우에도 이 메소드를 통해 구현합니다
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        val views4x2 = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
        appWidgetManager.getAppWidgetInfo(appWidgetIds[0]).minWidth = getDeviceWidth(context)

        val refreshBtnIntent = Intent(context, WidgetProvider4x2::class.java)
        refreshBtnIntent.action = WIDGET_UPDATE
        val pendingRefresh: PendingIntent =
            PendingIntent.getBroadcast(context, 0, refreshBtnIntent, PendingIntent.FLAG_IMMUTABLE)

        val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
            .let {
                it.action = "enterApplication"
                PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
            }

        views4x2.apply {
            setOnClickPendingIntent(R.id.widget4x2MainLayout, pendingIntent)
            setOnClickPendingIntent(R.id.widget4x2Refresh, pendingRefresh)
            setOnClickPendingIntent(R.id.widget4x2ReloadLayout, pendingRefresh)
        }

        appWidgetManager.updateAppWidget(appWidgetIds, views4x2)
    }

    // 이 메소드는 앱 데이터가 구글 시스템에 백업 된 이후 복원 될 때 만약 위젯 데이터가 있다면 데이터가 복구 된 이후 호출 됩니다.
    // 일반적으로 사용 될 경우는 흔치 않습니다.
    // 위젯 ID 는 UID 별로 관리 되는데 이때 복원 시점에서 ID 가 변경 될 수 있으므로 백업 시점의 oldID 와 복원 후의 newID 를 전달합니다
    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        Timber.tag(TAG_W).i("onRestored")
    }

    // 해당 앱 위젯이 삭제 될 때 호출 됩니다
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Timber.tag(TAG_W).i("onDeleted")
    }

    // 앱의 브로드캐스트를 수신하며 해당 메서드를 통해 각 브로드캐스트에 맞게 메서드를 호출한다.
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Timber.tag(TAG_W)
            .i("onReceive : ${currentDateTimeString(context)} intent : ${intent.action}")

        intent.action?.let {
            when(it) {
                WIDGET_UPDATE,
                WIDGET_ENABLE -> {
                    loadData(context)
                }
                else -> {}
            }
        }
    }

    private fun changeVisibility(views: RemoteViews, isReload: Boolean) {
        val list = listOf(R.id.widget4x2Address,R.id.widget4x2PmValue,R.id.widget4x2RainPerValue,
        R.id.widget4x2AddressVector,R.id.widget4x2RainPer,R.id.widget4x2PmIndex,R.id.widget4x2Refresh,
        R.id.widget4x2VerticalLine,R.id.widget4x2SkyImg,R.id.widget4x2TempValue,R.id.widget4x2TempIndex)
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
        }
    }

    private fun <T> failToFetchData(t: T, views: RemoteViews, title: String) {

//        Toast.makeText(context, "데이터 호출 실패", Toast.LENGTH_SHORT).show()
        changeVisibility(views, true)

        when (t) {
            is java.lang.Exception -> {
                t.printStackTrace()
                t.localizedMessage?.let { it1 ->
                    RDBLogcat.writeLogCause(
                        "ANR 발생",
                        "Thread : WidgetProvider - $title",
                        it1
                    )
                }
            }
            is Throwable -> {
                t.printStackTrace()
                t.localizedMessage?.let { it1 ->
                    RDBLogcat.writeLogCause(
                        "ANR 발생",
                        "Thread : WidgetProvider - $title",
                        it1
                    )
                }
            }
            else -> {
                RDBLogcat.writeLogCause(
                    "ANR 발생",
                    "Thread : WidgetProvider - $title",
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

    @SuppressLint("MissingPermission")
    private fun loadData(context: Context) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
        val getLocation = GetLocation(context)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    getLocation.getAddress(it.latitude, it.longitude)?.let { addr ->
                        RDBLogcat.writeLogCause(
                            "위젯 데이터 호출 성공",
                            "onUpdate",
                            addr
                        )
                        changeVisibility(views, false)

                        getLocation.updateCurrentAddress(it.latitude, it.longitude, addr)

                        val getDataResponse: Call<ApiModel.Widget4x2Data> =
                            httpClient.mMyAPIImpl.getWidgetForecast(it.latitude, it.longitude, 1)

                        getDataResponse.enqueue(object :
                            Callback<ApiModel.Widget4x2Data> {
                            override fun onResponse(
                                call: Call<ApiModel.Widget4x2Data>,
                                response: Response<ApiModel.Widget4x2Data>
                            ) {
                                try {
                                    RDBLogcat.writeLogCause(
                                        "위젯 데이터 호출 성공",
                                        "Thread : WidgetProvider",
                                        response.body().toString()
                                    )
                                    val body = response.body()
                                    val data = body!!
                                    val current = data.current
                                    val thunder = data.thunder
                                    val sun = data.sun
                                    val realtime = data.realtime[0]
                                    val skyText = applySkyText(
                                        context,
                                        current.rainType!!,
                                        realtime.sky,
                                        thunder
                                    )

                                    views.apply {
                                        setViewVisibility(R.id.widget4x2ReloadLayout, View.GONE)

                                        setInt(
                                            R.id.widget4x2MainLayout, "setBackgroundResource",
                                            getSkyImgWidget(
                                                skyText,
                                                getCurrentSun(sun.sunrise!!, sun.sunset!!)
                                            )
                                        )

                                        setTextViewText(
                                            R.id.widget4x2Time,
                                            DataTypeParser.millsToString(
                                                getCurrentTime(),
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
                                            getDataText(data.quality.pm10Grade1h!!).trim()
                                        )

                                        setTextViewText(R.id.widget4x2TempIndex, skyText)

                                        setImageViewBitmap(
                                            R.id.widget4x2SkyImg,
                                            (getSkyImgLarge(context, skyText, false)
                                                    as BitmapDrawable).bitmap
                                        )

                                        setTextViewText(
                                            R.id.widget4x2Address,
                                            getNotificationAddress(context)
                                                .trim()
                                                .replace("null", "")
//                                            addrFormat[addrFormat.size - 2]
                                        )
                                    }

                                    fetch(context, views)
                                } catch (e: Exception) {
                                    failToFetchData(e, views, "onResponse - catch")
                                }
                            }

                            override fun onFailure(
                                call: Call<ApiModel.Widget4x2Data>,
                                t: Throwable
                            ) {
                                failToFetchData(t, views, "onFailure")
                            }
                        })
                    }
                }
            }.addOnFailureListener {
                failToFetchData(it, views, "addOnFailureListener")
            }
    }
}