package app.airsignal.weather.repo

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.dao.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.dao.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.dao.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.dao.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.dao.ErrorCode.ERROR_TIMEOUT
import app.airsignal.weather.dao.ErrorCode.ERROR_UNKNOWN
import app.airsignal.weather.dao.StaticDataObject.TAG_R
import app.airsignal.weather.retrofit.ApiModel
import app.airsignal.weather.retrofit.HttpClient.mMyAPIImpl
import com.orhanobut.logger.Logger
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
            mMyAPIImpl.getForecast(lat, lng, addr)
                .enqueue(object : Callback<ApiModel.GetEntireData> {
                    override fun onResponse(
                        call: Call<ApiModel.GetEntireData>,
                        response: Response<ApiModel.GetEntireData>
                    ) {
                        try {
                            if (response.isSuccessful) {
                                val responseBody = response.body()!!
                                Logger.t(TAG_R)
                                    .d("Success API : ${ApiState.Success(responseBody).data}")
                                _getDataResult.postValue(ApiState.Success(responseBody))
                            } else {
                                _getDataResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))

                                call.cancel()
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            _getDataResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiModel.GetEntireData>,
                        t: Throwable
                    ) {
                        try {
                            t.printStackTrace()
                            _getDataResult.postValue(ApiState.Error(ERROR_GET_DATA))
                            call.cancel()
                        } catch (e: Exception) {
                            when (e) {
                                is SocketTimeoutException -> {
                                    _getDataResult.postValue(ApiState.Error(ERROR_TIMEOUT))
                                }
                                is NetworkErrorException -> {
                                    _getDataResult.postValue(ApiState.Error(ERROR_NETWORK))
                                }
                                is NullPointerException -> {
                                    _getDataResult.postValue(ApiState.Error(ERROR_UNKNOWN))
                                }
                                else -> {
                                    _getDataResult.postValue(ApiState.Error(ERROR_UNKNOWN))
                                }
                            }
                        }
                    }
                })
        }
    }
}