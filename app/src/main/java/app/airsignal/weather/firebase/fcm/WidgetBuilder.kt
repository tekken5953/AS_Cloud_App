package app.airsignal.weather.firebase.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.core.app.NotificationCompat
import app.airsignal.weather.R
import app.airsignal.weather.firebase.db.RDBLogcat


class WidgetBuilder {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "600"             // FCM 채널 ID
        const val NOTIFICATION_CHANNEL_NAME = "AIRSIGNAL WIDGET"     // FCM 채널 NAME
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "Channel description"
    }

    fun sendNotification(context: Context) {
        val appContext = context.applicationContext
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = NOTIFICATION_CHANNEL_DESCRIPTION
            lockscreenVisibility = View.GONE
        }

        val notificationBuilder = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
        setNotiBuilder(notificationBuilder)
        notificationManager?.let {
            it.createNotificationChannel(notificationChannel)
            it.notify(1, notificationBuilder.build())
        }

        RDBLogcat.writeNotificationHistory(context, "widget noti","notify")
    }

    private fun setNotiBuilder(
       builder: NotificationCompat.Builder
    ) {
        builder
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_stat_airsignal_default)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setTimeoutAfter(1L)
    }
}
