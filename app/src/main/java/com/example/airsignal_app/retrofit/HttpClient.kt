package com.example.airsignal_app.retrofit

import android.annotation.SuppressLint
import com.example.airsignal_app.dao.IgnoredKeyFile.springServerURL
import com.example.airsignal_app.dao.StaticDataObject.TAG_R
import com.google.gson.GsonBuilder
import com.orhanobut.logger.Logger
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Singleton
@SuppressLint("SetTextI18n")
object HttpClient {
    /** API Interface 생성 **/
    lateinit var mMyAPIImpl: MyApiImpl

    /** 인스턴스가 메인 메모리를 바로 참조 -> 중복생성 방지 **/
    @Volatile
    private var instance: HttpClient? = null

    /** API Instance Singleton **/
    fun getInstance(): HttpClient {
        instance ?: synchronized(HttpClient::class.java) {   // 멀티스레드에서 동시생성하는 것을 막음
            instance ?: HttpClient.also {
                instance = it
                Logger.t(TAG_R).d("API Instance 생성")
            }
        }
        /** OkHttp 빌드
         *
         * 클라이언트 빌더 Interceptor 구분 **/
        val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder().apply {
            retryOnConnectionFailure(retryOnConnectionFailure = false)
//            addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
//                override fun log(message: String) {
//                    if (!message.startsWith("{") && !message.startsWith("[")) {
//                        Timber.tag("Timber").i(message)
//                        return
//                    }
//                    try {
//                        // Timber 와 Gson setPrettyPrinting 를 이용해 json 을 보기 편하게 표시해준다.
//                        LoggerUtil().getInstance().logJsonTimberDebug("Timber", message)
//                        return
//                    } catch (m: JsonSyntaxException) {
//                        Timber.tag("Timber").e(m.localizedMessage!!.toString())
//                        Timber.tag("Timber").e(message)
//                        return
//                    }
//                }
//            }).apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            })
             .build()
//            addInterceptor { chain ->
//                val request = chain.request()
//                val response = try {
//                    chain.proceed(request)
//                } catch (e: MainActivity.CustomTimeOutException) {
//                    e.localizedMessage
//                }
//                response as Response
//            }.build()
            addInterceptor {
                val request = it.request().newBuilder()
                    .addHeader("Connection", "close")
                    .build()

                it.proceed(request)
            }.build()
        }

        /** Gson Converter 생성**/
        val gson = GsonBuilder().setLenient().create()

        /** 서버 URL 주소에 연결, GSON Convert 활성화**/
        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(springServerURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(clientBuilder.build())
                .build()
        }

        if (instance != null)
            mMyAPIImpl = retrofit.create(MyApiImpl::class.java) // API 인터페이스 형태로 레트로핏 클라이언트 생성

        return instance!!
    }
}