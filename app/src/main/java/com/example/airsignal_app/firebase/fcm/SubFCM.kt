package com.example.airsignal_app.firebase.fcm

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.airsignal_app.view.activity.LoginActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class SubFCM : FirebaseMessagingService() {

    /** 메시지 받았을 때 **/
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.tag("Notification").d("FCM 메시지 수신")

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK)
        // 포그라운드 노티피케이션 발생
        NotificationBuilder().sendNotification(
            this,
            intent,
            message,
            "AS-Cloud FCM Test Msg",
            System.currentTimeMillis())
    }

    /** 토픽 구독 설정 **/
    fun subTopic(topic: String): SubFCM {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Timber.tag("Notification").d(msg)
            }
        return this
    }

    /** 토픽 구독 해제 **/
    fun unSubTopic(topic: String): SubFCM {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "UnSubscribed"
                if (!task.isSuccessful) {
                    msg = "UnSubscribed failed"
                }
                Timber.tag("Notification").w(msg)
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
                Timber.tag("Notification").d("FCM 토큰 : $token")
            }).result
        }
        return token
    }

    /** 새로운 토큰 발행 **/
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        //TODO 서버에 바뀐 토큰 보내기
        Timber.tag("Notification").d("sendRegistrationTokenToServer($token)")
    }
}