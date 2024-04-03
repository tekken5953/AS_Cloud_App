package app.airsignal.weather.repository

import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.network.retrofit.MyApiImpl

open class BaseRepository {
    val impl: MyApiImpl = HttpClient.retrofit

    sealed class ApiState<out T> {
        data class Success<out T>(val data: T) : ApiState<T>()
        data class Error(val errorMessage: String) : ApiState<Nothing>()
        object Loading : ApiState<Nothing>()
    }
}