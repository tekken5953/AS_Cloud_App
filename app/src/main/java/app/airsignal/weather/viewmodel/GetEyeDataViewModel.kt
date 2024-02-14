package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetEyeDataRepo
import java.io.IOException

class GetEyeDataViewModel(private val repo: GetEyeDataRepo) : BaseViewModel() {
    private var getEyeDataResult: LiveData<BaseRepository.ApiState<EyeDataModel.Entire>?>? = null

    fun loadData(sn: String, flag: String?, start: Int?, end: Int?): GetEyeDataViewModel {
        repo.loadDataResult(sn, flag, start, end)
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<EyeDataModel.Entire>?> {
        getEyeDataResult = repo._getEyeResult
        return getEyeDataResult ?: throw IOException()
    }
}