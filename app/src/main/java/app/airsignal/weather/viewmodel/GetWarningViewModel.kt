package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetWarningRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetWarningViewModel(private val repo: GetWarningRepo): BaseViewModel() {
    private var getResultData: LiveData<BaseRepository.ApiState<ApiModel.BroadCastWeather>>? = null

    fun loadDataResult(code: Int) : GetWarningViewModel {
        viewModelScope.launch { repo.loadDataResult(code) }
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<ApiModel.BroadCastWeather>> {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repo._getWarningResult }
            getResultData = result
        }

        return getResultData ?: throw IOException()
    }
}