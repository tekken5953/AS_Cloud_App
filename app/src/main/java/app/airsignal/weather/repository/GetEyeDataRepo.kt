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
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

class GetEyeDataRepo : BaseRepository() {
    // 날씨 호출 Response Body : Map
    var _getEyeResult =
        MutableLiveData<ApiState<EyeDataModel.Measured>?>()

    fun loadDataResult(sn: String) {
        CoroutineScope(Dispatchers.Default).launch {
            _getEyeResult.postValue(ApiState.Loading)
            impl.getMeasured(sn)
                .enqueue(object : Callback<EyeDataModel.Measured> {
                    override fun onResponse(
                        call: Call<EyeDataModel.Measured>,
                        response: Response<EyeDataModel.Measured>
                    ) {
                        try {
                            if (response.isSuccessful) {
                                val responseBody = processData(response.body())
                                _getEyeResult.postValue(ApiState.Success(responseBody))
                            } else {
                                _getEyeResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                                call.cancel()
                            }
                        } catch (e: NullPointerException) {
                            _getEyeResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                        } catch (e: JsonSyntaxException) {
                            _getEyeResult.postValue(ApiState.Error(ERROR_GET_DATA))
                        }
                    }

                    override fun onFailure(
                        call: Call<EyeDataModel.Measured>,
                        t: Throwable
                    ) {
                        try {
                            _getEyeResult.postValue(ApiState.Error(ERROR_GET_DATA))
                            call.cancel()
                        } catch (e: Exception) {
                            when (e) {
                                is SocketTimeoutException ->
                                    _getEyeResult.postValue(ApiState.Error(ERROR_TIMEOUT))
                                is NetworkErrorException ->
                                    _getEyeResult.postValue(ApiState.Error(ERROR_NETWORK))
                                is NullPointerException ->
                                    _getEyeResult.postValue(ApiState.Error(ERROR_NULL_POINT))
                                else -> {
                                    _getEyeResult.postValue(ApiState.Error(ERROR_UNKNOWN))
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun processData(rawData: EyeDataModel.Measured?): EyeDataModel.Measured {
        try {
            rawData?.let { d ->

            }
        } catch (e: Exception) {
            e.stackTraceToString()
        }

        return rawData ?: throw NullPointerException()
    }
}
