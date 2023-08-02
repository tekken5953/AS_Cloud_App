package com.example.airsignal_app.firebase.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.view.View
import androidx.core.app.NotificationCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.NOTIFICATION_CHANNEL_ID
import com.example.airsignal_app.dao.StaticDataObject.NOTIFICATION_CHANNEL_NAME
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.util.`object`.DataTypeParser.applySkyText
import com.example.airsignal_app.util.`object`.DataTypeParser.getSkyImgLarge
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.util.`object`.GetAppInfo.getNotificationAddress
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserNotiEnable
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserNotiSound
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserNotiVibrate
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
            setSound(if (getUserNotiSound(context)) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) else null, AudioAttributes.Builder().build())
            vibrationPattern = if (getUserNotiVibrate(context)) longArrayOf(0, 100, 200, 300) else null
            lockscreenVisibility = View.VISIBLE
            enableVibration(true)
        }

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

        notificationBuilder
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
//            .setSilent(true)
            .setSubText(getNotificationAddress(context))
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentIntent(pendingIntent)
            .setContentTitle("${parseStringToDoubleToInt(data["temp"].toString())}˚ ·" +
                    " ${applySkyText(context,data["rainType"],data["sky"],data["thunder"]!!.toDouble())}")
            .setContentText("최대 : ${parseStringToDoubleToInt(data["max"].toString())}˚ " +
                    "최소 : ${parseStringToDoubleToInt(data["min"].toString())}˚")
            .setLargeIcon(getSkyImg(context,
                data["rainType"]!!,data["sky"]!!, data["thunder"]!!.toDouble()))

        if (getUserNotiEnable(context)) {
            notificationManager!!.run {
                createNotificationChannel(notificationChannel)
                notify(1, notificationBuilder.build())
            }
        } else {
            RDBLogcat.writeNotificationHistory(context, "체크 해제로 인한 알림 미발송",
                "${GetAppInfo.getUserLastAddress(context)} $data")
        }
    }

    private fun getSkyImg(context: Context, rain: String?, sky: String?, thunder: Double?): Bitmap? {
        val bitmapDrawable = getSkyImgLarge(context,
            applySkyText(context, rain, sky, thunder),
            false) as BitmapDrawable

        return bitmapDrawable.bitmap
    }

    private fun parseStringToDoubleToInt(s: String): Int {
        return s.toDouble().roundToInt()
    }
}
