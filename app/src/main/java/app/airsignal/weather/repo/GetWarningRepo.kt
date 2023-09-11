package app.airsignal.weather.repo

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.dao.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.dao.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.dao.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.dao.ErrorCode.ERROR_UNKNOWN
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.retrofit.HttpClient.mMyAPIImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 8:56
 **/
class GetWarningRepo: BaseRepository() {
    var _getWarningResult = MutableLiveData<ApiState<ApiModel.BroadCastWeather>>()

    @SuppressLint("MissingPermission")
    fun loadDataResult(code: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            _getWarningResult.postValue(ApiState.Loading)
            mMyAPIImpl.getBroadCast(code).enqueue(object : Callback<ApiModel.BroadCastWeather> {
                override fun onResponse(
                    call: Call<ApiModel.BroadCastWeather>,
                    response: Response<ApiModel.BroadCastWeather>
                ) {
                    val responseBody = response.body()!!

                    try {
                        if (response.isSuccessful) {
                            _getWarningResult.postValue(ApiState.Success(responseBody))
                        } else {
                            _getWarningResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                        }
                    } catch(e: Exception) {
                        _getWarningResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                    }
                }

                override fun onFailure(call: Call<ApiModel.BroadCastWeather>, t: Throwable) {
                    try {
                        _getWarningResult.postValue(ApiState.Error(ERROR_NETWORK))
                    } catch(e: Exception) {
                        _getWarningResult.postValue(ApiState.Error(ERROR_UNKNOWN))
                    }
                }
            })
        }
    }
}