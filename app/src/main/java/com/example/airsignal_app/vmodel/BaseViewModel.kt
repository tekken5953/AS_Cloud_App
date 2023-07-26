package com.example.airsignal_app.vmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

open class BaseViewModel(msg: String?) : ViewModel() {
    private var message = msg
    var job: Job? = null

    override fun onCleared() {
        super.onCleared()
        if (job != null)
            job?.cancel()
//        if (message != null)
//            Timber.tag(StaticDataObject.TAG_R).i("%s 뷰모델 인스턴스 소멸", message)
    }
}