package app.airsignal.weather.view.perm

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.airsignal.weather.db.sp.GetSystemInfo
import app.airsignal.weather.db.sp.SetAppInfo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오전 11:53
 **/
class LocPermCautionDialog(
    mActivity: Activity,
    private val fm: FragmentManager, private val tagId: String?
    ) : BottomSheetDialogFragment() {
    private val activity = mActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
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
            SetAppInfo.setInitLocPermission("Done")
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
                GetSystemInfo.setupRatio(activity,bottomSheetDialog, 60)
            }
            this.window?.attributes?.windowAnimations = R.style.DialogAnimationBottom

            return this
        }
    }

    // 레이아웃 노출
    fun show() { LocPermCautionDialog(activity, fm, tagId).showNow(fm, tagId) }
}