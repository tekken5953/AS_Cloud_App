package com.example.airsignal_app.view.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityRedirectPermissionBinding
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.repo.BaseRepository
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.LoggerUtil
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.util.`object`.SetSystemInfo
import com.example.airsignal_app.view.ShowDialogClass
import com.example.airsignal_app.vmodel.GetAppVersionViewModel
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess


class RedirectPermissionActivity
    : BaseActivity<ActivityRedirectPermissionBinding>() {
    override val resID: Int get() = R.layout.activity_redirect_permission
    private val locationClass: GetLocation by inject()

    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }

    private val appVersionViewModel by viewModel<GetAppVersionViewModel>()

    override fun onResume() {
        super.onResume()

        if (RequestPermissionsUtil(this).isNetworkPermitted()) {
            appVersionViewModel.loadDataResult()
        } else {
            Toast.makeText(this,
                "인터넷 연결 상태를 확인해주세요",
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        FirebaseDatabase.getInstance()
        LoggerUtil().getInstance()

        binding.permissionPB.visibility = View.VISIBLE

        binding.permissionBtn.setOnClickListener {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (!RequestPermissionsUtil(this).isLocationPermitted()) {
                    RequestPermissionsUtil(this).requestLocation()
                }
            } else {
                locationClass.requestSystemGPSEnable()
            }
        }

        applyAppVersionData()
    }

    private fun enterMainPage() {
        if (RequestPermissionsUtil(this@RedirectPermissionActivity).isLocationPermitted()) {
            EnterPageUtil(this@RedirectPermissionActivity)
                .toMain(
                    getUserLoginPlatform(this)
                )
        }
    }

    private fun applyAppVersionData() {
        if (!appVersionViewModel.fetchData().hasObservers()) {
            appVersionViewModel.fetchData().observe(this) { result ->
                result?.let { ver ->
                    when(ver) {
                        is BaseRepository.ApiState.Success -> {
                            val versionInfo =
                                GetSystemInfo.getApplicationVersion(this)
                            if (ver.data.version == versionInfo) {
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    if (RequestPermissionsUtil(this).isLocationPermitted()) {
                                        enterMainPage()
                                    } else {
                                        binding.permissionBtn.visibility = View.VISIBLE
                                        binding.permissionPB.visibility = View.GONE
                                    }
                                } else {
                                    binding.permissionBtn.visibility = View.VISIBLE
                                    binding.permissionPB.visibility = View.GONE
                                }
                            } else {
                                //TODO 버전 업데이트로 유도
                            }
                        }
                        is BaseRepository.ApiState.Error -> {
                            val builder = AlertDialog.Builder(this)
                            val alertDialog = builder.create()
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"확인"
                            ) { _, _ ->
                                exitProcess(1)
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}