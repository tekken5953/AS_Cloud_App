package app.airsignal.weather.view

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import app.airsignal.weather.R

class MakeDoubleDialog(private val context: Context) {
    val builder = Dialog(context)
    fun make(titleString: String, applyString: String, cancelString: String, applyColor: Int)
            : Pair<AppCompatButton, AppCompatButton> {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_alert_double_btn, null)
        builder.apply {
            this.window?.setBackgroundDrawable(ColorDrawable(context.getColor(R.color.theme_view_color)))
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.setContentView(view)
            this.setCancelable(true)
            this.create()

            val cancel = view.findViewById<AppCompatButton>(R.id.alertDoubleCancelBtn)
            val apply = view.findViewById<AppCompatButton>(R.id.alertDoubleApplyBtn)
            val title = view.findViewById<TextView>(R.id.alertDoubleTitle)

            title.text = titleString
            apply.text = applyString
            apply.backgroundTintList = ColorStateList.valueOf(context.getColor(applyColor))
            cancel.text = cancelString
            cancel.setOnClickListener {
                this.dismiss()
            }

            this.show()

            return Pair(apply, cancel)
        }
    }

    fun dismiss() {
        builder.dismiss()
    }
}