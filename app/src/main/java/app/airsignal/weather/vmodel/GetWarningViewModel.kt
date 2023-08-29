package app.airsignal.weather.vmodel

import androidx.lifecycle.LiveData
import app.airsignal.weather.repo.BaseRepository
import app.airsignal.weather.repo.GetAppVersionRepo
import app.airsignal.weather.repo.GetWarningRepo
import app.airsignal.weather.retrofit.ApiModel

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetWarningViewModel(private val repo: GetWarningRepo): BaseViewModel("기상 특보") {
    private lateinit var getResultData: LiveData<BaseRepository.ApiState<ApiModel.BroadCastWeather>>

    fun loadDataResult(code: Int) : GetWarningViewModel {
        repo.loadDataResult(code)
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<ApiModel.BroadCastWeather>> {
        getResultData = repo._getWarningResult
        return getResultData
    }
}