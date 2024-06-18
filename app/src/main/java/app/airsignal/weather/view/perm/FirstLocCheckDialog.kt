package app.airsignal.weather.view.perm

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.GetSystemInfo
import app.airsignal.weather.db.sp.SetAppInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오전 11:53
 **/
class  FirstLocCheckDialog(
    mActivity: Activity,
    private val fm: FragmentManager, private val tagId: String?
) : BottomSheetDialogFragment() {
    private val activity = mActivity
    private val perm = RequestPermissionsUtil(activity)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_first_perm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apply = view.findViewById<AppCompatButton>(R.id.firstPermApplyBtn)
        val cancel = view.findViewById<AppCompatButton>(R.id.firstPermCancelBtn)

        apply.setOnClickListener {
            if (!perm.isLocationPermitted()) {  // 위치 권한 허용?
                if (perm.isShouldShowRequestPermissionRationale(
                        activity, android.Manifest.permission.ACCESS_FINE_LOCATION)   // 권한 거부가 2번 이하?
                ) {
                    when (GetAppInfo.getInitLocPermission(activity)) { // 위치 권한 요청이 처음?
                        "" -> {
                            SetAppInfo.setInitLocPermission(activity, "Second")
                            perm.requestLocation()
                        }
                        "Second" -> LocPermCautionDialog(activity, fm, BottomSheetDialogFragment().tag).show()
                    }
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }

            dismiss()
        }
        cancel.setOnClickListener { dismiss() }
    }

    // 다이얼로그 생성
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheetDialog.behavior.isDraggable = false
            setupRatio(bottomSheetDialog, 75)
        }
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationBottom

        return dialog
    }

    // 레이아웃 노출
    fun show() = FirstLocCheckDialog(activity, fm, tagId).showNow(fm, tagId)

    // 바텀 다이얼로그 세팅
    private fun setupRatio(bottomSheetDialog: BottomSheetDialog, ratio: Int) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = GetSystemInfo.getBottomSheetDialogDefaultHeight(activity,ratio)
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheet.background =
            ResourcesCompat.getDrawable(resources, R.drawable.loc_perm_bg, null)
    }
}