package com.example.airsignal_app.vmodel

import androidx.lifecycle.ViewModel
import com.example.airsignal_app.dao.StaticDataObject
import kotlinx.coroutines.Job
import timber.log.Timber

open class BaseViewModel(msg: String?) : ViewModel() {
    private var message = msg
    var job: Job? = null

    override fun onCleared() {
        super.onCleared()
        if (job != null)
            job?.cancel()
        if (message != null)
            Timber.tag(StaticDataObject.TAG_R).i("%s 뷰모델 인스턴스 소멸", message)
    }
}