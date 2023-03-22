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
import com.example.airsignal_app.util.IgnoredKeyFile.REQUEST_LOCATION

class RequestPermissionsUtil(activity: Context) {
    val context = activity

    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsLocationUpApi29Impl = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    @TargetApi(Build.VERSION_CODES.P)
    private val permissionsLocationDownApi29Impl = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
//    private val permissionNotification = arrayOf(if (Build.VERSION.SDK_INT >= 33) {
////        Manifest.permission.POST_NOTIFICATIONS
//    } else { null })

    // 위치정보 권한 요청
    fun requestLocation() {
        if (Build.VERSION.SDK_INT >= 29) {
            if(ActivityCompat.checkSelfPermission(context,
                    permissionsLocationUpApi29Impl[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                    permissionsLocationUpApi29Impl[1]) != PackageManager.PERMISSION_GRANTED) {
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

//    fun requestNotification() {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                permissionNotification.toString()
//            ) != PackageManager.PERMISSION_GRANTED)
//            {
//            ActivityCompat.requestPermissions(context, permissionNotification, REQUEST_NOTIFICATION)
//        }
//    }

    //권한을 허락 받아야함
    fun isPermitted(): Boolean {
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