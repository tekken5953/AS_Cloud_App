package com.example.airsignal_app.repo

import androidx.lifecycle.MutableLiveData
import com.example.airsignal_app.dao.StaticDataObject.CODE_INVALID_TOKEN
import com.example.airsignal_app.dao.StaticDataObject.CODE_SERVER_DOWN
import com.example.airsignal_app.dao.StaticDataObject.CODE_SERVER_OK
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.retrofit.HttpClient
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

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
                    data.postValue(CODE_SERVER_OK.toString())
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
                    data.postValue(response.body() as TD)
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
        } catch (e: java.lang.NumberFormatException) {
            e.printStackTrace()
            RDBLogcat.writeBadRequest(
                "NumberFormatException",
                "Error body : ${
                    response.errorBody().toString()
                }\nStack Trace : ${e.stackTraceToString()}"
            )
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
            RDBLogcat.writeBadRequest(
                "NullPointerException",
                "Error body : ${
                    response.errorBody().toString()
                }\nStack Trace : ${e.stackTraceToString()}"
            )
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
                    data.postValue(mList as TD)
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