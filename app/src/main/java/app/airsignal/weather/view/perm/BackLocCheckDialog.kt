package app.airsignal.weather.view.perm

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.GetSystemInfo
import app.airsignal.weather.db.sp.SetAppInfo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오전 11:53
 **/
class BackLocCheckDialog(
    mActivity: Activity,
    private val fm: FragmentManager, private val tagId: String?
) : BottomSheetDialogFragment() {
    private val activity = mActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_background_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apply = view.findViewById<AppCompatButton>(R.id.backPermApplyBtn)
        val cancel = view.findViewById<AppCompatButton>(R.id.backPermCancelBtn)
        val subTitle = view.findViewById<TextView>(R.id.backPermSubTitle)
        val title = view.findViewById<TextView>(R.id.backPermTitle)

        val subText = subTitle.text.toString()
        if (subText.contains(getString(R.string.nav_back_perm))) {
            val startIndex = subText.indexOf(getString(R.string.nav_back_perm))
            val span = SpannableStringBuilder(subText)
            span.setSpan(
                ForegroundColorSpan(activity.getColor(R.color.main_blue_color)),
                startIndex,
                startIndex + getString(R.string.nav_back_perm).length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            subTitle.text = span
        }

        if (Build.VERSION.SDK_INT >= 29) {
            title.text = getString(R.string.back_perm_up_title)
            subTitle.visibility = View.VISIBLE
            apply.text = getString(R.string.always_allowed)

            apply.setOnClickListener {
                if (RequestPermissionsUtil(activity)
                        .isShouldShowRequestPermissionRationale(
                            activity,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                ) {
                    when (GetAppInfo.getInitLocPermission()) {
                        "" -> SetAppInfo.setInitLocPermission("Second")
                        "Second" -> SetAppInfo.setInitLocPermission("Done")
                    }

                    RequestPermissionsUtil(activity).requestBackgroundLocation()
                    activity.recreate()
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", activity.packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }
                dismiss()
            }
        } else {
            title.text = getString(R.string.back_perm_down_title)
            subTitle.visibility = View.GONE
            apply.apply {
                val isPermedBackLoc = GetAppInfo.isPermedBackLoc()
                this.text = if (isPermedBackLoc) getString(R.string.undo_active) else getString(R.string.do_active)
                this.backgroundTintList =
                    ColorStateList.valueOf(
                        activity.getColor(
                            if (isPermedBackLoc) R.color.theme_alert_double_apply_color
                            else R.color.main_blue_color
                        )
                    )
                this.setOnClickListener {
                    SetAppInfo.setPermedBackLog(!isPermedBackLoc)
                    dismiss()
                    activity.recreate()
                }
            }
        }
        cancel.setOnClickListener { dismiss() }
    }

    // 다이얼로그 생성
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState).run {
            this.setCanceledOnTouchOutside(false)
            this.setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                bottomSheetDialog.behavior.isDraggable = false
                GetSystemInfo.setupRatio(activity,bottomSheetDialog, 65)
            }
            this.window?.attributes?.windowAnimations = R.style.DialogAnimationBottom

            return this
        }
    }

    // 레이아웃 노출
    fun show() = BackLocCheckDialog(activity, fm, tagId).showNow(fm, tagId)

}