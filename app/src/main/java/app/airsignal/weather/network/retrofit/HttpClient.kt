package app.airsignal.weather.network.retrofit

import app.airsignal.weather.network.NetworkIgnored.hostingServerURL
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
    private val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(retryOnConnectionFailure = false)
        connectionPool(ConnectionPool())
        connectTimeout(8, TimeUnit.SECONDS)
        readTimeout(8, TimeUnit.SECONDS)
        writeTimeout(8, TimeUnit.SECONDS)
        addInterceptor { chain ->
            try {
                val request = chain.request().newBuilder()
                    .addHeader("Connection", "close")
                    .addHeader("Platform", "android")
                    .build()
                chain.proceed(request)
            } catch (e: SocketTimeoutException) {
                chain.proceed(chain.request())
                throw e
            }
            catch (e: ConnectionPoolTimeoutException) {
                chain.proceed(chain.request())
                throw e
            }
        }.build()
    }

    private val gson =
        GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .setPrettyPrinting()
            .serializeNulls()
            .create()

    val retrofit: MyApiImpl =
        Retrofit.Builder()
            .baseUrl(hostingServerURL)
            .addConverterFactory(gsonConverterFactory() ?: GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
            .create(MyApiImpl::class.java)

    private fun gsonConverterFactory(): GsonConverterFactory? {
        val gson = GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .serializeNulls()
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