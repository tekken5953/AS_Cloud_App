package app.airsignal.weather.util

import android.content.Context
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.os.HandlerCompat

object KeyboardController {
    // 키보드 올리기
    fun onKeyboardUp(context: Context, et: EditText) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
            et.requestFocus()
            inputMethodManager.showSoftInput(et,InputMethodManager.SHOW_IMPLICIT)
        },100)
    }

    // 키보드 내리기
    fun onKeyboardDown(context: Context, et: EditText) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        et.clearFocus()
        inputMethodManager.hideSoftInputFromWindow(et.windowToken, 0)
    }
}