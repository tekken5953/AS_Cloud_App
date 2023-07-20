package com.example.airsignal_app.view.widget

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.REQUEST_BACKGROUND_LOCATION
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.util.`object`.SetAppInfo
import com.orhanobut.logger.Logger

class BackgroundPermissionActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        NotiJobService().getWidgetLocation(this)
        if (!RequestPermissionsUtil(this).isBackgroundRequestLocation()) {
            if (RequestPermissionsUtil(this).isShouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                when (GetAppInfo.getInitLocPermission(this)) {
                    "" -> {
                        SetAppInfo.setInitLocPermission(this, "Second")
                        RequestPermissionsUtil(this).requestBackgroundLocation()
                    }
                    "Second" -> {
                        SetAppInfo.setInitLocPermission(this, "Done")
                        RequestPermissionsUtil(this).requestBackgroundLocation()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "위치 권한을 항상 허용으로 설정해주세요",
                    Toast.LENGTH_SHORT
                )
                    .show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        } else {
            callWidgetServiceBroadcast()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_permission)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BACKGROUND_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한이 허용되었습니다", Toast.LENGTH_SHORT).show()
                Logger.t("testtest").d("권한이 허용되었습니다")
                callWidgetServiceBroadcast()
            } else {
                Toast.makeText(this, "권한이 거부되었습니다", Toast.LENGTH_SHORT).show()
                Logger.t("testtest").d("권한이 거부되었습니다")
                callWidgetServiceBroadcast()
            }
        }
    }

    private fun callWidgetServiceBroadcast() {
        // 브로드캐스트 인텐트를 생성합니다.
//        val intent = Intent(this, WidgetProvider4x2::class.java)
//
//        // 위젯 서비스의 액션을 설정합니다.
//        intent.action = WIDGET_UPDATE
//
//        // 브로드캐스트를 전송합니다.
//        sendBroadcast(intent)

        NotiJobService().getWidgetLocation(this)
        EnterPageUtil(this).toPermission()
    }
}