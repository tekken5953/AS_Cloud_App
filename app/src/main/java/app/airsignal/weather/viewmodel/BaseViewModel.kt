package app.airsignal.weather.viewmodel

import androidx.lifecycle.ViewModel
import app.airsignal.weather.util.TimberUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import okhttp3.internal.notify

open class BaseViewModel : ViewModel() {
    protected var job: Job? = null

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    fun cancelJob() {
        onCleared()
    }
}