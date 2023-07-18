package com.example.airsignal_app.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.AddressListAdapter
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.util.KeyboardController
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.DataTypeParser.convertAddress
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserFontScale
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLastAddress
import com.example.airsignal_app.util.`object`.SetAppInfo
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserLastAddr
import com.example.airsignal_app.util.`object`.SetSystemInfo
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val okBtn = view.findViewById<AppCompatButton>(R.id.permCautionBtn)

        okBtn.setOnClickListener {
            SetAppInfo.setInitLocPermission(activity, "Done")
            RequestPermissionsUtil(activity).requestLocation()
        }
    }
    // 다이얼로그 생성
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheetDialog.behavior.isDraggable = false
            setupRatio(bottomSheetDialog, 60)
        }
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationBottom

        return dialog
    }

    // 레이아웃 노출
    fun show() {
        LocPermCautionDialog(activity, fm, tagId).showNow(fm, tagId)
    }

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
        return getWindowHeight() * per / 100
    }

    // 디바이스 높이 구하기
    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(
            displayMetrics
        )
        return displayMetrics.heightPixels
    }
}