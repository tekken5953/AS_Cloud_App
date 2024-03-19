package app.airsignal.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.repository.GetEyeDataRepo
import app.airsignal.weather.util.TimberUtil
import kotlinx.coroutines.launch
import java.io.IOException

class GetEyeDataViewModel(private val repo: GetEyeDataRepo) : BaseViewModel() {
    private var getEyeDataResult: LiveData<BaseRepository.ApiState<EyeDataModel.Entire>?>? = null

    fun loadData(sn: String, flag: String?, start: Int?, end: Int?): GetEyeDataViewModel {
        TimberUtil().w("lifecycle_test","아이 디테일 데이터 호출")
        job?.cancel()
        job = viewModelScope.launch {
            repo.loadDataResult(sn, flag, start, end)
        }
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<EyeDataModel.Entire>?> {
        getEyeDataResult = repo._getEyeResult
        return getEyeDataResult ?: throw IOException()
    }
}