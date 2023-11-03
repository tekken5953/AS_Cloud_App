package app.airsignal.weather.repo

import app.airsignal.weather.retrofit.HttpClient

open class BaseRepository {
    private val httpClient = HttpClient
    val impl = httpClient.getInstance(isWidget = false).setClientBuilder()

    sealed class ApiState<out T> {
        data class Success<out T>(val data: T) : ApiState<T>()
        data class Error(val errorMessage: String) : ApiState<Nothing>()
        object Loading : ApiState<Nothing>()
    }
}