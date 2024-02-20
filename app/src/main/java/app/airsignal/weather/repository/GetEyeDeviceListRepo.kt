package app.airsignal.weather.repository

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.network.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.network.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.network.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.network.ErrorCode.ERROR_NULL_POINT
import app.airsignal.weather.network.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.network.ErrorCode.ERROR_TIMEOUT
import app.airsignal.weather.network.ErrorCode.ERROR_UNKNOWN
import app.airsignal.weather.network.NetworkUtils.modifyCurrentHumid
import app.airsignal.weather.network.NetworkUtils.modifyCurrentRainType
import app.airsignal.weather.network.NetworkUtils.modifyCurrentTempType
import app.airsignal.weather.network.NetworkUtils.modifyCurrentWindSpeed
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.util.TimberUtil
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

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
                    TimberUtil().d("eyetest", response.body().toString())
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            _getListResult.postValue(ApiState.Success(responseBody))
                        } else {
                            _getListResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                            call.cancel()
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
                    try {
                        _getListResult.postValue(ApiState.Error(ERROR_GET_DATA))
                        call.cancel()
                    } catch (e: Exception) {
                        TimberUtil().e("eyetest",t.stackTraceToString())
                        _getListResult.postValue(ApiState.Error(t.stackTraceToString()))
                    }
                }
            })
        }
    }

}