package app.airsignal.weather.firebase.fcm

import app.airsignal.weather.util.TimberUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class SubFCM: FirebaseMessagingService() {

    enum class Sort(val key: String) {
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
        }
    }

    /** 토픽 구독 설정 **/
    fun subTopic(topic: String): SubFCM {
        try {
            CoroutineScope(Dispatchers.Default).launch {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
            }
        } catch (e: Exception) { e.printStackTrace() }
        return this
    }

    /** 토픽 구독 해제 **/
    private fun unSubTopic(topic: String): SubFCM {
        val encodedStream = encodeTopic(topic)
        CoroutineScope(Dispatchers.Default).launch {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(encodedStream)
        }
        return this
    }

    // 어드민 계정 토픽
    fun subAdminTopic() {
        try {
            val encodedStream = encodeTopic("admin")
            subTopic(encodedStream)
        } catch (e: Exception) {
            e.stackTraceToString()
        }
    }

    /** 현재 위치 토픽 갱신 **/
    fun renewTopic(old: String, new: String) {
        if (old != new) {
            try {
                val encodedStream = encodeTopic(new)
                unSubTopic(old).subTopic(encodedStream)
            } catch (e: Exception) {
                e.stackTraceToString()
            }
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
    fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                TimberUtil().w("testtest", "Fetching FCM registration token failed : ${task.exception}")
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            TimberUtil().i("testtest","FCM Token is $token")
        })
    }

    /** 새로운 토큰 발행 **/
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println(token)
    }
}