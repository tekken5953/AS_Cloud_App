package com.example.airsignal_app.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityPermissionBinding
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.util.`object`.GetAppInfo.getInitLocPermission
import com.example.airsignal_app.util.`object`.GetAppInfo.getInitNotiPermission
import com.example.airsignal_app.util.`object`.GetSystemInfo
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
                    Toast.makeText(this, "알림은 앱 설정 -> 알림 항목에서 언제든 허용하실 수 있습니다.",
                        Toast.LENGTH_SHORT).show()
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

        RDBLogcat.writeUserPref(this, sort = RDBLogcat.USER_PREF_SETUP,
            title = RDBLogcat.USER_PREF_SETUP_INIT,
        value = "${DataTypeParser.parseLongToLocalDateTime(DataTypeParser.getCurrentTime())}")

        RDBLogcat.writeUserPref(this, sort = RDBLogcat.USER_PREF_SETUP,
            title = RDBLogcat.USER_PREF_SETUP_LAST_LOGIN,
            value = "${DataTypeParser.parseLongToLocalDateTime(DataTypeParser.getCurrentTime())}")

        RDBLogcat.writeUserPref(this, sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
            value = GetSystemInfo.getApplicationVersion(this))

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
                    val builder = AlertDialog.Builder(this)
                    val alertDialog = builder.create()
                    alertDialog.apply {
                        setButton(
                            AlertDialog.BUTTON_NEGATIVE,"확인"
                        ) { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        setTitle("위치 권한 거부됨")
                        setMessage("권한 -> 위치 -> 허용을 체크해주세요")
                        show()
                    }
                }
            }
        }
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "EnterPageUtil(this).fullyExit()",
            "com.example.airsignal_app.util.EnterPageUtil"
        )
    )
    override fun onBackPressed() {
        EnterPageUtil(this).fullyExit()
    }
}