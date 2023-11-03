package app.airsignal.weather.gps

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.airsignal.weather.view.widget.WidgetProvider

class GpsWorkManager(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val loc = GetLocation(applicationContext)
        if (loc.isNetWorkConnected()) {
            loc.getGpsInBackground()
        }
        else return Result.failure()
        return Result.success()
    }
}