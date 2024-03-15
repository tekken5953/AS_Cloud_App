package app.airsignal.weather.repository

import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.network.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.network.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.network.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.util.TimberUtil
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class GetEyeDeviceListRepo : BaseRepository() {
    // 날씨 호출 Response Body : Map
    var _getListResult =
        MutableLiveData<ApiState<List<EyeDataModel.Device>?>>()

    fun loadDataResult() {
        CoroutineScope(Dispatchers.Default).launch {
            _getListResult.postValue(ApiState.Loading)
            impl.deviceList.enqueue(object : Callback<List<EyeDataModel.Device>> {
                override fun onResponse(
                    call: Call<List<EyeDataModel.Device>>,
                    response: Response<List<EyeDataModel.Device>>
                ) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            _getListResult.postValue(ApiState.Success(sortData(responseBody)))
                        } else {
                            _getListResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                        }
                    } catch (e: NullPointerException) {
                        _getListResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                        TimberUtil().e("eyetest",e.stackTraceToString())
                    } catch (e: JsonSyntaxException) {
                        _getListResult.postValue(ApiState.Error(ERROR_GET_DATA))
                        TimberUtil().e("eyetest",e.stackTraceToString())
                    }
                }

                override fun onFailure(
                    call: Call<List<EyeDataModel.Device>>,
                    t: Throwable
                ) {
                    TimberUtil().e("eyetest",t.stackTraceToString())
                    try {
                        _getListResult.postValue(ApiState.Error(ERROR_GET_DATA))
                    } catch (e: Exception) {
                        _getListResult.postValue(ApiState.Error(t.stackTraceToString()))
                    }
                }
            })
        }
    }

    private fun sortData(rawData: List<EyeDataModel.Device>?): List<EyeDataModel.Device>?  {
        return try {
            rawData ?. let { list ->
                val running = list.sortedByDescending { it.detail?.power == true }
                val runningAndMaster = running.sortedByDescending { it.isMaster }
                runningAndMaster
            } ?: throw IOException()
        } catch (e: Exception) {
            e.stackTraceToString()
            rawData
        }
    }
}