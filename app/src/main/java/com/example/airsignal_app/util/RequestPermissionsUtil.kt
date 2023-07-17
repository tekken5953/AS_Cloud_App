package com.example.airsignal_app.util

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.airsignal_app.dao.StaticDataObject.REQUEST_NOTIFICATION
import com.orhanobut.logger.Logger
import timber.log.Timber

class RequestPermissionsUtil(private val context: Context) {

    private val permissionNetWork = Manifest.permission.INTERNET

    /** 위치 권한 SDK 버전 29 이상**/
//    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsLocationUpApi29Impl = arrayOf(
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
//        if (Build.VERSION.SDK_INT >= 29) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationUpApi29Impl[0]
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationUpApi29Impl[1]
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationUpApi29Impl[2]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    permissionsLocationUpApi29Impl,
                    1
                )
                Logger.t("TAG_P").i("Request Location Permissio")
            }
//        }
//        else {
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    permissionsLocationDownApi29Impl[0]
//                ) != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(
//                    context,
//                    permissionsLocationDownApi29Impl[1]
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    context as Activity,
//                    permissionsLocationDownApi29Impl,
//                    2
//                )
//                Logger.t("TAG_P").i("Request Location Permission by Down to 29")
//            }
//        }
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
                Logger.t("TAG_P").i("Request Notification Permission")
            }
        }
    }

    /**위치권한 허용 여부 검사**/
    fun isLocationPermitted(): Boolean {
//        if (Build.VERSION.SDK_INT >= 29) {
            for (perm in permissionsLocationUpApi29Impl) {
                if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
//        } else {
//            for (perm in permissionsLocationDownApi29Impl) {
//                if (ContextCompat.checkSelfPermission(context, perm)
//                    != PackageManager.PERMISSION_GRANTED
//                ) {
//                    return false
//                }
//            }
//        }
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
        return ContextCompat.checkSelfPermission(
            context,
            permissionNetWork
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** 위치 권한이 거부되어 있는지 확인 **/
    fun isLocationDenied(): Boolean {
//        if (Build.VERSION.SDK_INT >= 29) {
            for (perm in permissionsLocationUpApi29Impl) {
                if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_DENIED
                ) {
                    Timber.tag("TAG_P").i(ContextCompat.checkSelfPermission(context, perm).toString())
                    return false
                }
            }
//        } else {
//            for (perm in permissionsLocationDownApi29Impl) {
//                if (ContextCompat.checkSelfPermission(context, perm)
//                    != PackageManager.PERMISSION_DENIED
//                ) {
//                    Timber.tag("TAG_P").i(ContextCompat.checkSelfPermission(context, perm).toString())
//                    return false
//                }
//            }
//        }
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

    fun isShouldShowRequestPermissionRationale(activity: Activity): String {
//        if (Build.VERSION.SDK_INT >= 29) {
            for (perm in permissionsLocationUpApi29Impl) {
                return if (shouldShowRequestPermissionRationale(activity,perm)) {
                    // 이전에 권한 요청 거부
                    "denied once"
                } else {
                    // 이전에 "다시 묻지 않기"를 선택하여 권한 요청을 받지 않는 경우
                    "denied twice"
                }
            }
//        } else {
//            for (perm in permissionsLocationDownApi29Impl) {
//                return if (shouldShowRequestPermissionRationale(activity,perm)) {
//                    // 이전에 다시 묻지 않기를 선택
//                    "denied once"
//                } else {
//                    // 이전에 "다시 묻지 않기"를 선택하여 권한 요청을 받지 않는 경우
//                    "denied twice"
//                }
//            }
//        }
        return "null"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isPermissionsLocationBackground(): Boolean {
        return ContextCompat.checkSelfPermission(context, permissionsLocationBackground)==
                PackageManager.PERMISSION_GRANTED
    }
}