package com.example.airsignal_app

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddDeviceDialog : BottomSheetDialogFragment() {

    /** 레이아웃 바인딩 **/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.add_device_dialog, container, false)
    }

    /** 다이얼로그 이니셜라이징 **/
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState).apply {
            setCancelable(true)
            window?.attributes?.windowAnimations = R.style.DialogAnimation
        }

        return dialog
    }

    /** 뷰 바인딩 **/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cancel: ImageView = view.findViewById(R.id.addDeviceCancel)
        val addFrame: FrameLayout = view.findViewById(R.id.addDeviceFrame)

        addFrame.setOnClickListener {
            //TODO 시리얼 입력 페이지로 이동
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }
}