package com.example.airsignal_app.view

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.example.airsignal_app.R
import kotlin.system.exitProcess

class MakeSingleDialog(private val context: Context) {
    lateinit var apply: AppCompatButton
    lateinit var builder: Dialog

    fun makeDialog(textTitle: String, color: Int, buttonText: String): AppCompatButton {
        builder = Dialog(context)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_alert_single_btn,null)
        builder.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(view)
            setCancelable(false)
        }

        builder.create()

        val title = view.findViewById<TextView>(R.id.alertSingleTitle)
        apply = view.findViewById(R.id.alertSingleApplyBtn)

        apply.backgroundTintList = ColorStateList.valueOf(color)

        title.text = textTitle
        apply.text = buttonText
        apply.setOnClickListener {
            exitProcess(0)
        }
        builder.show()

        return apply
    }

    fun dismiss() {
        builder.dismiss()
    }
}