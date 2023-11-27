package app.airsignal.weather.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.R
import kotlin.system.exitProcess

class MakeSingleDialog(private val context: Context) {
    lateinit var apply: AppCompatButton
    val builder = Dialog(context)

    // 버튼이 하나인 다이얼로그 생성
    @SuppressLint("InflateParams")
    fun makeDialog(
        textTitle: String,
        color: Int,
        buttonText: String,
        cancelable: Boolean
    ): AppCompatButton {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_alert_single_btn, null)
        builder.apply {
            this.window?.setBackgroundDrawableResource(R.drawable.dialog_bg)
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.setContentView(view)
            this.setCancelable(cancelable)
            this.create()

            val title = view.findViewById<TextView>(R.id.alertSingleTitle)
            apply = view.findViewById(R.id.alertSingleApplyBtn)
            apply.backgroundTintList = ColorStateList.valueOf(color)

            title.text = textTitle
            apply.text = buttonText
            apply.setOnClickListener { exitProcess(0) }

            this.show()

            return apply
        }
    }

    fun dismiss() { builder.dismiss() }
}