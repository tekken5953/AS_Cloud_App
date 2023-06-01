package com.example.airsignal_app.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * @author : Lee Jae Young
 * @since : 2023-05-12 오후 2:35
 **/
class GetDeviceInfo {
    private val TAG = "buildUserInfo"

    @SuppressLint("HardwareIds")
    fun androidID(activity: Context): String {
        val id = Settings.Secure.getString(
            activity.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        Log.i(TAG,id)
        return id
    }
}