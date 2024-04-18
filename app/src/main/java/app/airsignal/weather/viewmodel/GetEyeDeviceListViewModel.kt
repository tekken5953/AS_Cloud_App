package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetEyeDeviceListRepo
import kotlinx.coroutines.launch

class GetEyeDeviceListViewModel(private val repo: GetEyeDeviceListRepo): BaseViewModel() {
    private var getResultData: LiveData<BaseRepository.ApiState<List<EyeDataModel.Device>?>>? = null

    fun loadDataResult(userId: String) {
        job?.cancel()
        job = viewModelScope.launch {
            repo.loadDataResult(userId)
        }
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<List<EyeDataModel.Device>?>>? {
        getResultData = repo._getListResult
        return getResultData
    }
}