package app.airsignal.weather.firebase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Ringtone
import android.media.RingtoneManager
import android.view.View
import androidx.core.app.NotificationCompat
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject.NOTIFICATION_CHANNEL_ID
import app.airsignal.weather.dao.StaticDataObject.NOTIFICATION_CHANNEL_NAME
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.util.VibrateUtil
import app.airsignal.weather.util.`object`.DataTypeParser.applySkyText
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgLarge
import app.airsignal.weather.util.`object`.GetAppInfo
import app.airsignal.weather.util.`object`.GetAppInfo.getNotificationAddress
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiEnable
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiSound
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiVibrate
import kotlin.math.roundToInt


class NotificationBuilder {
    fun sendNotification(context: Context, intent: Intent, data: Map<String,String>) {
//        // Get the layouts to use in the custom notification
//        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_small)
//        val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_large)
//        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel description"
            lockscreenVisibility = View.VISIBLE
            enableVibration(false)
        }

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, sound)

        fun setNotiBuilder(title: String, subtext: String?, content: String, imgPath: Bitmap?
        ) {
            notificationBuilder
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSubText(subtext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(content)
                .setLargeIcon(imgPath)
        }

        data["sort"]?.let { sort ->
            when(sort) {
                "daily" -> {
                    setNotiBuilder(
                        title = "${parseStringToDoubleToInt(data["temp"].toString())}˚" +
                                " ${applySkyText(context, data["rainType"],
                                    data["sky"], data["thunder"]!!.toDouble())}",
                        subtext = getNotificationAddress(context),
                        content = "최대 : ${parseStringToDoubleToInt(data["max"].toString())}˚ " +
                                "최소 : ${parseStringToDoubleToInt(data["min"].toString())}˚",
                        imgPath = getSkyImg(
                            context,
                            data["rainType"]!!, data["sky"]!!, data["thunder"]!!.toDouble()
                        )
                    )
                }
                "patch" -> {
                    data["payload"]?.let { payload ->
                        setNotiBuilder(title = "에어시그널 날씨", null, payload, null)
                    } ?: setNotiBuilder(title = "에어시그널 날씨", null, "새로운 업데이트가 준비되었어요", null)
                }
                "event" -> {
                    data["payload"]?.let { payload ->
                        setNotiBuilder(title = "에어시그널 날씨", null, payload, null)
                    } ?: setNotiBuilder(title = "에어시그널 날씨", null, "눌러서 이벤트를 확인하세요", null)
                }
            }
        }

        if (getUserNotiEnable(context)) {
            notificationManager!!.run {
                createNotificationChannel(notificationChannel)
                notify(1, notificationBuilder.build())

                applyRingtone(context, ringtone)
                applyVibrate(context)
            }
        } else {
            RDBLogcat.writeNotificationHistory(context, "체크 해제로 인한 알림 미발송",
                "${GetAppInfo.getUserLastAddress(context)} $data")
        }
    }

   private fun applyRingtone(context: Context,ringtone: Ringtone) {
       if (getUserNotiSound(context)) {
           if (ringtone.isPlaying) {
               ringtone.stop()
           }
           ringtone.play()
       }
   }

    private fun applyVibrate(context: Context) {
        if (getUserNotiVibrate(context)) {
            VibrateUtil(context).noti(longArrayOf(0, 100, 200, 300))
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
