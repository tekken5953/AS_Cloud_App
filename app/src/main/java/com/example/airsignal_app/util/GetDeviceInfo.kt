package com.example.airsignal_app.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * @author : Lee Jae Young
 * @since : 2023-05-12 오후 2:35
 **/
class GetDeviceInfo {
    private val TAG = "BUILD_USER_INFO"

    fun buildInfo() {
        Log.i(TAG, "BOARD = " + Build.BOARD)
        Log.i(TAG, "BRAND = " + Build.BRAND)
        Log.i(TAG, "CPU_ABI = " + Build.CPU_ABI)
        Log.i(TAG, "DEVICE = " + Build.DEVICE)
        Log.i(TAG, "DISPLAY = " + Build.DISPLAY)
        Log.i(TAG, "FINGERPRINT = " + Build.FINGERPRINT)
        Log.i(TAG, "HOST = " + Build.HOST)
        Log.i(TAG, "ID = " + Build.ID)
        Log.i(TAG, "MANUFACTURER = " + Build.MANUFACTURER)
        Log.i(TAG, "MODEL = " + Build.MODEL)
        Log.i(TAG, "PRODUCT = " + Build.PRODUCT)
        Log.i(TAG, "TAGS = " + Build.TAGS)
        Log.i(TAG, "TYPE = " + Build.TYPE)
        Log.i(TAG, "USER = " + Build.USER)
        Log.i(TAG, "VERSION.RELEASE = " + Build.VERSION.RELEASE)
    }

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