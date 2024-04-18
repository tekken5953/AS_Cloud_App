package app.airsignal.weather.repository

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.network.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.network.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.network.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.network.ErrorCode.ERROR_UNKNOWN
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
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

    @SuppressLint("MissingPermission")
    fun loadDataResult(code: Int) {
        _getWarningResult.postValue(ApiState.Loading)
        impl.getBroadCast(code).enqueue(object : Callback<ApiModel.BroadCastWeather> {
            override fun onResponse(
                call: Call<ApiModel.BroadCastWeather>,
                response: Response<ApiModel.BroadCastWeather>
            ) {
                val responseBody = response.body()!!

                try {
                    if (response.isSuccessful)
                        _getWarningResult.postValue(ApiState.Success(responseBody))
                    else
                        _getWarningResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                } catch (e: Exception) {
                    _getWarningResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                }
            }

            override fun onFailure(call: Call<ApiModel.BroadCastWeather>, t: Throwable) {
                try {
                    t.printStackTrace()
                    _getWarningResult.postValue(ApiState.Error(ERROR_NETWORK))
                } catch (e: Exception) {
                    _getWarningResult.postValue(ApiState.Error(ERROR_UNKNOWN))
                }
            }
        })
    }
}
