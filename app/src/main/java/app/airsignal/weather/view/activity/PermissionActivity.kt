package app.airsignal.weather.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.Toast
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.databinding.ActivityPermissionBinding
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.RequestPermissionsUtil
import app.airsignal.weather.util.`object`.*
import app.airsignal.weather.util.`object`.GetAppInfo.getInitLocPermission
import app.airsignal.weather.util.`object`.GetAppInfo.getInitNotiPermission
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

        SetSystemInfo.setStatusBar(this)

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
            value = "${GetSystemInfo.getApplicationVersionName(this)}.${GetSystemInfo.getApplicationVersionCode(this)}"
        )

        val userDataIndex = binding.permissionUserDataNotice.text.toString().indexOf(getString(R.string.data_usages).lowercase())

        val spanUserData = SpannableStringBuilder(binding.permissionUserDataNotice.text.toString())
        spanUserData.setSpan(URLSpan("https://docs.google.com/document/d/e/2PACX-1vQd0Dxx1oiWNUVspQcuiqp_q9OvCMf5Fx0vp7dhwpNSz312Yx0W8ltyjyqHx7VwwBXaWq_NZmBNf1b7/pub"),
                userDataIndex,
                userDataIndex + getString(R.string.data_usages).length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        binding.permissionUserDataNotice.text = spanUserData

        binding.permissionUserDataNotice.linksClickable = true
        binding.permissionUserDataNotice.movementMethod = LinkMovementMethod.getInstance()

        binding.permissionUserDataCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.permissionOkBtn.isEnabled = isChecked
        }

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
                        textTitle = getString(R.string.perm_loc_self),
                        color = getColor(R.color.main_blue_color),
                        buttonText = getString(R.string.ok),
                        true
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