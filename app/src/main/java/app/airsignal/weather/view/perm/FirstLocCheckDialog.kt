package app.airsignal.weather.view.perm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.core_databse.db.sp.GetAppInfo
import app.core_databse.db.sp.SetAppInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*


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

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apply = view.findViewById<AppCompatButton>(R.id.firstPermApplyBtn)
        val cancel = view.findViewById<AppCompatButton>(R.id.firstPermCancelBtn)

        apply.setOnClickListener {
            if (!perm.isLocationPermitted()) {  // 위치 권한 허용?
                if (perm.isShouldShowRequestPermissionRationale(
                        activity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )   // 권한 거부가 2번 이하?
                ) {
                    when (GetAppInfo.getInitLocPermission(activity)) { // 위치 권한 요청이 처음?
                        "" -> {
                            SetAppInfo.setInitLocPermission(activity, "Second")
                            perm.requestLocation()
                        }
                        "Second" -> {
                            LocPermCautionDialog(activity, fm, BottomSheetDialogFragment().tag).show()
                        }
                    }
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
            dismissNow()
        }
        cancel.setOnClickListener {
            dismissNow()
        }
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
    fun show() { FirstLocCheckDialog(activity, fm, tagId).showNow(fm, tagId) }

    // 바텀 다이얼로그 세팅
    private fun setupRatio(bottomSheetDialog: BottomSheetDialog, ratio: Int) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight(ratio)
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheet.background =
            ResourcesCompat.getDrawable(resources, R.drawable.loc_perm_bg, null)
    }

    // 바텀 다이얼로그 비율설정
    private fun getBottomSheetDialogDefaultHeight(per: Int): Int {
        return getWindowHeight() * per / 100
    }

    // 디바이스 높이 구하기
    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
}