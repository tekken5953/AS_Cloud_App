package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import app.airsignal.core_network.ErrorCode.ERROR_NETWORK
import app.airsignal.core_network.ErrorCode.ERROR_SERVER_CONNECTING
import app.airsignal.core_network.retrofit.ApiModel
import app.airsignal.core_repository.BaseRepository
import app.airsignal.core_viewmodel.GetAppVersionViewModel
import app.airsignal.weather.R
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.databinding.ActivitySplashBinding
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.util.`object`.DataTypeParser.setStatusBar
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import app.core_customview.MakeSingleDialog
import app.core_databse.db.sp.GetAppInfo.getUserLoginPlatform
import app.core_databse.db.sp.GetSystemInfo
import app.core_databse.db.sp.GetSystemInfo.goToPlayStore
import app.location.GetLocation
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override val resID: Int get() = R.layout.activity_splash

    private val appVersionViewModel by viewModel<GetAppVersionViewModel>()

    override fun onResume() {
        super.onResume()
        run {
            applyAppVersionData()
            appVersionViewModel.loadDataResult()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar(this)

        initBinding()

        // 유저 디바이스 설정 - 앱 버전
        RDBLogcat.writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
            value = "name is ${GetSystemInfo.getApplicationVersionName(this)} code is ${GetSystemInfo.getApplicationVersionCode(this)}"
        )

        // 유저 디바이스 설정 - SDK 버전
        RDBLogcat.writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_SDK_VERSION,
            value = Build.VERSION.SDK_INT
        )

        // 초기설정 로그 저장 - 마지막 접속 시간
        RDBLogcat.writeUserPref(
            this, sort = RDBLogcat.USER_PREF_SETUP,
            title = RDBLogcat.USER_PREF_SETUP_LAST_LOGIN,
            value = "${DataTypeParser.parseLongToLocalDateTime(DataTypeParser.getCurrentTime())}"
        )
    }

    // 권한이 허용되었으면 메인 페이지로 바로 이동, 아니면 권한 요청 페이지로 이동
    private fun enterPage(inAppMsgList: Array<ApiModel.InAppMsgItem>?) {
        if (RequestPermissionsUtil(this@SplashActivity).isLocationPermitted())
            EnterPageUtil(this@SplashActivity).toMain(getUserLoginPlatform(this), inAppMsgList)
        else EnterPageUtil(this@SplashActivity).toPermission()
    }

    // 앱 버전 뷰모델 데이터 호출
    private fun applyAppVersionData() {
        try {
            if (!appVersionViewModel.fetchData().hasObservers()) {
                appVersionViewModel.fetchData().observe(this) { result ->
                    result?.let { ver ->
                        when (ver) {
                            // 통신 성공
                            is BaseRepository.ApiState.Success -> {
                                binding.splashPB.visibility = View.GONE
                                val inAppArray = ver.data.inAppMsg
                                val versionName = GetSystemInfo.getApplicationVersionName(this)
                                val versionCode = GetSystemInfo.getApplicationVersionCode(this)
                                if ( "${versionName}.${versionCode}" == "${ver.data.serviceName}.${ver.data.serviceCode}" ) {
                                    enterPage(inAppArray)
                                } else {
                                    ver.data.test.forEach {
                                        if ("${versionName}.${versionCode}" == "${it.name}.${it.code}") {
                                            enterPage(inAppArray)
                                        } else {
                                            MakeSingleDialog(this)
                                                .makeDialog(getString(R.string.not_latest_go_to_store),
                                                    app.common_res.R.color.main_blue_color,getString(R.string.download), true)
                                                .setOnClickListener { goToPlayStore(this@SplashActivity) }
                                        }
                                    }
                                }
                            }

                            // 통신 실패
                            is BaseRepository.ApiState.Error -> {
                                binding.splashPB.visibility = View.GONE
                                when (ver.errorMessage) {
                                    ERROR_NETWORK -> {
                                        if (GetLocation(this).isNetWorkConnected())
                                            makeDialog(getString(R.string.unknown_error))
                                        else makeDialog(getString(R.string.error_network_connect))
                                    }
                                    ERROR_SERVER_CONNECTING -> makeDialog(getString(R.string.error_server_down))
                                    else -> makeDialog(getString(R.string.unknown_error))
                                }
                            }
                            // 통신 중
                            is BaseRepository.ApiState.Loading -> binding.splashPB.visibility = View.VISIBLE
                        }
                    }
                }
            }
        } catch(e: Exception) {
            binding.splashPB.visibility = View.GONE
            makeDialog("앱 버전을 불러올 수 없습니다.\n나중에 다시 시도해주세요.")
        }
    }

    // 다이얼로그 생성
    private fun makeDialog(s: String) {
        MakeSingleDialog(this).makeDialog(
            s, R.color.theme_alert_double_apply_color, getString(R.string.ok), false
        )
    }
}