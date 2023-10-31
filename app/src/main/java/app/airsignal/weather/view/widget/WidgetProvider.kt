package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.RemoteViews
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject.TAG_W
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.gps.GetLocation
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.retrofit.HttpClient
import app.airsignal.weather.util.AddressFromRegex
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.util.`object`.DataTypeParser.currentDateTimeString
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgWidget
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

    init { LoggerUtil().getInstance() }

    companion object {
        const val REFRESH_BUTTON_CLICKED = "refreshButtonClicked"
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
    ) { super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions) }

    // 위젯 메타 데이터를 구성 할 때 updatePeriodMillis 라는 업데이트 주기 값을 설정하게 되며, 이 주기에 따라 호출 됩니다.
    // 또한 앱 위젯이 추가 될 떄에도 호출 되므로 Service 와의 상호작용 등의 초기 설정이 필요 할 경우에도 이 메소드를 통해 구현합니다
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for(appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setOnClickPendingIntent(R.id.widgetRefresh, applyRefreshPendingIntent(context,appWidgetId))

            fetch(context, views)
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
        val appWidgetId = intent!!.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && intent.action == REFRESH_BUTTON_CLICKED) {
            val views = RemoteViews(context!!.packageName, R.layout.widget_layout)
            views.setOnClickPendingIntent(R.id.widgetRefresh, applyRefreshPendingIntent(context,appWidgetId))
            fetch(context, views)
        }
    }

    private fun applyRefreshPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val refreshBtnIntent = Intent(context, WidgetProvider::class.java)
        refreshBtnIntent.action = REFRESH_BUTTON_CLICKED
        refreshBtnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshBtnIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetch(context: Context, views: RemoteViews) {
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
            it.printStackTrace()
            RDBLogcat.writeWidgetHistory(context, "widget error", it.localizedMessage)
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)

//        @Suppress("DEPRECATION")
//        val locationRequest = com.google.android.gms.location.LocationRequest()
//        val locationSettingsRequest = LocationSettingsRequest.Builder()
//            .addLocationRequest(locationRequest)
//            .build()
//        val settingsClient = LocationServices.getSettingsClient(context)
//        settingsClient.checkLocationSettings(locationSettingsRequest)
//            .addOnSuccessListener {
//                // 위치 서비스가 활성화된 경우, 데이터를 호출하고 위젯을 업데이트
//                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
//                    .addOnSuccessListener(onSuccess)
//                    .addOnFailureListener(onFailure)
//            }
//            .addOnFailureListener {
//                Timber.tag(TAG_W).e(it.stackTraceToString())
//                // 위치 서비스가 비활성화된 경우, 사용자에게 위치 서비스를 활성화하라는 메시지 표시
//                Toast.makeText(context, "위치 서비스를 활성화 해주세요", Toast.LENGTH_SHORT).show()
//            }
    }

    private suspend fun requestWeather(lat: Double, lng: Double) : ApiModel.WidgetData? {
        return HttpClient.getInstance(true).mMyAPIImpl.getWidgetForecast(lat, lng, 1)
            .awaitResponse().body()
    }

    private fun getAddress(context: Context, lat: Double, lng: Double) : String? {
        return AddressFromRegex(GetLocation(context).getAddress(lat,lng) ?: "").getNotificationAddress()
    }

    private fun updateUI(context: Context, views: RemoteViews, data: ApiModel.WidgetData?, addr: String?) {
        val currentTime = currentDateTimeString("HH:mm")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName =
            ComponentName(context, WidgetProvider::class.java)
        RDBLogcat.writeWidgetHistory(context,"data", data.toString())

        views.apply {
            this.setTextViewText(R.id.widgetTime, currentTime)
            data?.let {
                this.setTextViewText(R.id.widgetTempValue, "${it.current.temperature ?: 0.0.toInt()}˚")
                this.setTextViewText(R.id.widgetSkyText, "${it.realtime[0].sky}")
                this.setTextViewText(R.id.widgetAddress, addr ?: "")
                this.setImageViewResource(R.id.widgetSkyImg, getSkyImgWidget(it.realtime[0].sky,true))
            }
        }

        appWidgetManager.updateAppWidget(componentName, views)
    }
}