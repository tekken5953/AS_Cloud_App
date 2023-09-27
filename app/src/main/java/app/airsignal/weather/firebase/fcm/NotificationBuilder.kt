package app.airsignal.weather.firebase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.view.View
import androidx.core.app.NotificationCompat
import app.airsignal.weather.R
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.util.VibrateUtil
import app.airsignal.weather.util.`object`.DataTypeParser.applySkyText
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgLarge
import app.airsignal.weather.util.`object`.GetAppInfo
import app.airsignal.weather.util.`object`.GetAppInfo.getNotificationAddress
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiEnable
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiSound
import app.airsignal.weather.util.`object`.GetAppInfo.getUserNotiVibrate
import app.airsignal.weather.util.`object`.GetSystemInfo
import app.airsignal.weather.view.activity.SplashActivity
import kotlinx.coroutines.*
import kotlin.math.roundToInt


class NotificationBuilder {
    lateinit var intent: Intent

    companion object {
        const val FCM_DAILY = "daily"
        const val FCM_PATCH = "patch"
        const val FCM_EVENT = "event"
        const val NOTIFICATION_CHANNEL_ID = "500"             // FCM 채널 ID
        const val NOTIFICATION_CHANNEL_NAME = "AIRSIGNAL"     // FCM 채널 NAME
    }

    fun sendNotification(context: Context, data: Map<String,String>) {
//        // Get the layouts to use in the custom notification
//        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_small)
//        val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_large)
//        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        if(data["sort"] == FCM_PATCH) {
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(GetSystemInfo.getPlayStoreURL(context))
        } else {
            intent = Intent(context, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            if (getUserNotiVibrate(context))
                NotificationManager.IMPORTANCE_DEFAULT
            else NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel description"
            lockscreenVisibility = View.VISIBLE
            setSound(sound,AudioAttributes.Builder().build())
        }

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
//        val ringtone = RingtoneManager.getRingtone(context, sound)

        fun setNotiBuilder(title: String, subtext: String?, content: String, imgPath: Bitmap?
        ) {
            notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.main_blue_color))
                .setWhen(System.currentTimeMillis())
                .setSubText(subtext)
                .setSmallIcon(R.drawable.ic_stat_airsignal_default)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(content)
                .setLargeIcon(imgPath)
        }

        when (data["sort"]) {
            FCM_DAILY -> {
                val temp = parseStringToDoubleToInt(data["temp"].toString())
                val rainType = data["rainType"]
                val sky = data["sky"]
                val thunder = data["thunder"]?.toDouble()
                val lunar = data["lunar"]?.toInt()
                setNotiBuilder(
                    title = "${temp}˚ ${applySkyText(context, rainType, sky, thunder)}",
                    subtext = getNotificationAddress(context),
                    content = "최대 : ${parseStringToDoubleToInt(data["max"].toString())}˚ " +
                            "최소 : ${parseStringToDoubleToInt(data["min"].toString())}˚",
                    imgPath = getSkyBitmap(context, rainType, sky, thunder, lunar ?: -1)
                )
            }
            FCM_PATCH -> {
                val payload = data["payload"] ?: "새로운 업데이트가 준비되었어요"
                setNotiBuilder(title = "에어시그널 날씨", subtext = null, content = payload, null)
            }
            FCM_EVENT -> {
                val payload = data["payload"] ?: "눌러서 이벤트를 확인하세요"
                setNotiBuilder(title = "에어시그널 날씨", subtext = null, content = payload, null)
            }
        }

        if (getUserNotiEnable(context)) {
            notificationManager?.let {
                it.createNotificationChannel(notificationChannel)
//                applyVibrate(context)
                it.notify(1, notificationBuilder.build())
            }
            RDBLogcat.writeNotificationHistory(context,data["sort"].toString(),data.toString())
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
        CoroutineScope(Dispatchers.Default).launch {
            VibrateUtil(context).noti(longArrayOf(0,100,100))
        }
    }

    private fun getSkyBitmap(
        context: Context,
        rain: String?,
        sky: String?,
        thunder: Double?,
        lunar: Int?
    ): Bitmap? {
        val bitmapDrawable = getSkyImgLarge(
            context,
            applySkyText(context, rain, sky, thunder),
            false,
            lunar ?: -1
        ) as BitmapDrawable

        return bitmapDrawable.bitmap
    }

    private fun parseStringToDoubleToInt(s: String): Int {
        return s.toDouble().roundToInt()
    }
}
