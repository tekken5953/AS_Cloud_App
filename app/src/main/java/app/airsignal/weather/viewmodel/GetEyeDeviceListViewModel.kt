package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetEyeDeviceListRepo
import app.airsignal.weather.util.TimberUtil
import kotlinx.coroutines.launch
import java.io.IOException

class GetEyeDeviceListViewModel(private val repo: GetEyeDeviceListRepo): BaseViewModel() {
    private var getResultData: LiveData<BaseRepository.ApiState<List<EyeDataModel.Device>?>>? = null

    fun loadDataResult() : GetEyeDeviceListViewModel {
        TimberUtil().w("lifecycle_test", "리스트 데이터 호출")
        job?.cancel()
        job = viewModelScope.launch {
            repo.loadDataResult()
        }
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<List<EyeDataModel.Device>?>> {
        getResultData = repo._getListResult
        return getResultData ?: throw IOException()
    }
}