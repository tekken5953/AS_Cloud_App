package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject
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
@SuppressLint("MissingPermission")
open class WidgetProvider : AppWidgetProvider() {
    init {
        LoggerUtil().getInstance()
    }

    companion object {
        const val REFRESH_BUTTON_CLICKED = "refreshButtonClicked"
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        val mContext = context!!.applicationContext
        val views = RemoteViews(mContext.packageName, R.layout.widget_layout)
        val perm = RequestPermissionsUtil(mContext)
        @RequiresApi(Build.VERSION_CODES.Q)
        if (!perm.isBackgroundRequestLocation()) {
            Toast.makeText(mContext, "위치 권한 '항상허용' 필요", Toast.LENGTH_SHORT).show()
            perm.requestBackgroundLocation()
        }
        val appWidgetId = intent!!.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && intent.action == REFRESH_BUTTON_CLICKED) {
            if (mContext != null) {
                refresh(context.applicationContext,appWidgetId)
            } else {
                Timber.tag(TAG_W).e("context is null")
            }
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        val onSuccess: (Location?) -> Unit = { location ->
            CoroutineScope(Dispatchers.Default).launch {
                location?.let { loc ->
                    val lat = loc.latitude
                    val lng = loc.longitude
                    val data = requestWeather(lat, lng)
                    val addr = getAddress(mContext, lat, lng)

                    Timber.tag(StaticDataObject.TAG_W).i("fetch address : $addr data : $data")

                    withContext(Dispatchers.Main) {
                        updateUI(mContext, views, data, addr)
                    }
                }
            }
        }
        val onFailure: (e: Exception) -> Unit = {
            Timber.tag(StaticDataObject.TAG_W).e(it.stackTraceToString())
            RDBLogcat.writeWidgetHistory(mContext, "widget error", it.localizedMessage)
        }
        CoroutineScope(Dispatchers.Default).launch {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure)
        }
    }

    // 앱 위젯은 여러개가 등록 될 수 있는데, 최초의 앱 위젯이 등록 될 때 호출 됩니다. (각 앱 위젯 인스턴스가 등록 될때마다 호출 되는 것이 아님)
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Timber.tag(TAG_W).i("onEnabled")

        val refreshBtnIntent = Intent(context, WidgetProvider::class.java).apply {
            putExtra("mode","data")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            11,
            refreshBtnIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,60000,pendingIntent)
    }

    // onEnabled() 와는 반대로 마지막의 최종 앱 위젯 인스턴스가 삭제 될 때 호출 됩니다
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Timber.tag(TAG_W).i("onDisabled")
        val refreshBtnIntent = Intent(context, WidgetProvider::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            11,
            refreshBtnIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
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
//        for (appWidgetId in appWidgetIds) {
//            try {
//                refresh(context.applicationContext,appWidgetId)
//            } catch (e: Exception) {
//                RDBLogcat.writeWidgetHistory(context.applicationContext,"error",e.stackTraceToString())
//            }
//        }
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

    private fun refresh(context: Context,appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        val refreshBtnIntent = Intent(context, WidgetProvider::class.java).apply {
            action = REFRESH_BUTTON_CLICKED
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshBtnIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(
            R.id.widgetRefresh,
            pendingIntent
        )

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)

        CoroutineScope(Dispatchers.Default).launch {
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
    }

    private suspend fun requestWeather(lat: Double, lng: Double): ApiModel.WidgetData? {
        val body =  HttpClient.getInstance(true).setClientBuilder()
        .getWidgetForecast(lat, lng, 1)
            .awaitResponse().body()
        Timber.tag(TAG_W).i("body : $body")
        return body
    }

    private fun getAddress(context: Context, lat: Double, lng: Double): String? {
        val loc = GetLocation(context).getAddress(lat, lng)
        val addr = AddressFromRegex(
            loc ?: ""
        ).getNotificationAddress()

        Timber.tag(TAG_W).i("addr : $addr")
        return addr
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
            RDBLogcat.writeWidgetHistory(appContext, "data", data.toString())

            views.run {
                this.setTextViewText(R.id.widgetTime, currentTime)
                data?.let {
                    this.setTextViewText(
                        R.id.widgetTempValue,
                        "${it.current.temperature ?: 0}˚"
                    )
                    this.setTextViewText(R.id.widgetAddress, addr ?: "")
                    this.setImageViewResource(R.id.widgetSkyImg,
                        getSkyImgWidget(it.realtime[0].sky, isNight)
                    )
                    val bg = getBackgroundImgWidget(sky = it.realtime[0].sky, isNight)
                    setInt(
                        R.id.widgetBackground, "setBackgroundResource",
                        bg
                    )
                    applyColor(appContext,views,bg)
                }
            }

            appWidgetManager.updateAppWidget(componentName, views)
        } catch (e: Exception) {
            Timber.tag(TAG_W).e(e.stackTraceToString())
        }
    }

    private fun applyColor(context: Context,views: RemoteViews, bg:Int) {
        val textArray = arrayOf(R.id.widgetTempValue, R.id.widgetTime, R.id.widgetAddress)
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