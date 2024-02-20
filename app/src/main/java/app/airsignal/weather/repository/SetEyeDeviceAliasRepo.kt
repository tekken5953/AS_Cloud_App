package app.airsignal.weather.repository

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.network.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.network.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.network.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.network.ErrorCode.ERROR_NULL_POINT
import app.airsignal.weather.network.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.network.ErrorCode.ERROR_TIMEOUT
import app.airsignal.weather.network.ErrorCode.ERROR_UNKNOWN
import app.airsignal.weather.util.TimberUtil
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

class SetEyeDeviceAliasRepo : BaseRepository() {
    // 날씨 호출 Response Body : Map
    var _setAliasResult =
        MutableLiveData<ApiState<String?>>()

    fun loadDataResult(alias: String, sn: String) {
        CoroutineScope(Dispatchers.Default).launch {
            _setAliasResult.postValue(ApiState.Loading)
            impl.updateAlias(alias, sn).enqueue(object : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    TimberUtil().d("eyetest",response.body().toString())
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            _setAliasResult.postValue(ApiState.Success(responseBody))
                        } else {
                            _setAliasResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                            call.cancel()
                        }
                    } catch (e: NullPointerException) {
                        _setAliasResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                    } catch (e: JsonSyntaxException) {
                        _setAliasResult.postValue(ApiState.Error(ERROR_GET_DATA))
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    TimberUtil().e("eyetest",t.stackTraceToString())
                    try {
                        _setAliasResult.postValue(ApiState.Error(ERROR_GET_DATA))
                        call.cancel()
                    } catch (e: Exception) {
                        when (e) {
                            is SocketTimeoutException ->
                                _setAliasResult.postValue(ApiState.Error(ERROR_TIMEOUT))
                            is NetworkErrorException ->
                                _setAliasResult.postValue(ApiState.Error(ERROR_NETWORK))
                            is NullPointerException ->
                                _setAliasResult.postValue(ApiState.Error(ERROR_NULL_POINT))
                            else -> {
                                _setAliasResult.postValue(ApiState.Error(ERROR_UNKNOWN))
                            }
                        }
                    }
                }
            })
        }
    }
}