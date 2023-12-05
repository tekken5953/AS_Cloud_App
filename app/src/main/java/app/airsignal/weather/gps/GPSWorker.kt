package app.airsignal.weather.gps

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * @author : Lee Jae Young
 * @since : 2023-03-29 오전 9:06
 **/
class GPSWorker(private val context: Context, params: WorkerParameters)
    : CoroutineWorker(context,params) {

    /** 워크 매니저 백그라운드 반복 **/
    override suspend fun doWork(): Result {
        return try {
            GetLocation(context).getGpsInBackground()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}