package com.example.airsignal_app.repo

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.airsignal_app.dao.StaticDataObject.CODE_INVALID_TOKEN
import com.example.airsignal_app.dao.StaticDataObject.CODE_SERVER_DOWN
import com.example.airsignal_app.dao.StaticDataObject.CODE_SERVER_OK
import com.example.airsignal_app.dao.StaticDataObject.CODE_TIMEOUT_EXCEPTION
import com.example.airsignal_app.retrofit.HttpClient
import com.orhanobut.logger.Logger
import retrofit2.Response
import timber.log.Timber
import java.net.SocketTimeoutException

open class BaseRepository {
    private val httpClient = HttpClient

    init {
        httpClient.getInstance()
    }

    inline fun <reified T> loadSuccessStringData(
        data: MutableLiveData<String>,
        response: Response<T>,
    ) {
        try {
            when (response.code()) {
                CODE_SERVER_OK -> {
                    data.value = CODE_SERVER_OK.toString()
                }
                CODE_SERVER_DOWN -> {
                    Logger.e("서버 연결 불가 : ${response.code()}")
                }
                CODE_INVALID_TOKEN -> {
                    Logger.w("만료된 토큰 : ${response.code()}")
                }
                else -> {
                    Logger.w("통신 성공 but 예상치 못한 에러 발생: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inline fun <reified TD, TR> loadSuccessMapData(
        data: MutableLiveData<TD>,
        response: Response<TR>,
    ) {
        try {
            when (response.code()) {
                CODE_SERVER_OK -> {
                    data.value = response.body() as TD
                    Log.d("Timber",data.value.toString())
                }
                CODE_SERVER_DOWN -> {
                    Logger.e("서버 연결 불가 : ${response.code()}")
                }
                CODE_INVALID_TOKEN -> {
                    Logger.w("만료된 토큰 : ${response.code()}")
                }
                else -> {
                    Logger.w("통신 성공 but 예상치 못한 에러 발생: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inline fun <reified TD, TR> loadSuccessListData(
        data: MutableLiveData<TD>,
        response: Response<TR>,
    ) {
        try {
            val mList: TR? = response.body()
            when (response.code()) {
                CODE_SERVER_OK -> {
                    data.value = mList as TD
                }
                CODE_SERVER_DOWN -> {
                    Logger.e("서버 연결 불가 : ${response.code()}")
                }
                CODE_INVALID_TOKEN -> {
                    Logger.w("만료된 토큰 : ${response.code()}")
                    data.value = null
                }
                else -> {
                    Logger.w("통신 성공 but 예상치 못한 에러 발생: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}