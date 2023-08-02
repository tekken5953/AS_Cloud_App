package com.example.airsignal_app.repo

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.example.airsignal_app.dao.ErrorCode.ERROR_NETWORK
import com.example.airsignal_app.retrofit.ApiModel
import com.example.airsignal_app.retrofit.HttpClient.mMyAPIImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 8:56
 **/
class GetAppVersionRepo: BaseRepository() {
    var _getAppVersionResult = MutableLiveData<ApiState<ApiModel.AppVersion>>()

    @SuppressLint("MissingPermission")
    fun loadDataResult() {
        CoroutineScope(Dispatchers.Default).launch {
            mMyAPIImpl.version.enqueue(object : Callback<ApiModel.AppVersion> {
                override fun onResponse(
                    call: Call<ApiModel.AppVersion>,
                    response: Response<ApiModel.AppVersion>
                ) {
                    val responseBody = response.body()!!

                    if (response.isSuccessful) {
//                        Logger.t(TAG_R).d("Success to get API : ${ApiState.Success(responseBody).data}")

                        _getAppVersionResult.postValue(ApiState.Success(responseBody))
                    } else {
//                        Logger.t(TAG_R).d("Fail to get API : ${ApiState.Success(responseBody).data}")

                        _getAppVersionResult.postValue(ApiState.Error(""))
                    }
                }

                override fun onFailure(call: Call<ApiModel.AppVersion>, t: Throwable) {
//                    Logger.t(TAG_R).d("Fail to get API : ${t.localizedMessage}")

                    _getAppVersionResult.postValue(ApiState.Error(ERROR_NETWORK))
                }
            })
        }
    }
}