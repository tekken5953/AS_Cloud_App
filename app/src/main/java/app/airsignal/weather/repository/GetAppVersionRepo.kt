package app.airsignal.weather.repository

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.network.ErrorCode
import app.airsignal.weather.network.retrofit.ApiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
                kotlin.runCatching {
                    val responseBody = response.body()
                    responseBody?.let {
                        if (response.isSuccessful) {
                            CoroutineScope(Dispatchers.Default).launch {
                                _getAppVersionResult.postValue(ApiState.Success(responseBody))
                            }
                        } else _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_API_PROTOCOL))
                    } ?: run { _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_NULL_RESPONSE)) }
                }.onFailure { exception ->
                    if (exception is IOException) _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_SERVER_CONNECTING))
                }
            }

            override fun onFailure(call: Call<ApiModel.AppVersion>, t: Throwable) {
                kotlin.runCatching {
                    _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_NETWORK))
                }.onFailure { exception ->
                    when (exception) {
                        is SocketTimeoutException -> _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_TIMEOUT))
                        is NetworkErrorException -> _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_NETWORK))
                        is NullPointerException -> _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_NULL_POINT))
                        else -> _getAppVersionResult.postValue(ApiState.Error(ErrorCode.ERROR_UNKNOWN))
                    }
                }
            }
        })
    }
}