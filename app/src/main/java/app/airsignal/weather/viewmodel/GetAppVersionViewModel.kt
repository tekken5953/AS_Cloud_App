package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetAppVersionRepo
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetAppVersionViewModel(private val repo: GetAppVersionRepo): BaseViewModel() {
    val getResultData: LiveData<BaseRepository.ApiState<ApiModel.AppVersion>>
    get() = repo._getAppVersionResult

    fun loadDataResult() : GetAppVersionViewModel {
        viewModelScope.launch { repo.loadDataResult() }
        return this
    }
}