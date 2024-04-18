package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.SetEyeDeviceAliasRepo
import java.io.IOException

class SetEyeDeviceAliasViewModel(private val repo: SetEyeDeviceAliasRepo): BaseViewModel() {
    private var getResultData: LiveData<BaseRepository.ApiState<String?>>? = null

    fun loadDataResult(userId: String, sn: String, alias: String) : SetEyeDeviceAliasViewModel {
        repo.loadDataResult(alias, sn, userId)
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<String?>> {
        getResultData = repo._setAliasResult
        return getResultData ?: throw IOException()
    }
}