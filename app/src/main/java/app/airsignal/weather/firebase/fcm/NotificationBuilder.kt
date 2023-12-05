package app.airsignal.weather.firebase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import app.airsignal.core_databse.db.sp.GetAppInfo
import app.airsignal.core_databse.db.sp.GetAppInfo.getNotificationAddress
import app.airsignal.core_databse.db.sp.GetAppInfo.getUserNotiEnable
import app.airsignal.core_databse.db.sp.GetAppInfo.getUserNotiVibrate
import app.airsignal.core_databse.db.sp.GetSystemInfo
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.R
import app.airsignal.weather.util.`object`.DataTypeParser.applySkyText
import app.airsignal.weather.util.`object`.DataTypeParser.getSkyImgLarge
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
        val appContext = context.applicationContext
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        if (data["sort"] == FCM_PATCH) {
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(GetSystemInfo.getPlayStoreURL(appContext))
        } else {
            intent = Intent("android.intent.action.MAIN")
            intent.setPackage("app.airsignal.weather")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent =
            PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            if (getUserNotiVibrate(appContext))
                NotificationManager.IMPORTANCE_DEFAULT
            else NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = NOTIFICATION_CHANNEL_DESCRIPTION
            lockscreenVisibility = View.VISIBLE
            setSound(sound, AudioAttributes.Builder().build())
        }

        val notificationBuilder = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)

        fun setNotiBuilder(
            title: String, subtext: String?, content: String, imgPath: Bitmap?
        ) {
            notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#00C2FF"))
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
                    title = "${temp}˚ ${applySkyText(appContext, rainType, sky, thunder)}",
                    subtext = getNotificationAddress(appContext),
                    content = "최대 : ${parseStringToDoubleToInt(data["max"].toString())}˚ " +
                            "최소 : ${parseStringToDoubleToInt(data["min"].toString())}˚",
                    imgPath = getSkyBitmap(appContext, rainType, sky, thunder, lunar ?: -1)
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

        if (getUserNotiEnable(appContext)) {
            notificationManager?.let {
                it.deleteNotificationChannel(notificationChannel.toString())
                it.createNotificationChannel(notificationChannel)
//                applyVibrate(appContext)
                it.cancel(1)
                it.notify(1, notificationBuilder.build())
            }
            RDBLogcat.writeNotificationHistory(appContext,data["sort"].toString(),"${getNotificationAddress(appContext)} $data")
        } else {
            RDBLogcat.writeNotificationHistory(appContext, "체크 해제로 인한 알림 미발송",
                "${GetAppInfo.getUserLastAddress(appContext)} $data")
        }

        if (getUserNotiEnable(appContext)) {
            notificationManager?.let {
                it.createNotificationChannel(notificationChannel)
                it.notify(1, notificationBuilder.build())
            }
            RDBLogcat.writeNotificationHistory(context, data["sort"].toString(), data.toString())
        } else {
            RDBLogcat.writeNotificationHistory(
                appContext, "체크 해제로 인한 알림 미발송",
                "${GetAppInfo.getUserLastAddress(appContext)} $data"
            )
        }
    }

    private fun getSkyBitmap(
        context: Context,
        rain: String?,
        sky: String?,
        thunder: Double?,
        lunar: Int?
    ): Bitmap? {
        return when
                (val bitmapDrawable = getSkyImgLarge(context,
                applySkyText(context, rain, sky, thunder),
                false, lunar ?: -1)) {
            is BitmapDrawable -> { bitmapDrawable.bitmap }
            is VectorDrawable -> { (bitmapDrawable).toBitmap() }
            else -> { null }
        }
    }

    private fun parseStringToDoubleToInt(s: String): Int {
        return s.toDouble().roundToInt()
    }
}
