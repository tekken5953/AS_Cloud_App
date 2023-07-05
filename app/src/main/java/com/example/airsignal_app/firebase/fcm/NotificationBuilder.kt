package com.example.airsignal_app.firebase.fcm

import android.app.Notification
import android.app.Notification.BADGE_ICON_LARGE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.IconCompat.IconType
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject
import com.example.airsignal_app.dao.StaticDataObject.NOTIFICATION_CHANNEL_ID
import com.example.airsignal_app.dao.StaticDataObject.NOTIFICATION_CHANNEL_NAME
import com.example.airsignal_app.db.room.repository.GpsRepository
import kotlin.math.roundToInt

class NotificationBuilder {
    fun sendNotification(context: Context, intent: Intent, data: Map<String,String>) {
//        // Get the layouts to use in the custom notification
//        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_small)
//        val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_large)
//        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        // Apply the layouts to the notification
//        val customNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
//            .setAutoCancel(true)
//            .setDefaults(Notification.DEFAULT_ALL)
//            .setWhen(System.currentTimeMillis())
////            .setSilent(true)
//            .setSubText("${data["location"]}시 중원구")
//            .setSmallIcon(R.drawable.app_icon)
//            .setPriority(NotificationManager.IMPORTANCE_HIGH)
//            .setContentIntent(pendingIntent)
//            .setContentTitle("${data["temp"].toString().toDouble().roundToInt()}˚")
//            .setContentText("최대 : ${data["max"]}˚ 최소 : ${data["min"]}˚")
//            .setLargeIcon((ResourcesCompat.getDrawable(context.resources,R.drawable.ico_sunny,
//                null) as BitmapDrawable).bitmap)


        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel description"
            enableLights(true)
            lightColor = Color.BLUE
            vibrationPattern = longArrayOf(0, 100, 200, 300)
            lockscreenVisibility = View.VISIBLE
            enableVibration(true)
        }

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

        notificationBuilder
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
//            .setSilent(true)
            .setSubText("${data["location"]}시 중원구")
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentIntent(pendingIntent)
            .setContentTitle("${data["temp"].toString().toDouble().roundToInt()}˚")
            .setContentText("최대 : ${data["max"]}˚ 최소 : ${data["min"]}˚")
            .setLargeIcon((ResourcesCompat.getDrawable(context.resources,R.drawable.ico_sunny,
                null) as BitmapDrawable).bitmap)

        notificationManager!!.run {
            createNotificationChannel(notificationChannel)
            notify(1, notificationBuilder.build())
        }
    }
}
