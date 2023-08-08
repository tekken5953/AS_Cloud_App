package com.example.airsignal_app.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.databinding.ActivityPermissionBinding
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.DataTypeParser
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.util.`object`.GetAppInfo.getInitLocPermission
import com.example.airsignal_app.util.`object`.GetAppInfo.getInitNotiPermission
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.util.`object`.SetAppInfo
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserNoti
import com.example.airsignal_app.view.LocPermCautionDialog
import com.example.airsignal_app.view.MakeSingleDialog
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
                    Toast.makeText(
                        this, getString(R.string.noti_always_can),
                        Toast.LENGTH_SHORT
                    ).show()
                    EnterPageUtil(this).toMain(GetAppInfo.getUserLoginPlatform(this))
                }
            } else {
                setUserNoti(this, IgnoredKeyFile.notiEnable, true)
                setUserNoti(this, IgnoredKeyFile.notiVibrate, true)
                EnterPageUtil(this).toMain(GetAppInfo.getUserLoginPlatform(this))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        initBinding()

        RDBLogcat.writeUserPref(
            this, sort = RDBLogcat.USER_PREF_SETUP,
            title = RDBLogcat.USER_PREF_SETUP_INIT,
            value = "${DataTypeParser.parseLongToLocalDateTime(DataTypeParser.getCurrentTime())}"
        )

        RDBLogcat.writeUserPref(
            this, sort = RDBLogcat.USER_PREF_SETUP,
            title = RDBLogcat.USER_PREF_SETUP_LAST_LOGIN,
            value = "${DataTypeParser.parseLongToLocalDateTime(DataTypeParser.getCurrentTime())}"
        )

        RDBLogcat.writeUserPref(
            this, sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
            value = GetSystemInfo.getApplicationVersion(this)
        )

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
                    MakeSingleDialog(this).makeDialog(
                        textTitle = getString(R.string.perm_self_msg),
                        color = getColor(R.color.main_blue_color),
                        buttonText = getString(R.string.ok)
                    )
                        .setOnClickListener {
                            RefreshUtils(this).refreshActivity()
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
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