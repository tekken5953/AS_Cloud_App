package app.airsignal.weather.repo

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.dao.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.dao.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.dao.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.dao.ErrorCode.ERROR_NULL_POINT
import app.airsignal.weather.dao.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.dao.ErrorCode.ERROR_TIMEOUT
import app.airsignal.weather.dao.ErrorCode.ERROR_UNKNOWN
import app.airsignal.weather.dao.StaticDataObject.TAG_R
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.firebase.db.RDBLogcat.writeErrorANR
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.util.`object`.DataTypeParser.modifyCurrentHumid
import app.airsignal.weather.util.`object`.DataTypeParser.modifyCurrentRainType
import app.airsignal.weather.util.`object`.DataTypeParser.modifyCurrentTempType
import app.airsignal.weather.util.`object`.DataTypeParser.modifyCurrentWindSpeed
import com.google.gson.JsonSyntaxException
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
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
                                val responseBody = processData(response.body())
                                Logger.t(TAG_R)
                                    .d("Success API : ${ApiState.Success(responseBody).data}")
                                _getDataResult.postValue(ApiState.Success(responseBody))
                            } else {
                                writeErrorANR(Thread.currentThread().toString(),response.body().toString())
                                _getDataResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                                call.cancel()
                            }
                        } catch (e: NullPointerException) {
                            Logger.t(TAG_R).e(e.stackTraceToString())
                            _getDataResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                        } catch (e: JsonSyntaxException) {
                            Logger.t(TAG_R).e(response.body().toString())
                            _getDataResult.postValue(ApiState.Error(ERROR_GET_DATA))
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiModel.GetEntireData>,
                        t: Throwable
                    ) {
                        Logger.t(TAG_R).e(t.stackTraceToString())
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
                                    Logger.t(TAG_R).e("onFailure : ERROR_UNKNOWN")
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
            writeErrorANR("processData by repository",e.stackTraceToString())
            e.stackTraceToString()
        }

        return rawData ?: throw NullPointerException()
    }
}