package com.example.airsignal_app.vmodel

import androidx.lifecycle.LiveData
import com.example.airsignal_app.repo.GetWeatherRepo
import com.example.airsignal_app.retrofit.ApiModel
import kotlinx.coroutines.*

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 2:02
 **/
class GetWeatherViewModel : BaseViewModel("날씨 데이터 호출") {

    // MutableLiveData 값을 받아 View 로 전달해 줄 LiveData
    private lateinit var getDataResultData: LiveData<ApiModel.GetEntireData>
    private val repo = GetWeatherRepo()

    // MutableLiveData 값을 갱신하기 위한 함수
    fun loadDataResult(lat: Double, lng: Double) {
        if (!isLoaded) {
            job = CoroutineScope(Dispatchers.IO).launch {
                repo.loadDataResult(lat,lng)
                isLoaded = true
            }
        }
    }

    // LiveData 에 MutableLiveData 값 적용 후 View 에 전달
    fun getDataResult(): LiveData<ApiModel.GetEntireData> {
        getDataResultData = repo._getDataResult
        return getDataResultData
    }
}