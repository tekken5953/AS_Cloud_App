package app.airsignal.weather.api.retrofit

import app.airsignal.weather.api.NetworkIgnored
import app.airsignal.weather.api.NetworkIgnored.hostingServerURL
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.apache.http.conn.ConnectionPoolTimeoutException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object HttpClient {
    private val clientBuilder: OkHttpClient.Builder =
        OkHttpClient.Builder().apply {
            retryOnConnectionFailure(retryOnConnectionFailure = false)
            connectionPool(ConnectionPool())
            connectTimeout(NetworkIgnored.connectTimeout, TimeUnit.SECONDS)
            readTimeout(NetworkIgnored.readTimeout, TimeUnit.SECONDS)
            writeTimeout(NetworkIgnored.writeTimeout, TimeUnit.SECONDS)
            addInterceptor { chain ->
                chain.proceed(
                    kotlin.runCatching {
                        chain.request().newBuilder()
                            .addHeader("Connection", NetworkIgnored.headerConnection)
                            .addHeader("Platform", NetworkIgnored.headerPlatform)
                            .build()
                    }.getOrElse { chain.request() }
                )
            }.build()
    }

    private val gson = getRawGsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()

    val retrofit: MyApiImpl =
        Retrofit.Builder()
            .baseUrl(hostingServerURL)
            .addConverterFactory(gsonConverterFactory() ?: GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
            .create(MyApiImpl::class.java)

    private fun gsonConverterFactory(): GsonConverterFactory? {
        val gson = getRawGsonBuilder()
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

    private fun getRawGsonBuilder(): GsonBuilder =
        GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .serializeNulls()
}