package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject.TAG_W
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.gps.GetLocation
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.retrofit.HttpClient
import app.airsignal.weather.util.AddressFromRegex
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.util.`object`.DataTypeParser.currentDateTimeString
import app.airsignal.weather.util.`object`.DataTypeParser.getBackgroundImgWidget
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgWidget
import app.airsignal.weather.util.`object`.GetAppInfo
import app.airsignal.weather.view.activity.SplashActivity
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-07-04 오후 4:27
 **/
open class WidgetProvider : AppWidgetProvider() {

    init {
        LoggerUtil().getInstance()
    }

    companion object {
        const val REFRESH_BUTTON_CLICKED = "app.airsignal.weather.view.widget.REFRESH_DATA"
        const val ENTER_APPLICATION = "app.airsignal.weather.view.widget.ENTER_APP"
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
    }

    // 위젯 메타 데이터를 구성 할 때 updatePeriodMillis 라는 업데이트 주기 값을 설정하게 되며, 이 주기에 따라 호출 됩니다.
    // 또한 앱 위젯이 추가 될 떄에도 호출 되므로 Service 와의 상호작용 등의 초기 설정이 필요 할 경우에도 이 메소드를 통해 구현합니다
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            try {
                RDBLogcat.writeWidgetHistory(context.applicationContext,"lifecycle","onUpdate")
                refresh(context.applicationContext,appWidgetId)
                val views = RemoteViews(context.packageName, R.layout.widget_layout_2x2)
            } catch (e: Exception) {
                RDBLogcat.writeWidgetHistory(context.applicationContext,"error",e.stackTraceToString())
            }
        }
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

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val appWidgetId = intent!!.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && intent.action == REFRESH_BUTTON_CLICKED) {
            if (context != null) {
                refresh(context.applicationContext,appWidgetId)
            } else {
                Timber.tag(TAG_W).e("context is null")
            }
        }
    }

    private fun refresh(context: Context,appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_2x2)

        val refreshBtnIntent = Intent(context, WidgetProvider::class.java).apply {
            action = REFRESH_BUTTON_CLICKED
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val enterPending: PendingIntent = Intent(context, SplashActivity::class.java)
            .let {
                it.action = ENTER_APPLICATION
                PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
            }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshBtnIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(
            R.id.widget2x2Refresh,
            pendingIntent
        )

        views.setOnClickPendingIntent(
            R.id.widget2x2Background,
            enterPending
        )

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)

        CoroutineScope(Dispatchers.IO).launch {
            fetch(context, views)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetch(context: Context, views: RemoteViews) {
        val perm = RequestPermissionsUtil(context)
        @RequiresApi(Build.VERSION_CODES.Q)
        if (!perm.isBackgroundRequestLocation()) {
            Toast.makeText(context, "위치 권한 '항상허용' 필요", Toast.LENGTH_SHORT).show()
            perm.requestBackgroundLocation()
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val onSuccess: (Location?) -> Unit = { location ->
            CoroutineScope(Dispatchers.Default).launch {
                location?.let { loc ->
                    val lat = loc.latitude
                    val lng = loc.longitude
                    val data = requestWeather(lat, lng)
                    val addr = getAddress(context, lat, lng)

                    Timber.tag(TAG_W).i("fetch address : $addr data : $data")

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
        }
        try {
            val lat = 37.4116017
            val lng = 127.0953589
            val data = requestWeather(lat, lng)
            val addr = getAddress(context, lat, lng)

            Timber.tag(TAG_W).i("fetch address : $addr data : $data")
            RDBLogcat.writeWidgetHistory(context.applicationContext, "data", data.toString())

            withContext(Dispatchers.Main) {
                updateUI(context, views, data, addr)
            }
        } catch (e: Exception) {
            RDBLogcat.writeWidgetHistory(context,"fetch error",e.localizedMessage)
        }
    }

    private suspend fun requestWeather(lat: Double, lng: Double): ApiModel.WidgetData? {
        return HttpClient.getInstance(true).setClientBuilder()
            .getWidgetForecast(lat, lng, 1)
            .awaitResponse().body()
    }

    private fun getAddress(context: Context, lat: Double, lng: Double): String? {
        val loc = GetLocation(context).getAddress(lat, lng)
        return AddressFromRegex(
            loc ?: ""
        ).getNotificationAddress()
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
                        R.id.widget2x2TempValue,
                        "${it.current.temperature ?: 0}˚"
                    )
                    this.setTextViewText(R.id.widget2x2Address, addr ?: "")
                    this.setImageViewResource(R.id.widget2x2SkyImg,
                        getSkyImgWidget(it.current.rainType, it.realtime[0].sky, isNight)
                    )
                    val bg = getBackgroundImgWidget(sky = it.realtime[0].sky, isNight)
                    setInt(R.id.widget2x2Background, "setBackgroundResource", bg)
                    applyColor(appContext,views,bg)
                }
            }

            appWidgetManager.updateAppWidget(componentName, views)
        } catch (e: Exception) {
            Timber.tag(TAG_W).e(e.stackTraceToString())
        }
    }

    private fun applyColor(context: Context,views: RemoteViews, bg:Int) {
        val textArray = arrayOf(R.id.widget2x2TempValue, R.id.widget2x2Time, R.id.widget2x2Address)
        views.run {
            textArray.forEach {
                when (bg) {
                    R.drawable.w_bg_sunny, R.drawable.w_bg_snow -> this.setTextColor(it,context.getColor(R.color.black))
                    R.drawable.w_bg_night, R.drawable.w_bg_cloudy -> this.setTextColor(it,context.getColor(R.color.white))
                    else -> this.setTextColor(it,context.getColor(android.R.color.transparent))
                }
            }
        }
    }
}