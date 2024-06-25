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
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.GetSystemInfo
import app.airsignal.weather.utils.DataTypeParser
import org.koin.core.component.KoinComponent
import kotlin.math.roundToInt


class NotificationBuilder: KoinComponent {
    lateinit var intent: Intent

    fun sendNotification(context: Context, data: Map<String,String>) {
        try {
            val appContext = context.applicationContext

            if (data["sort"] == StaticDataObject.FcmSort.FCM_PATCH.key) {
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
            val notificationFcmChannel = NotificationChannel(
                StaticDataObject.FcmChannel.NOTIFICATION_CHANNEL_ID.value,
                StaticDataObject.FcmChannel.NOTIFICATION_CHANNEL_NAME.value,
                if (GetAppInfo.getUserNotiVibrate())
                    NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = StaticDataObject.FcmChannel.NOTIFICATION_CHANNEL_DESCRIPTION.value
                lockscreenVisibility = View.VISIBLE
                setSound(sound, AudioAttributes.Builder().build())
            }

            val notificationBuilder = NotificationCompat.Builder(appContext, StaticDataObject.FcmChannel.NOTIFICATION_CHANNEL_ID.value)

            fun setNotiBuilder(
                title: String, subtext: String?, content: String, imgPath: Bitmap?) {
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
                StaticDataObject.FcmSort.FCM_DAILY.key -> {
                    val temp = parseStringToDoubleToInt(data["temp"].toString())
                    val rainType = data["rainType"]
                    val sky = data["sky"]
                    val thunder = data["thunder"]?.toDouble()
                    val lunar = data["lunar"]?.toInt()
                    setNotiBuilder(
                        title = "${temp}˚ ${DataTypeParser.applySkyText(appContext, rainType, sky, thunder)}",
                        subtext = GetAppInfo.getNotificationAddress(),
                        content = "최대 : ${parseStringToDoubleToInt(data["max"].toString())}˚ " +
                                "최소 : ${parseStringToDoubleToInt(data["min"].toString())}˚",
                        imgPath = getSkyBitmap(appContext, rainType, sky, thunder, lunar ?: -1)
                    )
                }
                StaticDataObject.FcmSort.FCM_PATCH.key -> {
                    setNotiBuilder(
                        title = "에어시그널",
                        subtext = null,
                        content = data["payload"] ?: "새로운 업데이트가 준비되었어요",
                        null
                    )
                }
                StaticDataObject.FcmSort.FCM_EVENT.key -> {
                    setNotiBuilder(
                        title = "에어시그널",
                        subtext = null,
                        content = data["payload"] ?: "눌러서 이벤트를 확인하세요",
                        null)
                }
            }

            if (GetAppInfo.getUserNotiEnable()) {
                (appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .let {
                        it.createNotificationChannel(notificationFcmChannel)
                        it.notify(1, notificationBuilder.build())
                    }
            }
        } catch (e: Exception) {
            e.stackTraceToString()
        }
    }

    private fun getSkyBitmap(
        context: Context,
        rain: String?,
        sky: String?,
        thunder: Double?,
        lunar: Int?
    ): Bitmap? =
        when (val bitmapDrawable = DataTypeParser.getSkyImgLarge(context,
            DataTypeParser.applySkyText(context, rain, sky, thunder),
            false, lunar ?: -1)) {
            is BitmapDrawable -> bitmapDrawable.bitmap
            is VectorDrawable -> (bitmapDrawable).toBitmap()
            else -> null
        }

    private fun parseStringToDoubleToInt(s: String): Int = s.toDouble().roundToInt()
}
