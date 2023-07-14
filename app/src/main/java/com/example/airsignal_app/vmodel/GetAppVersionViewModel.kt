package com.example.airsignal_app.vmodel

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.airsignal_app.gps.GpsDataModel
import com.example.airsignal_app.repo.BaseRepository
import com.example.airsignal_app.repo.GetAppVersionRepo
import com.example.airsignal_app.retrofit.ApiModel

/**
 * @author : Lee Jae Young
 * @since : 2023-07-14 오전 9:05
 **/
class GetAppVersionViewModel(private val repo: GetAppVersionRepo): BaseViewModel("앱 버전") {
    private lateinit var getResultData: LiveData<BaseRepository.ApiState<ApiModel.AppVersion>>

    fun loadDataResult() : GetAppVersionViewModel {
        repo.loadDataResult()
        return this
    }

    fun fetchData(): LiveData<BaseRepository.ApiState<ApiModel.AppVersion>> {
        getResultData = repo._getAppVersionResult
        return getResultData
    }
}