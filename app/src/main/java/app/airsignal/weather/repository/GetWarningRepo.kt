package app.airsignal.weather.repository

import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.network.ErrorCode
import app.airsignal.weather.network.retrofit.ApiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 8:56
 **/
class GetWarningRepo: BaseRepository() {
    var _getWarningResult = MutableLiveData<ApiState<ApiModel.BroadCastWeather>>()

    fun loadDataResult(code: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            _getWarningResult.postValue(ApiState.Loading)
            impl.getBroadCast(code).enqueue(object : Callback<ApiModel.BroadCastWeather> {
                override fun onResponse(
                    call: Call<ApiModel.BroadCastWeather>,
                    response: Response<ApiModel.BroadCastWeather>) {
                    val responseBody = response.body()

                    kotlin.runCatching {
                        if (response.isSuccessful)
                            responseBody?.let {_getWarningResult.postValue(ApiState.Success(responseBody))}
                        else
                            _getWarningResult.postValue(ApiState.Error(ErrorCode.ERROR_API_PROTOCOL))
                    }.onFailure {
                        _getWarningResult.postValue(ApiState.Error(ErrorCode.ERROR_SERVER_CONNECTING))
                    }
                }

                override fun onFailure(call: Call<ApiModel.BroadCastWeather>, t: Throwable) {
                    kotlin.runCatching {
                        _getWarningResult.postValue(ApiState.Error(ErrorCode.ERROR_NETWORK))
                    }.onFailure {
                        _getWarningResult.postValue(ApiState.Error(ErrorCode.ERROR_UNKNOWN))
                    }
                }
            })
        }
    }
}
