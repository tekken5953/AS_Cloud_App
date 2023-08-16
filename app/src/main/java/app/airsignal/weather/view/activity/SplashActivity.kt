package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import app.airsignal.weather.R
import app.airsignal.weather.dao.ErrorCode.ERROR_NETWORK
import app.airsignal.weather.dao.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.weather.databinding.ActivitySplashBinding
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.repo.BaseRepository
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.util.RequestPermissionsUtil
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLoginPlatform
import app.airsignal.weather.util.`object`.GetSystemInfo
import app.airsignal.weather.util.`object`.GetSystemInfo.goToPlayStore
import app.airsignal.weather.util.`object`.SetAppInfo.fullScreenMode
import app.airsignal.weather.view.MakeSingleDialog
import app.airsignal.weather.vmodel.GetAppVersionViewModel
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

        fullScreenMode(this)

        initBinding()

        FirebaseDatabase.getInstance()
        LoggerUtil().getInstance()

        applyAppVersionData()

        // 유저 디바이스 설정 - 앱 버전
        RDBLogcat.writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
            value = GetSystemInfo.getApplicationVersion(this)
        )

        // 유저 디바이스 설정 - 디바이스 모델
        RDBLogcat.writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_DEVICE_MODEL,
            value = Build.MODEL
        )

        // 유저 디바이스 설정 - SDK 버전
        RDBLogcat.writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_SDK_VERSION,
            value = Build.VERSION.SDK_INT
        )
    }

    // 권한이 허용되었으면 메인 페이지로 바로 이동, 아니면 권한 요청 페이지로 이동
    private fun enterPage() {
        if (RequestPermissionsUtil(this@SplashActivity).isLocationPermitted()) {
            EnterPageUtil(this@SplashActivity).toMain(
                    getUserLoginPlatform(this)
                )
        } else {
            EnterPageUtil(this@SplashActivity).toPermission()
        }
    }

    // 앱 버전 뷰모델 데이터 호출
    private fun applyAppVersionData() {
        if (!appVersionViewModel.fetchData().hasObservers()) {
            appVersionViewModel.fetchData().observe(this) { result ->
                result?.let { ver ->
                    when (ver) {
                        // 통신 성공
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
                        // 통신 실패
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

                        // 통신 중
                        is BaseRepository.ApiState.Loading -> {
                            binding.splashPB.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    // 다이얼로그 생성
    private fun makeDialog(s: String) {
        MakeSingleDialog(this).makeDialog(
            s, getColor(R.color.theme_alert_double_apply_color), getString(R.string.ok)
        )
    }
}