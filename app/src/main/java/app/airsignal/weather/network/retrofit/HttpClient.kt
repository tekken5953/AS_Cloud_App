package app.airsignal.weather.network.retrofit

import android.annotation.SuppressLint
import app.airsignal.weather.network.NetworkIgnored.hostingServerURL
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


@SuppressLint("SetTextI18n")
object HttpClient {
    /** 인스턴스가 메인 메모리를 바로 참조 -> 중복생성 방지 **/
    @Volatile
    private var instance: HttpClient? = null

    /** API Instance Singleton **/
    fun getInstance(isWidget: Boolean): HttpClient {
        if (!isWidget) {
            instance ?: synchronized(HttpClient::class.java) {   // 멀티스레드에서 동시생성하는 것을 막음
                instance ?: HttpClient. also { client -> instance = client }
            }
        } else {
            try { instance = HttpClient } catch (e: Exception) { e.printStackTrace() }
        }
        return instance!!
    }

    fun setClientBuilder(): MyApiImpl {
        /** OkHttp 빌드
         *
         * 클라이언트 빌더 Interceptor 구분 **/
        val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder().apply {
            retryOnConnectionFailure(retryOnConnectionFailure = false)
            connectionPool(ConnectionPool())
            connectTimeout(12, TimeUnit.SECONDS)
            readTimeout(8, TimeUnit.SECONDS)
            writeTimeout(8, TimeUnit.SECONDS)
            addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Connection", "close")
                    .addHeader("Platform", "android")
                    .build()
                chain.proceed(request)
            }.build()
        }

        /** Gson Converter 생성**/
        val gson = GsonBuilder().setLenient().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setPrettyPrinting().create()

        /** 서버 URL 주소에 연결, GSON Convert 활성화**/
        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(hostingServerURL)
//                .baseUrl(localServerURL)
                .addConverterFactory(gsonConverterFactory() ?: GsonConverterFactory.create(gson))
                .client(clientBuilder.build())
                .build()
        }

        if (instance == null) instance = HttpClient // API 인터페이스 형태로 레트로핏 클라이언트 생성

        return retrofit.create(MyApiImpl::class.java)
    }

    private fun gsonConverterFactory(): GsonConverterFactory? {
        val gson = GsonBuilder()
            .setLenient()
            .registerTypeAdapter(LocalDateTime::class.java,
                JsonDeserializer { json, _, _ ->
                    LocalDateTime.parse(
                        json.asString,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    )
                })
            .registerTypeAdapter(LocalDate::class.java,
                JsonDeserializer { json, _, _ ->
                    LocalDate.parse(
                        json.asString,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )
                })
            .registerTypeAdapter(LocalTime::class.java,
                JsonDeserializer { json, _, _ ->
                    LocalTime.parse(
                        json.asString,
                        DateTimeFormatter.ofPattern("HH:mm:ss")
                    )
                })
            .create()

        return GsonConverterFactory.create(gson)
    }
}