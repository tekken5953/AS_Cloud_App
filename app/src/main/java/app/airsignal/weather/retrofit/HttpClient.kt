package app.airsignal.weather.retrofit

import android.annotation.SuppressLint
import app.airsignal.weather.dao.IgnoredKeyFile.hostingServerURL
import app.airsignal.weather.firebase.db.RDBLogcat
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
@SuppressLint("SetTextI18n")
object HttpClient {
    /** API Interface 생성 **/

    /** 인스턴스가 메인 메모리를 바로 참조 -> 중복생성 방지 **/
    @Volatile
    private var instance: HttpClient? = null

    /** API Instance Singleton **/
    fun getInstance(isWidget: Boolean): HttpClient {
        if (!isWidget) {
            instance ?: synchronized(HttpClient::class.java) {   // 멀티스레드에서 동시생성하는 것을 막음
                instance ?: HttpClient.also { client -> instance = client }
            }
        } else {
            try { instance = HttpClient } catch (e: Exception) {
                RDBLogcat.writeErrorANR(thread = Thread.currentThread().name,
                    msg = "인스턴스 생성 실패 - ${e.localizedMessage!!}")
            }
        }
        return instance!!
    }

    fun setClientBuilder(): MyApiImpl {
        val mMyAPIImpl: MyApiImpl

        /** OkHttp 빌드
         *
         * 클라이언트 빌더 Interceptor 구분 **/
        val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder().apply {
            retryOnConnectionFailure(retryOnConnectionFailure = true)
            connectTimeout(20, TimeUnit.SECONDS)
            readTimeout(12, TimeUnit.SECONDS)
            writeTimeout(12, TimeUnit.SECONDS)
            addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Connection", "close")
                    .build()
                chain.proceed(request)
            }.build()
        }

        /** Gson Converter 생성**/
        val gson = GsonBuilder().setLenient().create()

        /** 서버 URL 주소에 연결, GSON Convert 활성화**/
        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(hostingServerURL)
//                .baseUrl(localServerURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(clientBuilder.build())
                .build()
        }

        if (instance != null)
            mMyAPIImpl = retrofit.create(app.airsignal.weather.retrofit.MyApiImpl::class.java) // API 인터페이스 형태로 레트로핏 클라이언트 생성
        else {
            instance = HttpClient
            mMyAPIImpl = retrofit.create(app.airsignal.weather.retrofit.MyApiImpl::class.java) // API 인터페이스 형태로 레트로핏 클라이언트 생성
        }

        return mMyAPIImpl
    }
}