package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import app.airsignal.weather.R
import app.airsignal.weather.api.retrofit.ApiModel
import app.airsignal.weather.dao.IgnoredKeyFile
import app.airsignal.weather.databinding.ActivityPermissionBinding
import app.airsignal.weather.db.sp.*
import app.airsignal.weather.utils.controller.ScreenController
import app.airsignal.weather.utils.plain.ToastUtils
import app.airsignal.weather.utils.view.EnterPageUtil
import app.airsignal.weather.view.perm.FirstLocCheckDialog
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

<<<<<<< HEAD
class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>(), KoinComponent{
=======
class PermissionActivity : BaseActivity<ActivityPermissionBinding>(), KoinComponent{
>>>>>>> f5127faf2733fe7a95cb90d2e31e3722846e9b16
    override val resID: Int get() = R.layout.activity_permission
    private val perm = RequestPermissionsUtil(this)
    private val enter by lazy { EnterPageUtil(this) }
    private val toast: ToastUtils by inject()

    override fun onResume() {
        super.onResume()
        if (!perm.isLocationPermitted()) return // 위치 서비스 이용 가능?

        kotlin.runCatching {
            @Suppress("DEPRECATION")
            val inAppExtraList = intent.getParcelableArrayExtra(SpDao.IN_APP_MSG)
                ?.map { it as ApiModel.InAppMsgItem? }
                ?.toTypedArray()

            if (perm.isNotificationPermitted()) {
                SetAppInfo.setUserNoti(SpDao.NOTI_ENABLE, true)
                SetAppInfo.setUserNoti(SpDao.NOTI_VIBRATE, true)
                enter.toMain(GetAppInfo.getUserLoginPlatform(), inAppExtraList)
                return
            }

<<<<<<< HEAD
        if (GetAppInfo.getInitNotiPermission(this) != "") {
            toast.showMessage(getString(R.string.noti_always_can))
            enter.toMain(GetAppInfo.getUserLoginPlatform(this), inAppExtraList)
            return
        }
=======
            if (GetAppInfo.getInitNotiPermission() != "") {
                toast.showMessage(getString(R.string.noti_always_can))
                enter.toMain(GetAppInfo.getUserLoginPlatform(), inAppExtraList)
                return
            }
        }.exceptionOrNull()?.stackTraceToString()
>>>>>>> f5127faf2733fe7a95cb90d2e31e3722846e9b16

        SetAppInfo.setInitNotiPermission("Not Init")
        perm.requestNotification()  // 알림 권한 요청
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        ScreenController(this).setStatusBar()

        binding.permissionUserDataNotice.linksClickable = true
        binding.permissionUserDataNotice.movementMethod = LinkMovementMethod.getInstance()

        val userDataIndex = binding.permissionUserDataNotice.text.toString().indexOf(getString(R.string.data_usages).lowercase())
        val spanUserData = SpannableStringBuilder(binding.permissionUserDataNotice.text.toString())

        spanUserData.setSpan(UnderlineSpan(),
                userDataIndex, userDataIndex + getString(R.string.data_usages).length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        spanUserData.setSpan(ForegroundColorSpan(getColor(R.color.main_blue_color)),
            userDataIndex, userDataIndex + getString(R.string.data_usages).length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        binding.permissionUserDataNotice.text = spanUserData

        binding.permissionUserDataNotice.setOnClickListener {
            // 개인정보 처리방침 열림
            val intent = Intent(this@PermissionActivity, WebURLActivity::class.java).apply {
                putExtra("sort","dataUsage")
                putExtra("appBar",true)
            }
            startActivity(intent)
        }

        // 개인정보 처리방침 체크 박스
        binding.permissionUserDataCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.permissionOkBtn.isEnabled = isChecked
        }

        // 권한 허용 버튼 클릭
        binding.permissionOkBtn.setOnClickListener {
            FirstLocCheckDialog(this,supportFragmentManager,BottomSheetDialogFragment().tag).show()
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "EnterPageUtil(this).fullyExit()",
        "app.airsignal.weather.util.EnterPageUtil"))
    override fun onBackPressed() { enter.fullyExit() }
}