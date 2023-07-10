package com.example.airsignal_app.repo

import com.example.airsignal_app.retrofit.HttpClient

open class BaseRepository {
    private val httpClient = HttpClient

    init { httpClient.getInstance(isWidget = false) }

    sealed class ApiState<out T> {
        data class Success<out T>(val data: T) : ApiState<T>()
        data class Error(val errorMessage: String) : ApiState<Nothing>()
        object Loading : ApiState<Nothing>()
    }
}