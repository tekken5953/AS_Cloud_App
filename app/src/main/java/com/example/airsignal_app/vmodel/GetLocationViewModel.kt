package com.example.airsignal_app.vmodel

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.airsignal_app.gps.GpsDataModel
import com.example.airsignal_app.repo.GetLocationRepo

/**
 * @author : Lee Jae Young
 * @since : 2023-06-28 오전 10:27
 **/
class GetLocationViewModel(private val repo: GetLocationRepo): BaseViewModel("주소 호출") {
    private lateinit var getResultData: LiveData<GpsDataModel>

    fun loadDataResult(context: Context) : GetLocationViewModel {
        repo.loadDataResult(context)
        return this
    }

    fun fetchData(): LiveData<GpsDataModel> {
        getResultData = repo._getLocationResult
        return getResultData
    }
}