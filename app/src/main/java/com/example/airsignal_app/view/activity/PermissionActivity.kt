package com.example.airsignal_app.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityPermissionBinding
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.GetAppInfo
import timber.log.Timber
import kotlin.system.exitProcess

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>() {
    override val resID: Int get() = R.layout.activity_permission
    private val perm = RequestPermissionsUtil(this)

    override fun onResume() {
        super.onResume()

        if (perm.isLocationPermitted()) {
            if (!perm.isNotificationPermitted()) {
                if (!perm.isNotiDenied()) {
                    perm.requestNotification()
                } else {
                    EnterPageUtil(this).toMain(GetAppInfo.getUserLoginPlatform(this))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        initBinding()

        binding.permissionOkBtn.setOnClickListener {
            if (!perm.isLocationPermitted()) {
                when (perm.isShouldShowRequestPermissionRationale(this)) {
                    "denied once" -> {
                        Timber.tag("TAG_P").d( "denied once")
                    }
                    "denied twice" -> {
                        Timber.tag("TAG_P").d( "denied twice")
                        Toast.makeText(
                            this,
                            "위치 권한이 거부되어 있습니다\n서비스 이용을 위해 허용해주세요",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    else -> {
                        Timber.tag("TAG_P").d( "denied else")
                    }
                }
                perm.requestLocation()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exitProcess(1)
    }
}