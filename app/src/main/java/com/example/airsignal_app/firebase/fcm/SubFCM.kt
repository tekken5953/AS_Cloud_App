package com.example.airsignal_app.firebase.fcm

import android.content.Intent
import com.example.airsignal_app.dao.StaticDataObject.TAG_N
import com.example.airsignal_app.view.activity.LoginActivity
import com.example.airsignal_app.view.activity.RedirectPermissionActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.provider.FirebaseInitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class SubFCM : FirebaseMessagingService() {

    /** 메시지 받았을 때 **/
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.tag(TAG_N).d("FCM 메시지 수신 : data : ${message.data}\n" +
                "from : ${message.from}")

        val intent = Intent(this, RedirectPermissionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK)
        // 포그라운드 노티피케이션 발생
        NotificationBuilder().sendNotification(
            this,
            intent,
            message.data
        )
    }

    /** 토픽 구독 설정 **/
    fun subTopic(topic: String): SubFCM {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed : $topic"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Timber.tag(TAG_N).d(msg)
            }
        return this
    }

    /** 토픽 구독 해제 **/
    fun unSubTopic(topic: String): SubFCM {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "UnSubscribed : $topic"
                if (!task.isSuccessful) {
                    msg = "UnSubscribed failed"
                }
                Timber.tag(TAG_N).w(msg)
            }
        return this
    }

    /** 현재 토큰정보 불러오기 **/
    suspend fun getToken(): String? {
        val token = withContext(Dispatchers.IO) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.tag("Notification").w("Fetching FCM registration token failed by $task.exception")
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
        //TODO 서버에 바뀐 토큰 보내기
        Timber.tag(TAG_N).d("sendRegistrationTokenToServer($token)")
    }
}