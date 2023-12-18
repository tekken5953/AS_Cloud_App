package app.airsignal.weather.firebase.fcm

import app.airsignal.weather.firebase.fcm.NotificationBuilder.Companion.FCM_DAILY
import app.airsignal.weather.firebase.fcm.NotificationBuilder.Companion.FCM_EVENT
import app.airsignal.weather.firebase.fcm.NotificationBuilder.Companion.FCM_PATCH
import app.airsignal.weather.view.widget.WidgetProvider
import app.airsignal.weather.view.widget.WidgetProvider42
import app.core_databse.db.sp.GetAppInfo
import app.utils.LoggerUtil
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

    /** 메시지 받았을 때 **/
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        when(message.data["sort"]) {
            FCM_PATCH, FCM_DAILY -> {
                NotificationBuilder().sendNotification(applicationContext,message.data)
            }
            FCM_EVENT -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val isLandingEnable =
                        GetAppInfo.isLandingNotification(applicationContext)
                    if (isLandingEnable) {
                        NotificationBuilder().sendNotification(applicationContext,message.data)
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
                LoggerUtil().d("TAG_FCM","subscribe $topic")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerUtil().e("TAG_FCM","subscribe fail to $topic")
        }
        return this
    }

    /** 토픽 구독 해제 **/
    private fun unSubTopic(topic: String): SubFCM {
        val encodedStream = encodeTopic(topic)
        CoroutineScope(Dispatchers.Default).launch {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(encodedStream)
            LoggerUtil().d("TAG_FCM","unsubscribe $topic")
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
        LoggerUtil().d("fcm_noti","old is $old new is $new")
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
        println(token)
    }
}