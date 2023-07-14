package com.example.airsignal_app.repo

import androidx.lifecycle.MutableLiveData
import com.example.airsignal_app.dao.StaticDataObject.TAG_R
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient.mMyAPIImpl
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

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
            mMyAPIImpl.getForecast(lat, lng, addr)
                .enqueue(object : Callback<ApiModel.GetEntireData> {
                    override fun onResponse(
                        call: Call<ApiModel.GetEntireData>,
                        response: Response<ApiModel.GetEntireData>
                    ) {
                        val responseBody = response.body()!!

                        if (response.isSuccessful) {
                            Logger.t(TAG_R).d("Success API : ${ApiState.Success(responseBody).data}")
                            _getDataResult.postValue(ApiState.Success(responseBody))
                        } else {
                            Logger.t(TAG_R).e("Data Error API : ${ApiState.Success(responseBody).data}")
                            _getDataResult.postValue(ApiState.Error("API ERROR OCCURRED"))
                            call.cancel()
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiModel.GetEntireData>,
                        t: Throwable
                    ) {
                        Logger.t(TAG_R).e("API NetworkError : ${t.stackTraceToString()}")
                        _getDataResult.postValue(ApiState.Error("Network Error"))
                        call.cancel()
                    }
                })
        }
    }
}