package app.airsignal.weather.view.widget

import android.content.Context
import app.airsignal.weather.dao.IgnoredKeyFile.fcmServerKey
import app.airsignal.weather.firebase.db.RDBLogcat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class WidgetFCM(private val context: Context) {
    suspend fun sendFCMMessage(): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                CoroutineScope(Dispatchers.Default).launch {
                    if (task.isSuccessful) {
                        val token = task.result
                        // FCM 서버 엔드포인트
                        val fcmEndpoint =
                            "https://fcm.googleapis.com/fcm/send"
                        // FCM 서버에 요청을 보내기 위한 인증 키
                        val serverKey = fcmServerKey

                        // 메시지 생성
                        val message = JSONObject()
                        message.put("to", "Bearer $token")
                        message.put("priority", "high")

                        // 메시지 데이터 추가 (원하는 내용으로 수정)
                        val data = JSONObject()
                        data.put("title", "FCM Message")
                        data.put("body", "Sent FCM message.")
                        data.put("sort", "widget")
                        message.put("data", data)

                        // OkHttpClient 생성
                        val client = OkHttpClient()

                        // HTTP 요청 생성
                        val request = Request.Builder()
                            .url(fcmEndpoint)
                            .post(
                                message.toString().toRequestBody("application/json".toMediaType())
                            )
                            .addHeader("Authorization", "key=$serverKey")
                            .build()

                        withContext(Dispatchers.IO) {
                            // HTTP 요청 실행
                            val response = client.newCall(request).execute()

                            // 응답 확인
                            if (response.isSuccessful) {
                                RDBLogcat.writeWidgetHistory(
                                    context.applicationContext,
                                    "fcm",
                                    "true : ${response.body?.string()}"
                                )
                            } else {
                                RDBLogcat.writeWidgetHistory(
                                    context.applicationContext,
                                    "fcm",
                                    "fail : ${response.body?.string()}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}