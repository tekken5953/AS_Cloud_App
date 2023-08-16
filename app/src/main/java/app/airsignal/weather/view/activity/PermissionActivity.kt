package app.airsignal.weather.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.databinding.ActivityPermissionBinding
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.RequestPermissionsUtil
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.util.`object`.GetAppInfo
import app.airsignal.weather.util.`object`.GetAppInfo.getInitLocPermission
import app.airsignal.weather.util.`object`.GetAppInfo.getInitNotiPermission
import app.airsignal.weather.util.`object`.GetSystemInfo
import app.airsignal.weather.util.`object`.SetAppInfo
import app.airsignal.weather.util.`object`.SetAppInfo.setUserNoti
import app.airsignal.weather.view.LocPermCautionDialog
import app.airsignal.weather.view.MakeSingleDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>() {
    override val resID: Int get() = R.layout.activity_permission
    private val perm = RequestPermissionsUtil(this)

    override fun onResume() {
        super.onResume()
        if (perm.isLocationPermitted()) {   // 위치 서비스 이용 가능?
            if (!perm.isNotificationPermitted()) {  // 알림 서비스 이용 가능?
                if (getInitNotiPermission(this) == "") { // 알림 서비스 권한 호출이 처음?
                    SetAppInfo.setInitNotiPermission(this, "Not Init")
                    perm.requestNotification()  // 알림 권한 요청
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

        // 초기설정 로그 저장 - 초기 설치 날짜
        RDBLogcat.writeUserPref(
            this, sort = RDBLogcat.USER_PREF_SETUP,
            title = RDBLogcat.USER_PREF_SETUP_INIT,
            value = "${DataTypeParser.parseLongToLocalDateTime(DataTypeParser.getCurrentTime())}"
        )

        // 초기설정 로그 저장 - 마지막 접속 시간
        RDBLogcat.writeUserPref(
            this, sort = RDBLogcat.USER_PREF_SETUP,
            title = RDBLogcat.USER_PREF_SETUP_LAST_LOGIN,
            value = "${DataTypeParser.parseLongToLocalDateTime(DataTypeParser.getCurrentTime())}"
        )

        // 초기설정 로그 저장 - 디바이스 SDK 버전
        RDBLogcat.writeUserPref(
            this, sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
            value = GetSystemInfo.getApplicationVersion(this)
        )

        // 권한 허용 버튼 클릭
        binding.permissionOkBtn.setOnClickListener {
            if (!perm.isLocationPermitted()) {  // 위치 권한 허용?
                if (perm.isShouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )   // 권한 거부가 2번 이하?
                ) {
                    when (getInitLocPermission(this)) { // 위치 권한 요청이 처음?
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