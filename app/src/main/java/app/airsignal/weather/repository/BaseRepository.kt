package app.airsignal.weather.repository

import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.network.retrofit.MyApiImpl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class BaseRepository: KoinComponent {
    private val httpClient: HttpClient by inject()
    val impl: MyApiImpl = httpClient.retrofit

    sealed class ApiState<out T> {
        data class Success<out T>(val data: T) : ApiState<T>()
        data class Error(val errorMessage: String) : ApiState<Nothing>()
        object Loading : ApiState<Nothing>()
    }
}