package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.widget.Toast
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.R
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.databinding.ActivityPermissionBinding
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.dao.RDBLogcat.writeUserPref
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.`object`.DataTypeParser.getCurrentTime
import app.airsignal.weather.util.`object`.DataTypeParser.parseLongToLocalDateTime
import app.airsignal.weather.db.sp.GetAppInfo.getInitNotiPermission
import app.airsignal.weather.db.sp.GetAppInfo.getUserLoginPlatform
import app.airsignal.weather.db.sp.GetSystemInfo.getApplicationVersionCode
import app.airsignal.weather.db.sp.GetSystemInfo.getApplicationVersionName
import app.airsignal.weather.db.sp.SetAppInfo.setInitNotiPermission
import app.airsignal.weather.db.sp.SetAppInfo.setUserNoti
import app.airsignal.weather.util.`object`.DataTypeParser.setStatusBar
import app.airsignal.weather.view.perm.FirstLocCheckDialog
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import app.airsignal.weather.db.sp.SpDao.IN_APP_MSG
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>() {
    override val resID: Int get() = R.layout.activity_permission
    private val perm = RequestPermissionsUtil(this)
    private val enter by lazy {EnterPageUtil(this)}

    override fun onResume() {
        super.onResume()
        if (perm.isLocationPermitted()) {   // 위치 서비스 이용 가능?
            @Suppress("DEPRECATION") val inAppExtraList = intent.getParcelableArrayExtra(IN_APP_MSG)?.map {it as ApiModel.InAppMsgItem}?.toTypedArray()
            if (!perm.isNotificationPermitted()) {  // 알림 서비스 이용 가능?
                val initNotiPermission = getInitNotiPermission(this)
                if (initNotiPermission == "") { // 알림 서비스 권한 호출이 처음?
                    setInitNotiPermission(this, "Not Init")
                    perm.requestNotification()  // 알림 권한 요청
                } else {
                    Toast.makeText(
                        this, getString(R.string.noti_always_can),
                        Toast.LENGTH_SHORT
                    ).show()
                    enter.toMain(getUserLoginPlatform(this),inAppExtraList)
                }
            } else {
                setUserNoti(this, IgnoredKeyFile.notiEnable, true)
                setUserNoti(this, IgnoredKeyFile.notiVibrate, true)
                enter.toMain(getUserLoginPlatform(this),inAppExtraList)
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        setStatusBar(this)

        // 초기설정 로그 저장 - 초기 설치 날짜
        writeUserPref(
            this, sort = RDBLogcat.USER_PREF_SETUP,
            title = RDBLogcat.USER_PREF_SETUP_INIT,
            value = "${parseLongToLocalDateTime(getCurrentTime())}"
        )

        // 초기설정 로그 저장 - 디바이스 SDK 버전
        writeUserPref(
            this, sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_APP_VERSION,
            value = "${getApplicationVersionName(this)}.${getApplicationVersionCode(this)}"
        )

        // 유저 디바이스 설정 - 디바이스 모델
        writeUserPref(
            this,
            sort = RDBLogcat.USER_PREF_DEVICE,
            title = RDBLogcat.USER_PREF_DEVICE_DEVICE_MODEL,
            value = Build.MODEL
        )

        binding.permissionUserDataNotice.linksClickable = true
        binding.permissionUserDataNotice.movementMethod = LinkMovementMethod.getInstance()

        val userDataIndex = binding.permissionUserDataNotice.text.toString().indexOf(getString(R.string.data_usages).lowercase())
        val spanUserData = SpannableStringBuilder(binding.permissionUserDataNotice.text.toString())

        spanUserData.setSpan(UnderlineSpan(),
                userDataIndex,
                userDataIndex + getString(R.string.data_usages).length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        spanUserData.setSpan(ForegroundColorSpan(getColor(R.color.main_blue_color)),
            userDataIndex,
            userDataIndex + getString(R.string.data_usages).length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        binding.permissionUserDataNotice.text = spanUserData

        binding.permissionUserDataNotice.setOnClickListener {
            // 개인정보 처리방침 열림
            val intent = Intent(this@PermissionActivity, WebURLActivity::class.java)
                intent.putExtra("sort","dataUsage")
                intent.putExtra("appBar",true)
                startActivity(intent)
        }

        binding.permissionUserDataCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // 개인정보 처리방침 체크 박스
            binding.permissionOkBtn.isEnabled = isChecked
        }

        // 권한 허용 버튼 클릭
        binding.permissionOkBtn.setOnClickListener {
            FirstLocCheckDialog(
                this,
                supportFragmentManager,
                BottomSheetDialogFragment().tag
            ).show()
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "EnterPageUtil(this).fullyExit()",
        "app.airsignal.weather.util.EnterPageUtil"))
    override fun onBackPressed() {
        enter.fullyExit()
    }
}