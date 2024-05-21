package app.airsignal.weather.view.perm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.airsignal.weather.db.sp.GetSystemInfo
import app.airsignal.weather.db.sp.SetAppInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*


/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오전 11:53
 **/
class LocPermCautionDialog(
    mActivity: Activity,
    private val fm: FragmentManager, private val tagId: String?,
) : BottomSheetDialogFragment() {
    private val activity = mActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_loc_perm_caution, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val okBtn = view.findViewById<AppCompatButton>(R.id.permCautionBtn)
        val locIcon = view.findViewById<ImageView>(R.id.permCautionImg)
        val locShadow = view.findViewById<ImageView>(R.id.permCautionImgShadow)

        // 권한 재요청 아이콘 애니메이션 적용
        locIcon.animation =
            AnimationUtils.loadAnimation(context,R.anim.loc_perm_caution_icon_anim).apply { start() }

        // 권한 재요청 그림자 애니메이션 적용
        locShadow.animation =
            AnimationUtils.loadAnimation(context,R.anim.loc_perm_caution_shadow_anim).apply { start() }

        // 확인 버튼 클릭
        okBtn.setOnClickListener {
            dismiss()
            SetAppInfo.setInitLocPermission(activity, "Done")
            RequestPermissionsUtil(activity).requestLocation()
        }
    }

    // 다이얼로그 생성
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState).apply {
            this.setCanceledOnTouchOutside(false)
            this.setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                bottomSheetDialog.behavior.isDraggable = false
                setupRatio(bottomSheetDialog, 60)
            }
            this.window?.attributes?.windowAnimations = R.style.DialogAnimationBottom

            return this
        }
    }

    // 레이아웃 노출
    fun show() { LocPermCautionDialog(activity, fm, tagId).showNow(fm, tagId) }

    // 바텀 다이얼로그 세팅
    private fun setupRatio(bottomSheetDialog: BottomSheetDialog, ratio: Int) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight(ratio)
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheet.background = ResourcesCompat.getDrawable(resources, R.drawable.loc_perm_bg,null)
    }

    // 바텀 다이얼로그 비율설정
    private fun getBottomSheetDialogDefaultHeight(per: Int): Int {
        return GetSystemInfo.getWindowHeight(activity) * per / 100
    }
}