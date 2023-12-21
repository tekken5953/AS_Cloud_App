package app.airsignal.core_viewmodel

import androidx.lifecycle.LiveData
import app.airsignal.core_network.retrofit.ApiModel
import app.airsignal.core_repository.BaseRepository
import app.airsignal.core_repository.GetWarningRepo
import java.io.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetWarningViewModel(private val repo: GetWarningRepo): BaseViewModel() {
    private var getResultData: LiveData<BaseRepository.ApiState<ApiModel.BroadCastWeather>>? = null

    fun loadDataResult(code: Int) : GetWarningViewModel {
        repo.loadDataResult(code)
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<ApiModel.BroadCastWeather>> {
        getResultData = repo._getWarningResult
        return getResultData ?: throw IOException()
    }
}