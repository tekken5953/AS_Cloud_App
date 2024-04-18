package app.airsignal.weather.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

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