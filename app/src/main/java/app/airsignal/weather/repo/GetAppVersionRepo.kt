package app.airsignal.weather.repo

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import app.airsignal.weather.dao.ErrorCode.ERROR_API_PROTOCOL
import app.airsignal.weather.dao.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.dao.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.dao.ErrorCode.ERROR_UNKNOWN
import app.airsignal.weather.retrofit.ApiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 8:56
 **/
class GetAppVersionRepo: BaseRepository() {
    var _getAppVersionResult = MutableLiveData<ApiState<ApiModel.AppVersion>>()

    @SuppressLint("MissingPermission")
    fun loadDataResult() {
        CoroutineScope(Dispatchers.Default).launch {
            _getAppVersionResult.postValue(ApiState.Loading)
            impl.version.enqueue(object : Callback<ApiModel.AppVersion> {
                override fun onResponse(
                    call: Call<ApiModel.AppVersion>,
                    response: Response<ApiModel.AppVersion>
                ) {
                    val responseBody = response.body()!!

                    try {
                        if (response.isSuccessful)
                            _getAppVersionResult.postValue(ApiState.Success(responseBody))
                        else
                            _getAppVersionResult.postValue(ApiState.Error(ERROR_API_PROTOCOL))
                    } catch(e: IOException) {
                        _getAppVersionResult.postValue(ApiState.Error(ERROR_SERVER_CONNECTING))
                    }
                }

                override fun onFailure(call: Call<ApiModel.AppVersion>, t: Throwable) {
                    try {
                        _getAppVersionResult.postValue(ApiState.Error(ERROR_NETWORK))
                    } catch(e: Exception) {
                        _getAppVersionResult.postValue(ApiState.Error(ERROR_UNKNOWN))
                    }
                }
            })
        }
    }
}