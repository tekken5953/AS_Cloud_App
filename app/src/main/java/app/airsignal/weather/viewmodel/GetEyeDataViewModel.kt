package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetEyeDataRepo
import java.io.IOException

class GetEyeDataViewModel(private val repo: GetEyeDataRepo) : BaseViewModel() {
    private var getEyeDataResult: LiveData<BaseRepository.ApiState<EyeDataModel.Measured>?>? = null

    fun loadData(sn: String): GetEyeDataViewModel {
        repo.loadDataResult(sn)
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<EyeDataModel.Measured>?> {
        getEyeDataResult = repo._getEyeResult
        return getEyeDataResult ?: throw IOException()
    }
}