package app.airsignal.weather.utils.controller

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
            inputMethodManager.showSoftInput(et,1)
            et.requestFocus()
        },300)
    }

    // 키보드 내리기
    fun onKeyboardDown(context: Context, et: EditText) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        et.clearFocus()
        inputMethodManager.hideSoftInputFromWindow(et.windowToken, 0)
    }
}