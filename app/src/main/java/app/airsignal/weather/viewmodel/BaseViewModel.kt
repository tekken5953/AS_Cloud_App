package app.airsignal.weather.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

open class BaseViewModel : ViewModel() {
    private var job: Job? = null

    override fun onCleared() {
        super.onCleared()
        if (job != null) job?.cancel()
    }
}