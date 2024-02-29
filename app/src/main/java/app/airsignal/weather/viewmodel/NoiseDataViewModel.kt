package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.NoiseDataRepo
import java.io.IOException

class NoiseDataViewModel(private val repo: NoiseDataRepo): BaseViewModel() {
    private var getResultData: LiveData<BaseRepository.ApiState<List<AdapterModel.NoiseDetailItem>?>>? = null

    fun loadDataResult(sn: String, flag: String?, start:Int ?, end: Int?) : NoiseDataViewModel {
        repo.loadDataResult(sn, flag, start, end)
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<List<AdapterModel.NoiseDetailItem>?>> {
        getResultData = repo._getNoiseResult
        return getResultData ?: throw IOException()
    }
}