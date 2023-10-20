package app.airsignal.weather.vmodel

import androidx.lifecycle.LiveData
import app.airsignal.weather.repo.BaseRepository
import app.airsignal.weather.repo.GetAppVersionRepo
import app.airsignal.weather.retrofit.ApiModel
import okio.IOException

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetAppVersionViewModel(private val repo: GetAppVersionRepo): BaseViewModel("앱 버전") {
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