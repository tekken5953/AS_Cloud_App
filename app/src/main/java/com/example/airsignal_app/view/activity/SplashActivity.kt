package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.ErrorCode.ERROR_NETWORK
import com.example.airsignal_app.databinding.ActivitySplashBinding
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.repo.BaseRepository
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.LoggerUtil
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.util.`object`.GetSystemInfo.goToPlayStore
import com.example.airsignal_app.view.MakeSingleDialog
import com.example.airsignal_app.vmodel.GetAppVersionViewModel
import com.google.firebase.database.FirebaseDatabase
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess


@SuppressLint("CustomSplashScreen")
class SplashActivity
    : BaseActivity<ActivitySplashBinding>() {
    override val resID: Int get() = R.layout.activity_splash

    private val appVersionViewModel by viewModel<GetAppVersionViewModel>()

    override fun onResume() {
        super.onResume()

        appVersionViewModel.loadDataResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fullScreenMode()

        initBinding()

        FirebaseDatabase.getInstance()
        LoggerUtil().getInstance()

        applyAppVersionData()

        RDBLogcat.writeUserPref(this, sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
            value = GetSystemInfo.getApplicationVersion(this))

        RDBLogcat.writeUserPref(this, sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_DEVICE_MODEL,
            value = Build.MODEL)

        RDBLogcat.writeUserPref(this, sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_SDK_VERSION,
            value = Build.VERSION.SDK_INT)
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
        if (RequestPermissionsUtil(this@SplashActivity).isLocationPermitted()) {
            EnterPageUtil(this@SplashActivity)
                .toMain(
                    getUserLoginPlatform(this)
                )
        } else {
            EnterPageUtil(this@SplashActivity)
                .toPermission()
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
                                enterPage()
                            } else {
                                val builder = AlertDialog.Builder(this)
                                val alertDialog = builder.create()
                                alertDialog.apply {
                                    setButton(AlertDialog.BUTTON_NEGATIVE,"다운로드"
                                    ) { _, _ ->
                                        goToPlayStore(this@SplashActivity)
                                    }
                                    setMessage("새로운 버전이 있습니다.")
                                    show()
                                }
                            }
                        }
                        is BaseRepository.ApiState.Error -> {
                            if (ver.errorMessage == ERROR_NETWORK) {
                                MakeSingleDialog(this).netWorkIsNotConnectedDialog(
                                    "인터넷 연결 상태를 확인 후 재실행 해주세요",
                                    getColor(R.color.theme_alert_double_apply_color),
                                    getString(R.string.ok)
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}