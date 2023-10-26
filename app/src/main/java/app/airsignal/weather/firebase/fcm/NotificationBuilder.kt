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
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "Channel description"
    }

    fun sendNotification(context: Context, data: Map<String,String>) {
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
            description = NOTIFICATION_CHANNEL_DESCRIPTION
            lockscreenVisibility = View.VISIBLE
            setSound(sound,AudioAttributes.Builder().build())
        }

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

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

        if (getUserNotiEnable(context)) {
            notificationManager?.let {
                it.createNotificationChannel(notificationChannel)
                it.notify(1, notificationBuilder.build())
            }
            RDBLogcat.writeNotificationHistory(context,data["sort"].toString(),data.toString())
        } else {
            RDBLogcat.writeNotificationHistory(context, "체크 해제로 인한 알림 미발송",
                "${GetAppInfo.getUserLastAddress(context)} $data")
        }

        fun processFcmNotification(data: Map<String, String>, context: Context) {
            val notificationTitle: String
            val notificationContent: String

            when (data["sort"]) {
                FCM_DAILY -> {
                    val temp = parseStringToDoubleToInt(data["temp"].toString())
                    val rainType = data["rainType"]
                    val sky = data["sky"]
                    val thunder = data["thunder"]?.toDouble()
                    val lunar = data["lunar"]?.toInt()
                    notificationTitle = "${temp}˚ ${applySkyText(context, rainType, sky, thunder)}"
                    notificationContent = "최대 : ${parseStringToDoubleToInt(data["max"].toString())}˚ " +
                            "최소 : ${parseStringToDoubleToInt(data["min"].toString())}˚"
                }
                FCM_PATCH, FCM_EVENT -> {
                    val payload = data["payload"] ?: if (data["sort"] == FCM_PATCH)
                        "새로운 업데이트가 준비되었어요" else "눌러서 이벤트를 확인하세요"
                    notificationTitle = "에어시그널 날씨"
                    notificationContent = payload
                }
                else -> {
                    // 기본 처리 또는 예외 처리
                    notificationTitle = "에어시그널 알림"
                    notificationContent = "현재 날씨를 확인해보세요"
                }
            }

            setNotiBuilder(
                title = notificationTitle,
                subtext = getNotificationAddress(context),
                content = notificationContent,
                imgPath = getSkyBitmap(context,
                    data["rainType"],
                    data["sky"],
                    data["thunder"]?.toDouble(),
                    data["lunar"]?.toInt() ?: -1)
            )
        }

        processFcmNotification(data,context)
    }

   private fun applyRingtone(context: Context,ringtone: Ringtone) {
       if (getUserNotiSound(context)) {
           if (ringtone.isPlaying) ringtone.stop()
           ringtone.play()
       }
   }

    private fun applyVibrate(context: Context) {
        VibrateUtil(context).noti(longArrayOf(0,100,100))
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
