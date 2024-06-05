package app.airsignal.weather.firebase.fcm

import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.utils.TimberUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class SubFCM: FirebaseMessagingService() {
    /** 메시지 받았을 때 **/
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        when(message.data["sort"]) {
            StaticDataObject.FcmSort.FCM_PATCH.key,
            StaticDataObject.FcmSort.FCM_DAILY.key-> {
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
        CoroutineScope(Dispatchers.Default).launch {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(encodeTopic(topic))
        }
        return this
    }

    // 어드민 계정 토픽
    fun subAdminTopic() {
        try { subTopic(encodeTopic(StaticDataObject.FcmSort.FCM_ADMIN.key)) }
        catch (e: Exception) { e.stackTraceToString() }
    }

    /** 현재 위치 토픽 갱신 **/
    fun renewTopic(old: String, new: String) {
        if (old != new) {
            try { unSubTopic(old).subTopic(encodeTopic(new)) }
            catch (e: Exception) { e.stackTraceToString() }
        }
    }

    /**
     * 토픽 인코딩
     *
     * @param topic
     * @return Encoded Topic
     */
    private fun encodeTopic(topic: String): String =
        Base64.getEncoder().encodeToString(topic.toByteArray())
            .replace("=", "").replace("+", "")

    /** 현재 토큰정보 불러오기 **/
    fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) { return@OnCompleteListener }

            // Log and toast
            TimberUtil.i("testtest","FCM Token is ${task.result}")
        })
    }

    /** 새로운 토큰 발행 **/
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        TimberUtil.i("testtest","FCM New Token is $token")
    }
}