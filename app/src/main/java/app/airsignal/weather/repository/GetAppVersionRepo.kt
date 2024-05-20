package app.airsignal.weather.repository

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.network.ErrorCode
import app.airsignal.weather.network.retrofit.ApiModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 8:56
 **/
class GetAppVersionRepo: BaseRepository() {
    var _getAppVersionResult = MutableLiveData<ApiState<ApiModel.AppVersion>>()

    fun loadDataResult() {
        _getAppVersionResult.postValue(ApiState.Loading)
        impl.version.enqueue(object : Callback<ApiModel.AppVersion> {
            override fun onResponse(
                call: Call<ApiModel.AppVersion>,
                response: Response<ApiModel.AppVersion>
            ) {
                try {
                    val responseBody = response.body()
                    responseBody?.let {
                        if (response.isSuccessful) {
                            CoroutineScope(Dispatchers.Default).launch {
                                _getAppVersionResult.postValue(ApiState.Success(responseBody))
                            }
                        } else _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_API_PROTOCOL))
                    } ?: run {
                        _getAppVersionResult.postValue(ApiState.Error("RESPONSE_IS_NULL"))
                    }
                } catch (e: IOException) {
                    _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_SERVER_CONNECTING))
                }
            }

            override fun onFailure(call: Call<ApiModel.AppVersion>, t: Throwable) {
                try {
                    _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_NETWORK))
                } catch (e: Exception) {
                    when (e) {
                        is SocketTimeoutException ->
                            _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_TIMEOUT))
                        is NetworkErrorException ->
                            _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_NETWORK))
                        is NullPointerException ->
                            _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_NULL_POINT))
                        else -> {
                            _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_UNKNOWN))
                        }
                    }
                }
            }
        })
    }
}