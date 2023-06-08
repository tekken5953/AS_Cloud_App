package com.example.airsignal_app.repo

import androidx.lifecycle.MutableLiveData
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient.mMyAPIImpl
import com.example.airsignal_app.view.ToastUtils
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 2:03
 **/
class GetWeatherRepo : BaseRepository() {
    // 날씨 호출 Response Body : Map
    var _getDataResult =
        MutableLiveData<ApiModel.GetEntireData>()

    fun loadDataResult(lat: Double?, lng: Double?, addr: String?) {
        val getDataMap: Call<ApiModel.GetEntireData> = mMyAPIImpl.getForecast(lat,lng,addr)
        getDataMap.enqueue(object : Callback<ApiModel.GetEntireData> {
            override fun onResponse(
                call: Call<ApiModel.GetEntireData>,
                response: Response<ApiModel.GetEntireData>
            ) {
                loadSuccessMapData(_getDataResult, response)
                Logger.t("Timber").d(response.body().toString())
            }
            override fun onFailure(call: Call<ApiModel.GetEntireData>, t: Throwable) {
                Logger.e("날씨 데이터 호출 실패 : " + t.localizedMessage)
                call.timeout()
                call.cancel()
            }
        })
    }
}