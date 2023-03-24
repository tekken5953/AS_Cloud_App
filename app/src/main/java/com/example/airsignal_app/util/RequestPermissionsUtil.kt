package com.example.airsignal_app.util

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.airsignal_app.dao.StaticDataObject.REQUEST_LOCATION
import com.example.airsignal_app.dao.StaticDataObject.REQUEST_NOTIFICATION

class RequestPermissionsUtil(activity: Context) {
    val context = activity

    /** 위치 권한 SDK 버전 29 이상**/
    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsLocationUpApi29Impl = arrayOf (
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    /** 위치 권한 SDK 버전 29 이하**/
    @TargetApi(Build.VERSION_CODES.P)
    private val permissionsLocationDownApi29Impl = arrayOf (
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    /** 알림 권한 SDK 버전 33 이상**/
    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionNotification =
        Manifest.permission.ACCESS_NOTIFICATION_POLICY

    /** 위치정보 권한 요청**/
    fun requestLocation() {
        if (Build.VERSION.SDK_INT >= 29) {
            if(ActivityCompat.checkSelfPermission(context,
                    permissionsLocationUpApi29Impl[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                    permissionsLocationUpApi29Impl[1]) != PackageManager.PERMISSION_GRANTED  ||
                ActivityCompat.checkSelfPermission(context,
                    permissionsLocationUpApi29Impl[2]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as Activity, permissionsLocationUpApi29Impl, REQUEST_LOCATION)
            }
        } else {
            if(ActivityCompat.checkSelfPermission(context,
                    permissionsLocationDownApi29Impl[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                    permissionsLocationDownApi29Impl[1]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as Activity, permissionsLocationDownApi29Impl, REQUEST_LOCATION)
            }
        }
    }

    /** 알림 권한 요청 **/
    fun requestNotification() {
        if (ActivityCompat.checkSelfPermission(
                context,
                permissionNotification
            ) != PackageManager.PERMISSION_GRANTED)
            {
            ActivityCompat.requestPermissions(context as Activity,
                arrayOf(permissionNotification), REQUEST_NOTIFICATION)
        }
    }

    /**알림권한의 허용 여부검사**/
    fun isLocationPermitted(): Boolean {
        if (Build.VERSION.SDK_INT >= 29) {
            for (perm in permissionsLocationUpApi29Impl) {
                if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        } else {
            for (perm in permissionsLocationDownApi29Impl) {
                if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }

        return true
    }
}