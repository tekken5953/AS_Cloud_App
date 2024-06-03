package app.airsignal.weather.repository

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.network.ErrorCode
import app.airsignal.weather.network.NetworkUtils
import app.airsignal.weather.network.retrofit.ApiModel
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 2:03
 **/
class GetWeatherRepo : BaseRepository() {
    // 날씨 호출 Response Body : Map
    var _getDataResult =
        MutableLiveData<ApiState<ApiModel.GetEntireData>?>()

    fun loadDataResult(lat: Double?, lng: Double?, addr: String?) {
        CoroutineScope(Dispatchers.Default).launch {
            _getDataResult.postValue(ApiState.Loading)
            impl.getForecast(lat, lng, addr).enqueue(object : Callback<ApiModel.GetEntireData> {
                    override fun onResponse(
                        call: Call<ApiModel.GetEntireData>,
                        response: Response<ApiModel.GetEntireData>) {
                        kotlin.runCatching {
                            if (response.isSuccessful)
                                _getDataResult.postValue(ApiState.Success(processData(response.body())))
                            else {
                                _getDataResult.postValue(ApiState.Error(ErrorCode.ERROR_API_PROTOCOL))
                                call.cancel()
                            }
                        }.onFailure { exception ->
                            when(exception) {
                                is NullPointerException -> _getDataResult.postValue(ApiState.Error(ErrorCode.ERROR_SERVER_CONNECTING))
                                is JsonSyntaxException -> _getDataResult.postValue(ApiState.Error(ErrorCode.ERROR_GET_DATA))
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiModel.GetEntireData>,
                        t: Throwable) {
                        kotlin.runCatching {
                            _getDataResult.postValue(ApiState.Error(ErrorCode.ERROR_GET_DATA))
                            call.cancel()
                        }.onFailure { exception ->
                            when (exception) {
                                is SocketTimeoutException ->
                                    _getDataResult.postValue(ApiState.Error(ErrorCode.ERROR_TIMEOUT))
                                is NetworkErrorException ->
                                    _getDataResult.postValue(ApiState.Error(ErrorCode.ERROR_NETWORK))
                                is NullPointerException ->
                                    _getDataResult.postValue(ApiState.Error(ErrorCode.ERROR_NULL_POINT))
                            }
                        }
                    }
                })
        }
    }

    private fun processData(rawData: ApiModel.GetEntireData?): ApiModel.GetEntireData {
        kotlin.runCatching {
            rawData?.let { d ->
                d.current.rainType = NetworkUtils.modifyCurrentRainType(d.current.rainType,d.realtime[0].rainType)
                d.current.temperature = NetworkUtils.modifyCurrentTempType(d.current.temperature, d.realtime[0].temp)
                d.current.windSpeed = NetworkUtils.modifyCurrentWindSpeed(d.current.windSpeed, d.realtime[0].windSpeed)
                d.current.humidity = NetworkUtils.modifyCurrentHumid(d.current.humidity, d.realtime[0].humid)
            }
        }.exceptionOrNull()?.stackTraceToString()

        return rawData ?: throw NullPointerException()
    }
}