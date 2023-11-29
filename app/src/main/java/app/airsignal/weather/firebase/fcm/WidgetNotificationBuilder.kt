package app.airsignal.weather.firebase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.core.app.NotificationCompat
import app.airsignal.weather.R
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.view.widget.WidgetProvider
import app.airsignal.weather.view.widget.WidgetProvider42
import java.util.logging.Handler


class WidgetNotificationBuilder {
    lateinit var intent: Intent

    companion object {
        const val WIDGET_NOTIFICATION_CHANNEL_ID = "FCM_ID"             // FCM 채널 ID
        const val WIDGET_NOTIFICATION_CHANNEL_NAME = "WIDGET_FCM_NAME"     // FCM 채널 NAME
        const val WIDGET_NOTIFICATION_CHANNEL_DESCRIPTION = "Channel description"
    }

    fun sendNotification(context: Context, data: Map<String,String>) {
        val appContext = context.applicationContext
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val notificationChannel = NotificationChannel(
            WIDGET_NOTIFICATION_CHANNEL_ID,
            WIDGET_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = WIDGET_NOTIFICATION_CHANNEL_DESCRIPTION
            lockscreenVisibility = View.GONE
        }

        val notificationBuilder = NotificationCompat.Builder(appContext, WIDGET_NOTIFICATION_CHANNEL_ID).apply {
            this.setPriority(NotificationCompat.PRIORITY_HIGH)
            .setTimeoutAfter(1)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_stat_airsignal_default)
        }

        notificationManager?.let {
            it.createNotificationChannel(notificationChannel)
            it.notify(2, notificationBuilder.build())
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                RDBLogcat.writeNotificationHistory(
                    appContext,
                    "위젯",
                    "${data["layout"]}, ${data["widgetId"]}"
                )
                if (data["layout"] == "22") {
                    WidgetProvider().processUpdate(
                        appContext,
                        data["widgetId"]?.toInt() ?: -1
                    )
                } else if (data["layout"] == "42") {
                    WidgetProvider42().processUpdate(
                        appContext,
                        data["widgetId"]?.toInt() ?: -1
                    )
                }
            },1000)
        }
    }
}
