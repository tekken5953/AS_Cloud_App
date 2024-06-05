package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetWarningRepo
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetWarningViewModel(private val repo: GetWarningRepo): BaseViewModel() {
    val getResultData: LiveData<BaseRepository.ApiState<ApiModel.BroadCastWeather>>
    get() = repo._getWarningResult

    fun loadDataResult(code: Int) : GetWarningViewModel {
        viewModelScope.launch { repo.loadDataResult(code) }
        return this
    }
}