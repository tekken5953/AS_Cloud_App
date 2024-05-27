package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetWeatherRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 2:02
 **/
class GetWeatherViewModel(private val repo: GetWeatherRepo) : BaseViewModel() {
    // MutableLiveData 값을 받아 View 로 전달해 줄 LiveData
    private var getDataResultData: LiveData<BaseRepository.ApiState<ApiModel.GetEntireData>?>? = null

    // MutableLiveData 값을 갱신하기 위한 함수
    fun loadData(lat: Double?, lng: Double?, addr: String?): GetWeatherViewModel {
        job?.cancel()
        job = viewModelScope.launch { repo.loadDataResult(lat, lng, addr) }
        return this
    }

    // LiveData 에 MutableLiveData 값 적용 후 View 에 전달
    fun fetchData(): LiveData<BaseRepository.ApiState<ApiModel.GetEntireData>?> {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repo._getDataResult }
            getDataResultData = result
        }

        return getDataResultData ?: throw IOException()
    }
}