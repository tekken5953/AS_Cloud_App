package app.airsignal.weather.view.widget

import app.airsignal.weather.dao.IgnoredKeyFile.fcmServerKey
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class WidgetFCM() {
    fun sendFCMMessage(sort:String, appWidgetId: Int) {
        CoroutineScope(Dispatchers.Default).launch {
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
                        data.put("sort", "widget")
                        data.put("widgetId", appWidgetId)
                        data.put("layout", sort)
                        message.put("data", data)

                        // OkHttpClient 생성
                        val client = OkHttpClient()

                        // HTTP 요청 생성
                        val request = Request.Builder()
                            .url(fcmEndpoint)
                            .post(
                                message.toString()
                                    .toRequestBody("application/json".toMediaType())
                            )
                            .addHeader("Authorization", "key=$serverKey")
                            .build()

                        withContext(Dispatchers.IO) {
                            // HTTP 요청 실행
                            client.newCall(request).execute()
                        }
                    }
                }
            }
        }
    }
}