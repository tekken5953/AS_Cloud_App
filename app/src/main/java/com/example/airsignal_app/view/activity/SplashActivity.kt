package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.ErrorCode.ERROR_NETWORK
import com.example.airsignal_app.dao.ErrorCode.ERROR_SERVER_CONNECTING
import com.example.airsignal_app.databinding.ActivitySplashBinding
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.repo.BaseRepository
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.LoggerUtil
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.util.`object`.GetSystemInfo.goToPlayStore
import com.example.airsignal_app.view.MakeSingleDialog
import com.example.airsignal_app.vmodel.GetAppVersionViewModel
import com.google.firebase.database.FirebaseDatabase
import org.koin.androidx.viewmodel.ext.android.viewModel


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override val resID: Int get() = R.layout.activity_splash

    private val appVersionViewModel by viewModel<GetAppVersionViewModel>()

    override fun onResume() {
        super.onResume()

        appVersionViewModel.loadDataResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        fullScreenMode()

        initBinding()

        FirebaseDatabase.getInstance()
        LoggerUtil().getInstance()

        applyAppVersionData()

        RDBLogcat.writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
            value = GetSystemInfo.getApplicationVersion(this)
        )

        RDBLogcat.writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_DEVICE_MODEL,
            value = Build.MODEL
        )

        RDBLogcat.writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_SDK_VERSION,
            value = Build.VERSION.SDK_INT
        )
    }

    private fun enterPage() {
        if (RequestPermissionsUtil(this@SplashActivity).isLocationPermitted()) {
            EnterPageUtil(this@SplashActivity).toMain(
                    getUserLoginPlatform(this)
                )
        } else {
            EnterPageUtil(this@SplashActivity).toPermission()
        }
    }

    private fun applyAppVersionData() {
        if (!appVersionViewModel.fetchData().hasObservers()) {
            appVersionViewModel.fetchData().observe(this) { result ->
                result?.let { ver ->
                    when (ver) {
                        is BaseRepository.ApiState.Success -> {
                            binding.splashPB.visibility = View.GONE
                            val versionInfo = GetSystemInfo.getApplicationVersion(this)
                            if (ver.data.version == versionInfo) {
                                enterPage()
                            } else {
                                MakeSingleDialog(this)
                                    .makeDialog(getString(R.string.not_latest_version),
                                R.color.main_blue_color,getString(R.string.download))
                                    .setOnClickListener {
                                        goToPlayStore(this@SplashActivity)
                                    }
                            }
                        }
                        is BaseRepository.ApiState.Error -> {
                            binding.splashPB.visibility = View.GONE
                            when (ver.errorMessage) {
                                ERROR_NETWORK -> {
                                    makeDialog(getString(R.string.error_network_connect))
                                }

                                ERROR_SERVER_CONNECTING -> {
                                    makeDialog(getString(R.string.error_server_down))
                                }
                            }
                        }

                        is BaseRepository.ApiState.Loading -> {
                            binding.splashPB.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun makeDialog(s: String) {
        MakeSingleDialog(this).makeDialog(
            s, getColor(R.color.theme_alert_double_apply_color), getString(R.string.ok)
        )
    }
}