package com.example.airsignal_app.firebase.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject
import com.example.airsignal_app.dao.StaticDataObject.NOTIFICATION_CHANNEL_ID
import com.example.airsignal_app.dao.StaticDataObject.NOTIFICATION_CHANNEL_NAME
import com.example.airsignal_app.db.room.repository.GpsRepository

class NotificationBuilder {


    fun sendNotification(context: Context, intent: Intent,data: String, title: String,time: Long) {
//        // Get the layouts to use in the custom notification
//        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_small)
//        val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_large)
//
//        // Apply the layouts to the notification
//        val customNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
//            .setSmallIcon(R.drawable.app_icon)
//            .setLargeIcon((ResourcesCompat.getDrawable(context.resources,R.drawable.sunny_test,null) as BitmapDrawable).bitmap)
//            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//            .setCustomContentView(notificationLayout)
//            .setCustomBigContentView(notificationLayoutExpanded)
//            .build()


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

        val pmString = "미세먼지 나쁨"
        val pmSpan = SpannableStringBuilder(pmString).setSpan(
            ForegroundColorSpan(Color.RED),
            5,pmString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).toString()
        val location = GpsRepository(context).findById(StaticDataObject.CURRENT_GPS_ID).addr.toString()
        val locationSpan = SpannableStringBuilder(location).setSpan(
            android.text.style.AbsoluteSizeSpan(10),0,location.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).toString()
        val data = "최고: 24˚ 최저 : 10˚"

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(time)
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(data)
            .setLargeIcon((ResourcesCompat.getDrawable(context.resources,R.drawable.sunny_test,null) as BitmapDrawable).bitmap)

        notificationManager!!.run {
            createNotificationChannel(notificationChannel)
            notify(1, notificationBuilder.build())
        }
    }
}
