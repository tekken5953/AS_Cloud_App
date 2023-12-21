package app.airsignal.core_repository

import android.accounts.NetworkErrorException
import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.airsignal.core_network.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.core_network.ErrorCode.ERROR_GET_DATA
import app.airsignal.core_network.ErrorCode.ERROR_NETWORK
import app.airsignal.core_network.ErrorCode.ERROR_NULL_POINT
import app.airsignal.core_network.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.core_network.ErrorCode.ERROR_TIMEOUT
import app.airsignal.core_network.ErrorCode.ERROR_UNKNOWN
import app.airsignal.core_network.NetworkUtils.modifyCurrentHumid
import app.airsignal.core_network.NetworkUtils.modifyCurrentRainType
import app.airsignal.core_network.NetworkUtils.modifyCurrentTempType
import app.airsignal.core_network.NetworkUtils.modifyCurrentWindSpeed
import app.airsignal.core_network.retrofit.ApiModel
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.toHeaderList
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
            impl.getForecast(lat, lng, addr)
                .enqueue(object : Callback<ApiModel.GetEntireData> {
                    override fun onResponse(
                        call: Call<ApiModel.GetEntireData>,
                        response: Response<ApiModel.GetEntireData>
                    ) {
                        try {
                            if (response.isSuccessful) {
//                                Log.d("TAG_R","raw : ${response.raw()}\nheader : ${response.headers().toHeaderList()}")
                                val responseBody = processData(response.body())
                                _getDataResult.postValue(ApiState.Success(responseBody))
                            } else {
                                _getDataResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                                call.cancel()
                            }
                        } catch (e: NullPointerException) {
                            _getDataResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                        } catch (e: JsonSyntaxException) {
                            _getDataResult.postValue(ApiState.Error(ERROR_GET_DATA))
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiModel.GetEntireData>,
                        t: Throwable
                    ) {
                        try {
                            _getDataResult.postValue(ApiState.Error(ERROR_GET_DATA))
                            call.cancel()
                        } catch (e: Exception) {
                            when (e) {
                                is SocketTimeoutException ->
                                    _getDataResult.postValue(ApiState.Error(ERROR_TIMEOUT))
                                is NetworkErrorException ->
                                    _getDataResult.postValue(ApiState.Error(ERROR_NETWORK))
                                is NullPointerException ->
                                    _getDataResult.postValue(ApiState.Error(ERROR_NULL_POINT))
                                else -> {
                                    _getDataResult.postValue(ApiState.Error(ERROR_UNKNOWN))
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun processData(rawData: ApiModel.GetEntireData?): ApiModel.GetEntireData {
        try {
            rawData?.let { d ->
                d.current.rainType = modifyCurrentRainType(d.current.rainType,d.realtime[0].rainType)
                d.current.temperature = modifyCurrentTempType(d.current.temperature, d.realtime[0].temp)
                d.current.windSpeed = modifyCurrentWindSpeed(d.current.windSpeed, d.realtime[0].windSpeed)
                d.current.humidity = modifyCurrentHumid(d.current.humidity, d.realtime[0].humid)
            }
        } catch (e: Exception) {
            e.stackTraceToString()
        }

        return rawData ?: throw NullPointerException()
    }
}