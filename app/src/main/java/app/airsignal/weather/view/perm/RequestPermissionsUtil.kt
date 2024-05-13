package app.airsignal.weather.view.perm

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.sp.GetAppInfo

class RequestPermissionsUtil(private val context: Context) {

    private val permissionNetWork = Manifest.permission.INTERNET
    private val tagRequestPermission = 0x0000001

    /** 위치 권한 SDK 버전 29 이상**/
    private val permissionsLocation = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /** 백그라운드 위치 권한 31 이상 **/
    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsLocationBackground =
        Manifest.permission.ACCESS_BACKGROUND_LOCATION

    /** 알림 권한 SDK 버전 33 이상**/
    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionNotification = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val blePermissionArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    /** 위치정보 권한 요청**/
    fun requestLocation() {
        try{
            requestPermissions(
                context as Activity,
                permissionsLocation,
                StaticDataObject.REQUEST_LOCATION
            )
        } catch (e: Exception) {
            e.printStackTrace()
            RDBLogcat.writeErrorANR(Thread.currentThread().toString(),"requestLocation error ${e.stackTraceToString()}")
        }
    }

    /** 알림 권한 요청 **/
    fun requestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, permissionNotification[0])
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    context as Activity,
                    permissionNotification,
                    StaticDataObject.REQUEST_NOTIFICATION
                )
            }
        }
    }

    /**위치권한 허용 여부 검사**/
    fun isLocationPermitted(): Boolean {
        for (perm in permissionsLocation) {
            if (ContextCompat.checkSelfPermission(context, perm)
                != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    /**알림권한 허용 여부 검사**/
    fun isNotificationPermitted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (perm in permissionNotification) {
                return ContextCompat.checkSelfPermission(
                    context, perm) == PackageManager.PERMISSION_GRANTED
            }
        } else return true
        return true
    }

    /** 인터넷 허용 여부 검사 **/
    fun isNetworkPermitted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permissionNetWork
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** 권한 요청 거부 횟수에 따른 반환 **/
    fun isShouldShowRequestPermissionRationale(activity: Activity, perm: String): Boolean {
        return when (GetAppInfo.getInitLocPermission(activity)) {
            "" -> true
            "Second" -> true
            else -> {
                shouldShowRequestPermissionRationale(activity, perm)
                false
            }
        }
    }

    /** 백그라운드에서 위치 접근 권한 허용 여부 검사 **/
    fun isBackgroundRequestLocation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, permissionsLocationBackground) ==
                    PackageManager.PERMISSION_GRANTED
        } else { true }
    }

    /** 백그라운드에서 위치 접근 권한 요청 **/
    fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                context as Activity,
                arrayOf(permissionsLocationBackground),
                StaticDataObject.REQUEST_BACKGROUND_LOCATION
            )
        }
    }
}