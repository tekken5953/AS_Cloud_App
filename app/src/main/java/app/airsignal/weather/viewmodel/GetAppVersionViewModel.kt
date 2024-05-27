package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetAppVersionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetAppVersionViewModel(private val repo: GetAppVersionRepo): BaseViewModel() {
    private var getResultData: LiveData<BaseRepository.ApiState<ApiModel.AppVersion>>? = null

    fun loadDataResult() : GetAppVersionViewModel {
        viewModelScope.launch { repo.loadDataResult() }
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<ApiModel.AppVersion>> {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repo._getAppVersionResult }
            getResultData = result
        }

        return getResultData ?: throw IOException()
    }
}