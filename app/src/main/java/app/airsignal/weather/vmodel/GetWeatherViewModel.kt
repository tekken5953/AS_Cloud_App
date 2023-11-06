package app.airsignal.weather.vmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.repo.BaseRepository
import app.airsignal.weather.repo.GetWeatherRepo
import app.airsignal.weather.retrofit.ApiModel
import kotlinx.coroutines.launch
import okio.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 2:02
 **/
class GetWeatherViewModel(private val repo: GetWeatherRepo) : BaseViewModel("날씨 데이터 호출") {
    // MutableLiveData 값을 받아 View 로 전달해 줄 LiveData
    private var getDataResultData: LiveData<BaseRepository.ApiState<ApiModel.GetEntireData>?>? = null

    // MutableLiveData 값을 갱신하기 위한 함수
    fun loadData(lat: Double?, lng: Double?, addr: String?): GetWeatherViewModel {
        repo.loadDataResult(lat, lng, addr)
        return this
    }

    // LiveData 에 MutableLiveData 값 적용 후 View 에 전달
    fun fetchData(): LiveData<BaseRepository.ApiState<ApiModel.GetEntireData>?> {
        try { viewModelScope.launch { getDataResultData = repo._getDataResult }
        } catch (e: Exception) { e.printStackTrace() }

        return getDataResultData ?: throw IOException()
    }
}