package com.example.airsignal_app.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient
import com.example.airsignal_app.util.ConvertDataType
import com.example.airsignal_app.util.ConvertDataType.currentDateTimeString
import com.example.airsignal_app.view.activity.MainActivity
import com.orhanobut.logger.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.CompletableFuture

open class WidgetProvider : AppWidgetProvider() {
    private var responseData: Response<ApiModel.GetEntireData>? = null

    // 앱 위젯은 여러개가 등록 될 수 있는데, 최초의 앱 위젯이 등록 될 때 호출 됩니다. (각 앱 위젯 인스턴스가 등록 될때마다 호출 되는 것이 아님)
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Timber.tag("TAG_WIDGET").i("onEnabled")
    }

    // onEnabled() 와는 반대로 마지막의 최종 앱 위젯 인스턴스가 삭제 될 때 호출 됩니다
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Timber.tag("TAG_WIDGET").i("onDisabled")
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
        Timber.tag("TAG_WIDGET").i("onAppWidgetOptionsChanged")
    }

    // 위젯 메타 데이터를 구성 할 때 updatePeriodMillis 라는 업데이트 주기 값을 설정하게 되며, 이 주기에 따라 호출 됩니다.
    // 또한 앱 위젯이 추가 될 떄에도 호출 되므로 Service 와의 상호작용 등의 초기 설정이 필요 할 경우에도 이 메소드를 통해 구현합니다
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Timber.tag("TAG_WIDGET").i("onUpdate : ${currentDateTimeString(context)}")
        val db = GpsRepository(context).findById(CURRENT_GPS_ID)

        val getDataMap: Call<ApiModel.GetEntireData> =
            HttpClient.mMyAPIImpl.getForecast(db.lat, db.lng, db.lng.toString())
        getDataMap.enqueue(object : Callback<ApiModel.GetEntireData> {
            override fun onResponse(
                call: Call<ApiModel.GetEntireData>,
                response: Response<ApiModel.GetEntireData>
            ) {
                val future = CompletableFuture.runAsync {
                    responseData = response
                }

                future.get()
                appWidgetIds.forEach { _ ->
                    val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                        .let {
                            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
                        }

                    val pendingRefresh: PendingIntent = Intent(context, WidgetProvider::class.java).let {
                        GetLocation(context).getLocationInBackground()
                        PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
                    }

                    val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                        setOnClickPendingIntent(R.id.widgetMainLayout, pendingIntent)
                        setOnClickPendingIntent(R.id.widgetRefresh, pendingRefresh)

                        setTextViewText(R.id.widgetTempValue, "${responseData!!.body()!!.realtime[0].temp.toInt()}˚")
                        setTextViewText(R.id.widgetPmValue, responseData!!.body()!!.quality.pm10Value.toInt().toString())
                        setImageViewBitmap(R.id.widgetSkyImg,(ConvertDataType.getSkyImg(context,responseData!!.body()!!.realtime[0].sky) as BitmapDrawable).bitmap)
                        setTextViewText(R.id.widgetTimeStamp, ConvertDataType.millsToString(db.timeStamp,"HH시 mm분"))
                        setTextViewText(R.id.widgetAddress," · ${db.addr!!.split(" ").last()}")

//                setTextColor(R.id.widgetContentPM,ResourcesCompat.getColor(context.resources,
//                    R.color.progressGood, null))
                    }

//            val widgetCount = appWidgetIds.size
//            for (i: Int in 0 until(widgetCount)) {
//                val id = appWidgetIds[i]
//                appWidgetManager.updateAppWidget(id, views)
//            }
                    appWidgetManager.updateAppWidget(appWidgetIds, views)
                }
            }

            override fun onFailure(call: Call<ApiModel.GetEntireData>, t: Throwable) {
                Logger.e("날씨 데이터 호출 실패 : " + t.stackTraceToString())
                Toast.makeText(context, "데이터 호출 실패:(", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 이 메소드는 앱 데이터가 구글 시스템에 백업 된 이후 복원 될 때 만약 위젯 데이터가 있다면 데이터가 복구 된 이후 호출 됩니다.
    // 일반적으로 사용 될 경우는 흔치 않습니다.
    // 위젯 ID 는 UID 별로 관리 되는데 이때 복원 시점에서 ID 가 변경 될 수 있으므로 백업 시점의 oldID 와 복원 후의 newID 를 전달합니다
    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        Timber.tag("TAG_WIDGET").i("onRestored")
    }

    // 해당 앱 위젯이 삭제 될 때 호출 됩니다
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Timber.tag("TAG_WIDGET").i("onRestored")
    }

    // 앱의 브로드캐스트를 수신하며 해당 메서드를 통해 각 브로드캐스트에 맞게 메서드를 호출한다.
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Timber.tag("TAG_WIDGET").i("onReceive : ${currentDateTimeString(context)}")
        val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))


        onUpdate(context, AppWidgetManager.getInstance(context), ids)
    }
}