package app.airsignal.weather.firebase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.view.View
import androidx.core.app.NotificationCompat
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.R
import app.airsignal.weather.view.widget.WidgetProvider
import app.airsignal.weather.view.widget.WidgetProvider42
import app.location.GetLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WidgetNotificationBuilder {
    lateinit var intent: Intent

    companion object {
        const val WIDGET_NOTIFICATION_CHANNEL_ID = "FCM_ID"             // FCM 채널 ID
        const val WIDGET_NOTIFICATION_CHANNEL_NAME = "WIDGET_FCM_BACKGROUND"     // FCM 채널 NAME
        const val WIDGET_NOTIFICATION_CHANNEL_DESCRIPTION = "Channel description"
    }

    fun sendNotification(context: Context, data: Map<String,String>) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val notificationChannel = NotificationChannel(
            WIDGET_NOTIFICATION_CHANNEL_ID,
            WIDGET_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = WIDGET_NOTIFICATION_CHANNEL_DESCRIPTION
            lockscreenVisibility = View.GONE
        }

        val notificationBuilder = NotificationCompat.Builder(context, WIDGET_NOTIFICATION_CHANNEL_ID).apply {
            this.setPriority(NotificationCompat.PRIORITY_HIGH)
            .setTimeoutAfter(1)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_stat_airsignal_default)
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (data["layout"] == "22") {
                WidgetProvider().processUpdate(
                    context,
                    data["widgetId"]?.toInt() ?: -1
                )
            } else if (data["layout"] == "42") {
                WidgetProvider42().processUpdate(
                    context,
                    data["widgetId"]?.toInt() ?: -1
                )
            }
            RDBLogcat.writeNotificationHistory(
                context,
                "위젯",
                "${data["layout"]}, ${data["widgetId"]}"
            )
        }


        notificationManager?.let {
            it.createNotificationChannel(notificationChannel)
            it.notify(2, notificationBuilder.build())
        }
    }
}
