package app.airsignal.weather.firebase.fcm

import app.airsignal.weather.dao.StaticDataObject.TAG_N
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.view.widget.WidgetProvider
import app.airsignal.weather.view.widget.WidgetProvider42
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*


class SubFCM: FirebaseMessagingService() {

    /** 메시지 받았을 때 **/
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.data["sort"] != "widget") {
            // 포그라운드 노티피케이션 발생
            NotificationBuilder().sendNotification(applicationContext,message.data)
        } else {
            WidgetNotificationBuilder().sendNotification(applicationContext,message.data)
//            RDBLogcat.writeNotificationHistory(applicationContext,"위젯","${parsePriority(message.priority)},${message.data["layout"]},${message.data["widgetId"]}")
//            if (message.data["layout"] == "22") {
//                WidgetProvider().processUpdate(applicationContext, message.data["widgetId"]?.toInt() ?: -1)
//            } else if (message.data["layout"] == "42") {
//                WidgetProvider42().processUpdate(applicationContext, message.data["widgetId"]?.toInt() ?: -1)
//            }
        }
    }

    private fun parsePriority(priority: Int): String {
        return when(priority) {
            0 -> {"unknown"}
            1 -> {"high"}
            2 -> {"normal"}
            else -> {"error"}
        }
    }

    /** 토픽 구독 설정 **/
    suspend fun subTopic(topic: String): SubFCM {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Timber.tag(TAG_N).d("Subscribed : $topic")
        } catch (e: Exception) {
            Timber.tag(TAG_N).w("Subscribe failed: $e")
        }
        return this
    }

    /** 토픽 구독 해제 **/
    private fun unSubTopic(topic: String): SubFCM {
        val encodedStream = encodeTopic(topic)
        FirebaseMessaging.getInstance().unsubscribeFromTopic(encodedStream)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.tag(TAG_N).w("UnSubscribed failed")
                } else {
                    Timber.tag(TAG_N).i("UnSubscribed : $encodedStream")
                }
            }
        return this
    }

    // 어드민 계정 토픽
    suspend fun subAdminTopic() {
        val encodedStream = encodeTopic("admin")
        subTopic(encodedStream)
    }

    /** 현재 위치 토픽 갱신 **/
    suspend fun renewTopic(old: String, new: String) {
        val encodedStream = encodeTopic(new)
        unSubTopic(old).subTopic(encodedStream)
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
    }
}