package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.airsignal_app.R
import com.example.airsignal_app.util.RefreshUtils

class MyDeviceActivity : AppCompatActivity() {

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_device)
        val refreshUtils = RefreshUtils(this)

        /** 장치 추가 다이얼로그 **/
        val viewAddDevice: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_add_device,null)
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

        val back: ImageView = findViewById(R.id.myDeviceBack)
        val addBtn: ImageView = findViewById(R.id.myDeviceAddDevice)

        back.setOnClickListener { onBackPressed() }

        addBtn.setOnClickListener {
            refreshUtils.showDialog(viewAddDevice, true)  // 장치추가 레이아웃 출력
            val addDeviceFrame: FrameLayout = viewAddDevice.findViewById(R.id.addDeviceFrame)   // 장치추가 클릭 필드
            val cancelAddDevice: ImageView = viewAddDevice.findViewById(R.id.addDeviceCancel) // 등록취소
            cancelAddDevice.setOnClickListener {
                // 액티비티 갱신
                refreshUtils.refreshActivity(this)
            }
            addDeviceFrame.setOnClickListener {
                refreshUtils.showDialog(viewInputSerial,true)   // 시리얼입력 레이아웃 출력
                val nextBtn: AppCompatButton = viewInputSerial.findViewById(R.id.inputSerialNextBtn)    // 다음으로 이동
                val cancelSerial: ImageView = viewInputSerial.findViewById(R.id.inputSerialCancel)    // 등록취소

                cancelSerial.setOnClickListener {
                    // 액티비티 갱신
                    refreshUtils.refreshActivity(this)
                }

                nextBtn.setOnClickListener {
                    //TODO 시리얼 번호 유효성 검사 작성 필요
                    refreshUtils.showDialog(viewLoading, false) // 로딩 레이아웃 출력
                    // 로딩 GIF
                    Glide.with(it.context).asGif().load(R.drawable.loading_gif)
                        .into(viewLoading.findViewById(R.id.progressAddFrameImage))
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        // 2초뒤에 완료 레이아웃 출력
                        refreshUtils.showDialog(viewComplete, false)
                        val viewCompleteOkBtn: AppCompatButton =    // 완료 버튼
                            viewComplete.findViewById(R.id.compAddOkBtn)
                        viewCompleteOkBtn.setOnClickListener {
                            // 액티비티 갱신
                            refreshUtils.refreshActivity(this)
                        }
                    }, 2000)
                }
            }
        }
    }
}