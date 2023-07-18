package com.example.airsignal_app.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityPermissionBinding
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.util.`object`.GetAppInfo.getInitLocPermission
import com.example.airsignal_app.util.`object`.GetAppInfo.getInitNotiPermission
import com.example.airsignal_app.util.`object`.SetAppInfo
import com.example.airsignal_app.view.LocPermCautionDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>() {
    override val resID: Int get() = R.layout.activity_permission
    private val perm = RequestPermissionsUtil(this)

    override fun onResume() {
        super.onResume()

        if (perm.isLocationPermitted()) {
            if (!perm.isNotificationPermitted()) {
                if (getInitNotiPermission(this) == "") {
                    SetAppInfo.setInitNotiPermission(this, "Not Init")
                    perm.requestNotification()
                } else {
                    EnterPageUtil(this).toMain(GetAppInfo.getUserLoginPlatform(this))
                }
            } else {
                EnterPageUtil(this).toMain(GetAppInfo.getUserLoginPlatform(this))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        initBinding()

        binding.permissionOkBtn.setOnClickListener {
            if (!perm.isLocationPermitted()) {
                if (perm.isShouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    when (getInitLocPermission(this)) {
                        "" -> {
                            SetAppInfo.setInitLocPermission(this, "Second")
                            perm.requestLocation()
                        }
                        "Second" -> {
                            LocPermCautionDialog(
                                this,
                                supportFragmentManager,
                                BottomSheetDialogFragment().tag
                            )
                                .show()
                        }
                    }
                } else {
                    Log.d("TAG_P", "Denied Permission")
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
            }
        }
    }

    override fun onBackPressed() {
        EnterPageUtil(this).fullyExit()
    }
}