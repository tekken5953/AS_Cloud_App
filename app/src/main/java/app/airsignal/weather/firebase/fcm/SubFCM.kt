package app.airsignal.weather.firebase.fcm

import android.content.Intent
import androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService
import app.airsignal.weather.dao.StaticDataObject.TAG_N
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*


class SubFCM: FirebaseMessagingService() {
    private val instance = FirebaseMessaging.getInstance()

    /** 메시지 받았을 때 **/
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.tag(TAG_N).d("onMessageReceived(${message.data})")

        // 포그라운드 노티피케이션 발생
        NotificationBuilder().sendNotification(
            applicationContext,
            message.data
        )
    }

    /** 토픽 구독 설정 **/
    fun subTopic(topic: String): SubFCM {
        instance.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val msg = "Subscribe failed"
                    Timber.tag(TAG_N).w("$msg ${task.exception}")
                } else {
                    val msg = "Subscribed : $topic"
                    Timber.tag(TAG_N).d(msg)
                }
            }
        return this
    }

    /** 토픽 구독 해제 **/
    private fun unSubTopic(topic: String): SubFCM {
        val encodedStream = encodeTopic(topic)
        instance.unsubscribeFromTopic(encodedStream)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val msg = "UnSubscribed failed"
                    Timber.tag(TAG_N).w("$msg ${task.exception}")
                } else {
                    val msg = "UnSubscribed : $encodedStream"
                    Timber.tag(TAG_N).w(msg)
                }
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
        val encodedStream = encodeTopic(new)
        Timber.tag(TAG_N).i("renew topic - old : $old new : $new encoded stream : $encodedStream")

        unSubTopic(old).subTopic(encodedStream)
    }

    private fun encodeTopic(topic: String): String {
        val encoder: Base64.Encoder = Base64.getEncoder()
        return encoder.encodeToString(topic.toByteArray())
            .replace("=", "").replace("+", "")
    }

    /** 현재 토큰정보 불러오기 **/
    suspend fun getToken(): String? {
        val token = withContext(Dispatchers.IO) {
            instance.token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.tag("Notification")
                        .w("Fetching FCM registration token failed by ${task.exception}")
                    return@OnCompleteListener
                }
                val token = task.result
                Timber.tag(TAG_N).d("FCM 토큰 : $token")
            }).result
        }
        return token
    }

    /** 새로운 토큰 발행 **/
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.tag(TAG_N).d("sendRegistrationTokenToServer($token)")
    }
}