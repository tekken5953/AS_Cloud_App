package com.example.airsignal_app.gps

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.lang.Exception

/**
 * @author : Lee Jae Young
 * @since : 2023-03-29 오전 9:06
 **/
class GpsWorker(mContext: Context, params: WorkerParameters) : CoroutineWorker(mContext,params) {
    private val context = mContext
    /** 워크 매니저 백그라운드 반복 **/
    override suspend fun doWork(): Result {
        return try {
            GetLocation(context).getLocation()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}