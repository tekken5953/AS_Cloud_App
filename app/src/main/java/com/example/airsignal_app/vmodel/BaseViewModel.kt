package com.example.airsignal_app.vmodel

import androidx.lifecycle.ViewModel
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Job

open class BaseViewModel(msg: String?) : ViewModel() {
    var message = msg
    var job: Job? = null

    var isLoaded = false

    override fun onCleared() {
        super.onCleared()
        if (job != null)
            job?.cancel()
        if (message != null)
         Logger.i("$message 뷰모델 인스턴스 소멸")
    }
}