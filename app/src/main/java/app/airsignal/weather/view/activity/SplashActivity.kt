package app.airsignal.weather.view.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.os.HandlerCompat
import app.airsignal.weather.R
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.databinding.ActivitySplashBinding
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.SharedPreferenceManager
import app.airsignal.weather.db.sp.GetSystemInfo
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.location.GetLocation
import app.airsignal.weather.network.ErrorCode
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.MakeSingleDialog
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import app.airsignal.weather.viewmodel.GetAppVersionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override val resID: Int get() = R.layout.activity_splash

    private val appVersionViewModel by viewModel<GetAppVersionViewModel>()
    private val fetch by lazy {appVersionViewModel.fetchData()}

    private var isReady = false

    init {
        TimberUtil().getInstance()
        LoggerUtil().getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(R.drawable.splash_lottie_bg)

        initBinding().run {
            if (fetch.hasObservers()) fetch.removeObservers(this@SplashActivity)

            applyAppVersionData()

            binding.splashPB.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isReady = true
                    appVersionViewModel.loadDataResult()
                }
            })

            // 유저 디바이스 설정 - 앱 버전
            RDBLogcat.writeUserPref(
                this@SplashActivity,
                sort = RDBLogcat.USER_PREF_DEVICE,
                title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
                value = "name is ${GetSystemInfo.getApplicationVersionName(this@SplashActivity)} code is ${
                    GetSystemInfo.getApplicationVersionCode(this@SplashActivity)}"
            )

            // 유저 디바이스 설정 - SDK 버전
            RDBLogcat.writeUserPref(
                this@SplashActivity,
                sort = RDBLogcat.USER_PREF_DEVICE,
                title = RDBLogcat.USER_PREF_DEVICE_SDK_VERSION,
                value = Build.VERSION.SDK_INT
            )

            // 초기설정 로그 저장 - 마지막 접속 시간
            RDBLogcat.writeUserPref(
                this@SplashActivity, sort = RDBLogcat.USER_PREF_SETUP,
                title = RDBLogcat.USER_PREF_SETUP_LAST_LOGIN,
                value = "${DataTypeParser.parseLongToLocalDateTime(DataTypeParser.getCurrentTime())}"
            )
        }
    }

    // 권한이 허용되었으면 메인 페이지로 바로 이동, 아니면 권한 요청 페이지로 이동
    private fun enterPage(inAppMsgList: List<ApiModel.InAppMsgItem?>?) {
        if (isReady) {
            HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                if (RequestPermissionsUtil(this@SplashActivity).isLocationPermitted()) {
                    EnterPageUtil(this@SplashActivity).toMain(
                        GetAppInfo.getUserLoginPlatform(this),
                        inAppMsgList?.toTypedArray())
                } else { EnterPageUtil(this@SplashActivity).toPermission() }
            }, 500)
        } else {
            HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                enterPage(inAppMsgList)
            }, 500)
        }
    }

    // 앱 버전 뷰모델 데이터 호출
    private fun applyAppVersionData() {
        try {
            fetch.observe(this) { result ->
                result?.let { ver ->
                    when (ver) {
                        // 통신 성공
                        is BaseRepository.ApiState.Success -> {
                            val sp = SharedPreferenceManager(this@SplashActivity)
                            val inAppArray = ver.data.inAppMsg
                            val versionName = GetSystemInfo.getApplicationVersionName(this)
                            val versionCode = GetSystemInfo.getApplicationVersionCode(this)
                            val fullVersion = "${versionName}.${versionCode}"
                            val skipThisPatchKey = SpDao.PATCH_SKIP + "${ver.data.serviceName}.${ver.data.serviceCode}"

                            // 현재 버전이 최신 버전인 경우
                            if (fullVersion == "${ver.data.serviceName}.${ver.data.serviceCode}") {
                                enterPage(inAppArray)   // 메인 페이지로 이동
                            } else { // 현재 버전이 최신 버전이 아닌 경우
                                val array = ArrayList<String>()
                                ver.data.test.forEach { array.add("${it.name}.${it.code}") }

                                // 테스트 버전에 현재 버전이 포함되는 경우
                                if (array.contains(fullVersion)) {
                                    // 스킵이 설정되어 있지 않은 경우
                                    if (!sp.getBoolean(skipThisPatchKey, false)) {
                                        // 최신 버전 설치와 현재 버전 사용 선택 다이얼로그 노출
                                        val dialog = MakeDoubleDialog(this)
                                            .make(
                                                getString(R.string.exist_last_version),
                                                getString(R.string.download), getString(R.string.use_current_version), R.color.main_blue_color
                                            )
                                        // 설치 선택 시 스토어 이동
                                        dialog.first.setOnClickListener {
                                            sp.setBoolean(skipThisPatchKey, false)
                                            GetSystemInfo.goToPlayStore(this@SplashActivity)
                                        }
                                        // 현재 버전 이용 선택 시 메인 이동
                                        dialog.second.setOnClickListener {
                                            sp.setBoolean(skipThisPatchKey, true)
                                            ToastUtils(this@SplashActivity).showMessage(getString(R.string.patch_store_notice))
                                            enterPage(inAppArray)
                                        }
                                    } else enterPage(inAppArray)  // 스킵이 설정되어 있는 경우 메인 이동
                                } else {
                                    // 모든 허용 버전에 해당되지 않은 경우
                                    MakeSingleDialog(this)
                                        .makeDialog(
                                            getString(R.string.not_latest_go_to_store),
                                            R.color.main_blue_color,
                                            getString(R.string.download),
                                            true
                                        ).setOnClickListener {
                                            sp.setBoolean(SpDao.PATCH_SKIP, false)
                                            GetSystemInfo.goToPlayStore(this@SplashActivity)
                                        }
                                }
                            }
                        }

                        // 통신 실패
                        is BaseRepository.ApiState.Error -> {
                            when (ver.errorMessage) {
                                ErrorCode.ERROR_NETWORK -> {
                                    if (GetLocation(this).isNetWorkConnected()) {
                                        makeDialog(getString(R.string.unknown_error))
                                    } else { makeDialog(getString(R.string.error_network_connect)) }
                                }

                                ErrorCode.ERROR_SERVER_CONNECTING -> { makeDialog(getString(R.string.error_server_down)) }

                                else -> makeDialog(getString(R.string.unknown_error))
                            }
                        }
                        // 통신 중
                        is BaseRepository.ApiState.Loading -> {}
                    }
                }
            }
        } catch (e: IOException) { makeDialog(getString(R.string.fail_to_get_app_version)) }
    }

    // 다이얼로그 생성
    private fun makeDialog(s: String) {
        MakeSingleDialog(this).makeDialog(
            s, R.color.theme_alert_double_apply_color, getString(R.string.ok), false
        )
    }
}