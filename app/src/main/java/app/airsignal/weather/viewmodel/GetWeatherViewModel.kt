package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.api.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetWeatherRepo
import kotlinx.coroutines.launch

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 2:02
 **/
class GetWeatherViewModel(private val repo: GetWeatherRepo) : BaseViewModel() {
    // MutableLiveData 값을 받아 View 로 전달해 줄 LiveData
    val getDataResultData: LiveData<BaseRepository.ApiState<ApiModel.GetEntireData>?>
    get() = repo._getDataResult

    // MutableLiveData 값을 갱신하기 위한 함수
    fun loadData(lat: Double?, lng: Double?, addr: String?): GetWeatherViewModel {
        job?.cancel()
        job = viewModelScope.launch { repo.loadDataResult(lat, lng, addr) }
        return this
    }
}