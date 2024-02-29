package app.airsignal.weather.repository

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.network.ErrorCode
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

class NoiseDataRepo : BaseRepository() {
    // 날씨 호출 Response Body : Map
    var _getNoiseResult =
        MutableLiveData<ApiState<List<AdapterModel.NoiseDetailItem>?>>()

    fun loadDataResult(flag: String?, start: Int?, end: Int?) {
        CoroutineScope(Dispatchers.Default).launch {
            _getNoiseResult.postValue(ApiState.Loading)
            impl.getNoise(flag, start, end).enqueue(object : Callback<List<AdapterModel.NoiseDetailItem>>{
                override fun onResponse(
                    call: Call<List<AdapterModel.NoiseDetailItem>>,
                    response: Response<List<AdapterModel.NoiseDetailItem>>
                ) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            _getNoiseResult.postValue(ApiState.Success(responseBody))
                        } else {
                            _getNoiseResult.postValue(ApiState.Error(ErrorCode.ERROR_API_PROTOCOL))
                            call.cancel()
                        }
                    } catch (e: NullPointerException) {
                        _getNoiseResult.postValue(ApiState.Error(ErrorCode.ERROR_SERVER_CONNECTING))
                    } catch (e: JsonSyntaxException) {
                        _getNoiseResult.postValue(ApiState.Error(ErrorCode.ERROR_GET_DATA))
                    }
                }

                override fun onFailure(
                    call: Call<List<AdapterModel.NoiseDetailItem>>,
                    t: Throwable
                ) {
                    try {
                        _getNoiseResult.postValue(ApiState.Error(ErrorCode.ERROR_GET_DATA))
                        call.cancel()
                    } catch (e: Exception) {
                        when (e) {
                            is SocketTimeoutException ->
                                _getNoiseResult.postValue(ApiState.Error(ErrorCode.ERROR_TIMEOUT))
                            is NetworkErrorException ->
                                _getNoiseResult.postValue(ApiState.Error(ErrorCode.ERROR_NETWORK))
                            is NullPointerException ->
                                _getNoiseResult.postValue(ApiState.Error(ErrorCode.ERROR_NULL_POINT))
                            else -> {
                                _getNoiseResult.postValue(ApiState.Error(ErrorCode.ERROR_UNKNOWN))
                            }
                        }
                    }
                }
            })
        }
    }

    private fun processData(flag: String?, rawData: List<AdapterModel.NoiseDetailItem>?)
    : List<AdapterModel.NoiseDetailItem> {
        try {
            rawData?.let { data ->
                data.forEachIndexed { index, item ->
                }
            }
        } catch (e: Exception) {
            e.stackTraceToString()
        }

        return rawData ?: throw NullPointerException()
    }
}
