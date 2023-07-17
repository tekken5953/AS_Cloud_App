package com.example.airsignal_app.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityRedirectBinding
import com.example.airsignal_app.repo.BaseRepository
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.LoggerUtil
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.vmodel.GetAppVersionViewModel
import com.google.firebase.database.FirebaseDatabase
import org.koin.androidx.viewmodel.ext.android.viewModel


class RedirectActivity
    : BaseActivity<ActivityRedirectBinding>() {
    override val resID: Int get() = R.layout.activity_redirect

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

        fullScreenMode()

        initBinding()

        FirebaseDatabase.getInstance()
        LoggerUtil().getInstance()

        applyAppVersionData()
    }

    // 몰입모드로 전환됩니다
    private fun fullScreenMode() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    private fun enterPage() {
        if (RequestPermissionsUtil(this@RedirectActivity).isLocationPermitted()) {
            EnterPageUtil(this@RedirectActivity)
                .toMain(
                    getUserLoginPlatform(this)
                )
        } else {
            EnterPageUtil(this@RedirectActivity)
                .toPermission()
        }
    }

    fun goToPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=$packageName")
        startActivity(intent)
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
                                enterPage()
                            } else {
                                //TODO 버전 업데이트로 유도
//                                goToPlayStore()
                            }
                        }
//                        is BaseRepository.ApiState.Error -> {
//                            val builder = AlertDialog.Builder(this)
//                            val alertDialog = builder.create()
//                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"확인"
//                            ) { _, _ ->
//                                exitProcess(1)
//                            }
//                        }

                        else -> {}
                    }
                }
            }
        }
    }
}