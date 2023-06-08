package com.example.airsignal_app.vmodel

import androidx.lifecycle.LiveData
import com.example.airsignal_app.repo.GetWeatherRepo
import com.example.airsignal_app.retrofit.ApiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 2:02
 **/
class GetWeatherViewModel : BaseViewModel("날씨 데이터 호출") {

    // MutableLiveData 값을 받아 View 로 전달해 줄 LiveData
    private lateinit var getDataResultData: LiveData<ApiModel.GetEntireData>
    private val repo = GetWeatherRepo()

    // MutableLiveData 값을 갱신하기 위한 함수
    fun loadDataResult(lat: Double?, lng: Double?, addr: String?) : GetWeatherViewModel {
        val threadPoolDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
        job = CoroutineScope(threadPoolDispatcher).launch {
            repo.loadDataResult(lat, lng, addr)
        }
        runBlocking {
            job!!.join()
            threadPoolDispatcher.close()
        }
        return this
    }

    // LiveData 에 MutableLiveData 값 적용 후 View 에 전달
    fun getDataResult(): LiveData<ApiModel.GetEntireData> {

        getDataResultData = repo._getDataResult
        return getDataResultData
    }
}