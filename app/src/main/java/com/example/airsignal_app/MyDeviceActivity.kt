package com.example.airsignal_app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.airsignal_app.util.RefreshUtils

class MyDeviceActivity : AppCompatActivity() {
    private lateinit var builder: AlertDialog.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_device)

        val addBtn = findViewById<FrameLayout>(R.id.addDeviceFrame)
        addBtn.setOnClickListener {
            builder = AlertDialog.Builder(this, R.style.AlertDialog)
            val viewInputSerial: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_input_serial, null)
            val viewLoading: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_progress_add_device, null)
            val viewComplete: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_comp_add_device, null)

            builder.setView(viewInputSerial).setCancelable(false)
            val alertDialog: AlertDialog = builder.create()

            val nextBtn: AppCompatButton = viewInputSerial.findViewById(R.id.inputSerialNextBtn)
            val cancel: ImageView = viewInputSerial.findViewById(R.id.inputSerialCancel)

            cancel.setOnClickListener {
                alertDialog.dismiss()
            }

            nextBtn.setOnClickListener {
                builder.setView(viewLoading).show().setCancelable(false)
                Glide.with(it.context).asGif().load(R.drawable.loading_gif)
                    .into(viewLoading.findViewById(R.id.progressAddFrameImage))
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    builder.setView(viewComplete).show().setCancelable(false)
                    val viewCompleteOkBtn: AppCompatButton =
                        viewComplete.findViewById(R.id.compAddOkBtn)
                    viewCompleteOkBtn.setOnClickListener {
                        RefreshUtils().refreshActivity(this)
                    }
                }, 2000)
            }

            alertDialog.show()
        }

        val backBtn: ImageView = findViewById(R.id.addDeviceBack)
        backBtn.setOnClickListener {
            onBackPressed()
        }
    }
}