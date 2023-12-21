package app.airsignal.core_viewmodel

import androidx.lifecycle.LiveData
import app.airsignal.core_network.retrofit.ApiModel
import app.airsignal.core_repository.BaseRepository
import app.airsignal.core_repository.GetAppVersionRepo
import java.io.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetAppVersionViewModel(private val repo: GetAppVersionRepo): BaseViewModel() {
    private var getResultData: LiveData<BaseRepository.ApiState<ApiModel.AppVersion>>? = null

    fun loadDataResult() : GetAppVersionViewModel {
        repo.loadDataResult()
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<ApiModel.AppVersion>> {
        getResultData = repo._getAppVersionResult
        return getResultData ?: throw IOException()
    }
}