package app.airsignal.weather.firebase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.view.View
import androidx.core.app.NotificationCompat
import app.airsignal.weather.R
import app.airsignal.weather.dao.RDBLogcat

class EyeNotiBuilder(private val context: Context) {
    private val notificationManager: NotificationManager? =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

    private fun createNotificationChannel(importance: Int) {
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationChannel = NotificationChannel(
            SubFCM.Channel.NOTIFICATION_CHANNEL_ID.value,
            SubFCM.Channel.NOTIFICATION_CHANNEL_NAME.value,
            importance
        ).apply {
            description = SubFCM.Channel.NOTIFICATION_CHANNEL_DESCRIPTION.value
            lockscreenVisibility = View.VISIBLE
            setSound(sound, AudioAttributes.Builder().build())
        }

        notificationManager?.createNotificationChannel(notificationChannel)
    }

    private fun buildNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, SubFCM.Channel.NOTIFICATION_CHANNEL_ID.value)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(Color.parseColor("#00C2FF"))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_stat_airsignal_default)
    }

    fun sendNotification(data: Map<String, String>) {
        try {
            val appContext = context.applicationContext
            val intent = Intent("android.intent.action.MAIN")
            intent.setPackage("app.airsignal.weather")
            intent.component =
                ComponentName(
                    "app.airsignal.weather",
                    "app.airsignal.weather.as_eye.activity.EyeListActivity"
                )
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK)

            val pendingIntent =
                PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val notificationBuilderInstance = buildNotificationBuilder()

            val sort = data["sort"]

            sort?.let {
                val payload = "${data["payload"]}의 ${parseSortToTitle(sort)} 감지되었습니다"
                setNotificationContent(
                    notificationBuilderInstance,
                    title = "에어시그널",
                    subtext = "${data["device"]}",
                    content = payload
                )
            }

            notificationBuilderInstance.setContentIntent(pendingIntent)

            notificationManager?.let {
                createNotificationChannel(NotificationManager.IMPORTANCE_HIGH)
                it.notify(2, notificationBuilderInstance.build())
            }

        } catch (e: Exception) {
            RDBLogcat.writeErrorANR("FCM Thread", e.localizedMessage!!)
        }
    }

    private fun setNotificationContent(
        builder: NotificationCompat.Builder,
        title: String,
        subtext: String?,
        content: String
    ) {
        builder.setContentTitle(title)
            .setSubText(subtext)
            .setContentText(content)
    }

    private fun parseSortToTitle(sort: String?): String? {
        return when (sort) {
            SubFCM.Sort.FCM_EYE_NOISE.key -> "소음이"
            SubFCM.Sort.FCM_EYE_GYRO.key -> "진동이"
            SubFCM.Sort.FCM_EYE_BRIGHT.key -> "조도 변화가"
            else -> null
        }
    }
}