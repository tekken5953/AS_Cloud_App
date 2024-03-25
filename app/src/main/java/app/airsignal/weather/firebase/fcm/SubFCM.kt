package app.airsignal.weather.firebase.fcm

import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.sp.GetAppInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class SubFCM: FirebaseMessagingService() {

    enum class Sort(val key: String) {
        FCM_EYE_NOISE("noise"), FCM_EYE_BRIGHT("bright"), FCM_EYE_GYRO("gyro"),
        FCM_DAILY("daily"), FCM_PATCH("patch"), FCM_EVENT("event")
    }

    enum class Channel(val value: String) {
        NOTIFICATION_CHANNEL_ID("500"),             // FCM 채널 ID
        NOTIFICATION_CHANNEL_NAME("AIRSIGNAL"),     // FCM 채널 NAME
        NOTIFICATION_CHANNEL_DESCRIPTION("Channel description")
    }

    /** 메시지 받았을 때 **/
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        when(message.data["sort"]) {
            Sort.FCM_PATCH.key,
            Sort.FCM_DAILY.key-> {
                NotificationBuilder().sendNotification(applicationContext,message.data)
            }
            Sort.FCM_EVENT.key -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val isLandingEnable =
                        GetAppInfo.isLandingNotification(applicationContext)
                    if (isLandingEnable) {
                        NotificationBuilder().sendNotification(applicationContext,message.data)
                    }
                }
            }
            Sort.FCM_EYE_NOISE.key,
            Sort.FCM_EYE_BRIGHT.key,
            Sort.FCM_EYE_GYRO.key -> {
                message.data["device"]?.let {
                    if (SharedPreferenceManager(applicationContext).getBoolean(it, false)) {
                        EyeNotiBuilder(applicationContext).sendNotification(message.data)
                    }
                }
            }
        }
    }

    /** 토픽 구독 설정 **/
    fun subTopic(topic: String): SubFCM {
        try {
            CoroutineScope(Dispatchers.Default).launch {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    /** 토픽 구독 해제 **/
    fun unSubTopic(topic: String): SubFCM {
        val encodedStream = encodeTopic(topic)
        CoroutineScope(Dispatchers.Default).launch {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(encodedStream)
        }
        return this
    }

    // 어드민 계정 토픽
    fun subAdminTopic() {
        val encodedStream = encodeTopic("admin")
        subTopic(encodedStream)
    }

    /** 현재 위치 토픽 갱신 **/
    fun renewTopic(old: String, new: String) {
        if (old != new) {
            val encodedStream = encodeTopic(new)
            unSubTopic(old).subTopic(encodedStream)
        }
    }

    /**
     * 토픽 인코딩
     *
     * @param topic
     * @return Encoded Topic
     */
    private fun encodeTopic(topic: String): String {
        val encoder: Base64.Encoder = Base64.getEncoder()
        return encoder.encodeToString(topic.toByteArray())
            .replace("=", "").replace("+", "")
    }

    /** 현재 토큰정보 불러오기 **/
    suspend fun getToken(): String? {
        val token = withContext(Dispatchers.IO) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
            }).result
        }
        return token
    }

    /** 새로운 토큰 발행 **/
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println(token)
    }
}