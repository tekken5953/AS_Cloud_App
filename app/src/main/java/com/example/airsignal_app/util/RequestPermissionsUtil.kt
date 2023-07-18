package com.example.airsignal_app.util

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.airsignal_app.dao.StaticDataObject.REQUEST_LOCATION
import com.example.airsignal_app.dao.StaticDataObject.REQUEST_NOTIFICATION
import com.example.airsignal_app.util.`object`.GetAppInfo.getInitLocPermission
import com.example.airsignal_app.util.`object`.SetAppInfo.setInitLocPermission
import com.orhanobut.logger.Logger
import timber.log.Timber

class RequestPermissionsUtil(private val context: Context) {

    private val permissionNetWork = Manifest.permission.INTERNET

    /** 위치 권한 SDK 버전 29 이상**/
//    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsLocation = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsLocationBackground =
       Manifest.permission.ACCESS_BACKGROUND_LOCATION


//    /** 위치 권한 SDK 버전 29 이하**/
//    @TargetApi(Build.VERSION_CODES.P)
//    private val permissionsLocationDownApi29Impl = arrayOf(
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION
//    )

    /** 알림 권한 SDK 버전 33 이상**/
    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionNotification = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    /** 위치정보 권한 요청**/
    fun requestLocation() {
        Log.d("TAG_P","Request Location")
        ActivityCompat.requestPermissions(
            context as Activity,
            permissionsLocation,
            REQUEST_LOCATION
        )
    }

    /** 알림 권한 요청 **/
    fun requestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, permissionNotification[0])
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    permissionNotification,
                    REQUEST_NOTIFICATION
                )
                Log.d("TAG_P","Request Notification Permission")
            }
        }
    }

    /**위치권한 허용 여부 검사**/
    fun isLocationPermitted(): Boolean {
        for (perm in permissionsLocation) {
            if (ContextCompat.checkSelfPermission(context, perm)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Logger.t("TAG_P").i("Location is false")
                return false
            }
        }
        Logger.t("TAG_P").i("Location is true")
        return true
    }

    /**알림권한 허용 여부 검사**/
    fun isNotificationPermitted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (perm in permissionNotification) {
                return ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            return true
        }
        return true
    }

    /** 인터넷 허용 여부 검사 **/
    fun isNetworkPermitted(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            context,
            permissionNetWork
        ) == PackageManager.PERMISSION_GRANTED
        Logger.t("TAG_P").i("Network is $result")
        return result
    }

    /** 위치 권한이 거부되어 있는지 확인 **/
    fun isLocationDenied(): Boolean {
        for (perm in permissionsLocation) {
            if (ContextCompat.checkSelfPermission(context, perm)
                != PackageManager.PERMISSION_DENIED
            ) {
                Timber.tag("TAG_P").i(ContextCompat.checkSelfPermission(context, perm).toString())
                return false
            }
        }
        Timber.tag("TAG_P").i("isLocationDenied is True")
        return true
    }

    fun isNotiDenied(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (perm in permissionNotification) {
                Timber.tag("TAG_P").i(ContextCompat.checkSelfPermission(context, perm).toString())
                return ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) == PackageManager.PERMISSION_DENIED
            }
        } else {
            return true
        }
        return true
    }

    fun isShouldShowRequestPermissionRationale(activity: Activity): Boolean {
        return when (getInitLocPermission(activity)) {
            "" -> {
                Log.d("TAG_P","isShouldShowRequestPermissionRationale is Second")
                true
            }
            "Second" -> {
                Log.d("TAG_P","isShouldShowRequestPermissionRationale is Done")
                true
            }
            else -> {
                Log.d("TAG_P","isShouldShowRequestPermissionRationale is False")
                shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isPermissionsLocationBackground(): Boolean {
        return ContextCompat.checkSelfPermission(context, permissionsLocationBackground)==
                PackageManager.PERMISSION_GRANTED
    }
}