package com.example.airsignal_app.view.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityMyDeviceBinding
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.view.ShowDialogClass

class MyDeviceActivity
    : BaseActivity<ActivityMyDeviceBinding>() {
    override val resID: Int get() = R.layout.activity_my_device

    private val sp by lazy { ShowDialogClass().getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        val refreshUtils = RefreshUtils(this)

        /** 장치 추가 다이얼로그 **/
        val viewAddDevice: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_add_device, null)

        /** 시리얼 번호 입력 다이얼로그 **/
        val viewInputSerial: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_input_serial, null)

        /** 등록 처리 다이얼로그 **/
        val viewLoading: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_progress_add_device, null)
        viewLoading.isEnabled = false   // 클릭 막기

        /** 등록 완료 다이얼로그 **/
        val viewComplete: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_comp_add_device, null)


        binding.myDeviceAddDevice.setOnClickListener {
            ShowDialogClass().show(viewAddDevice, true)  // 장치추가 레이아웃 출력
            val addDeviceFrame: FrameLayout =
                viewAddDevice.findViewById(R.id.addDeviceFrame)   // 장치추가 클릭 필드
            val cancelAddDevice: ImageView =
                viewAddDevice.findViewById(R.id.addDeviceCancel) // 등록취소
            cancelAddDevice.setOnClickListener {
                // 액티비티 갱신
                refreshUtils.refreshActivity()
            }
            addDeviceFrame.setOnClickListener {
                sp.show(viewInputSerial, true)   // 시리얼입력 레이아웃 출력
                val nextBtn: AppCompatButton =
                    viewInputSerial.findViewById(R.id.inputSerialNextBtn)    // 다음으로 이동
                val cancelSerial: ImageView =
                    viewInputSerial.findViewById(R.id.inputSerialCancel)    // 등록취소
                val serialEt: EditText = viewInputSerial.findViewById(R.id.inputSerialEditText)
                val serialErrorText: TextView =
                    viewInputSerial.findViewById(R.id.inputSerialErrorText)

                cancelSerial.setOnClickListener {
                    // 액티비티 갱신
                    refreshUtils.refreshActivity()
                }

                serialEt.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        setNextButton(nextBtn, false, R.color.main_gray_color)
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (serialEt.text.length >= serialEt.maxEms) {
                            setNextButton(nextBtn, true, R.color.mode_color_view)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                nextBtn.setOnClickListener {
                    if (serialEt.length() != serialEt.maxEms) {
                        serialEt.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.serial_error, null)
                        serialErrorText.visibility = View.VISIBLE
                    } else {
                        serialEt.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.normal_box_bg, null)
                        serialErrorText.visibility = View.GONE

                        sp.show(viewLoading, false) // 로딩 레이아웃 출력
                        // 로딩 GIF
                        Glide.with(it.context).asGif().load(R.drawable.loading_gif)
                            .into(viewLoading.findViewById(R.id.progressAddFrameImage))
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            // 2초뒤에 완료 레이아웃 출력
                            sp.setBackPressed(findViewById(R.id.myDeviceBack))
                                .show(viewComplete, false)
                            val viewCompleteOkBtn: AppCompatButton =    // 완료 버튼
                                viewComplete.findViewById(R.id.compAddOkBtn)
                            viewCompleteOkBtn.setOnClickListener {
                                // 액티비티 갱신
                                refreshUtils.refreshActivity()
                            }
                        }, 2000)
                    }
                }
            }
        }
    }

    private fun setNextButton(button: AppCompatButton, isEnable: Boolean, color: Int) {
        button.apply {
            isEnabled = isEnable
            setTextColor(ResourcesCompat.getColor(resources, color, null))
        }
    }
}