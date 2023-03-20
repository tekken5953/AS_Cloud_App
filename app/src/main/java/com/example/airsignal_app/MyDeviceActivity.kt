package com.example.airsignal_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.util.RefreshUtils
import org.jetbrains.annotations.Nullable

class MyDeviceActivity : AppCompatActivity() {
    private lateinit var builder: AlertDialog.Builder
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_device)
        builder = AlertDialog.Builder(this, R.style.AlertDialog)

        val viewAddDevice: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_add_device,null)
        val viewInputSerial: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_input_serial, null)
        val viewLoading: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_progress_add_device, null)
        viewLoading.isEnabled = false
        val viewComplete: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_comp_add_device, null)

        val back: ImageView = findViewById(R.id.myDeviceBack)
        val addBtn: ImageView = findViewById(R.id.myDeviceAddDevice)

        back.setOnClickListener {
            onBackPressed()
        }

        addBtn.setOnClickListener {
            showDialog(viewAddDevice,true)
            val addDeviceFrame: FrameLayout = viewAddDevice.findViewById(R.id.addDeviceFrame)
            val backBtn: ImageView = viewAddDevice.findViewById(R.id.addDeviceCancel)
            backBtn.setOnClickListener {
                RefreshUtils().refreshActivity(this)
            }
            addDeviceFrame.setOnClickListener {
                showDialog(viewInputSerial, true)
                val nextBtn: AppCompatButton = viewInputSerial.findViewById(R.id.inputSerialNextBtn)
                val cancel: ImageView = viewInputSerial.findViewById(R.id.inputSerialCancel)

                cancel.setOnClickListener {
                    RefreshUtils().refreshActivity(this)
                }

                nextBtn.setOnClickListener {
                    showDialog(viewLoading, false)
                    Glide.with(it.context).asGif().load(R.drawable.loading_gif)
                        .into(viewLoading.findViewById(R.id.progressAddFrameImage))
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        showDialog(viewComplete, false)
                        val viewCompleteOkBtn: AppCompatButton =
                            viewComplete.findViewById(R.id.compAddOkBtn)
                        viewCompleteOkBtn.setOnClickListener {
                            RefreshUtils().refreshActivity(this)
                        }
                    }, 2000)
                }
            }
        }
    }

    private fun showDialog(v: View, cancelable: Boolean?) {
        if (cancelable == true) {
            v.let {
                if (v.parent == null)
                    builder.setView(v).show().setCancelable(true)
                else {
                    (v.parent as ViewGroup).removeView(v)
                    builder.setView(v).show().setCancelable(true)
                }
            }
        } else {
            v.let {
                if (v.parent == null)
                    builder.setView(v).show().setCancelable(false)
                else {
                    (v.parent as ViewGroup).removeView(v)
                    builder.setView(v).show().setCancelable(false)
                }
            }
        }
    }
}