package app.airsignal.weather.utils.controller

import android.content.Context
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.os.HandlerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object KeyboardController {
    // 키보드 올리기
    fun onKeyboardUp(context: Context, et: EditText) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!inputMethodManager.isAcceptingText) {
            HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                et.requestFocus()
                inputMethodManager.showSoftInput(et, 1)
            },300)
        }
    }

    // 키보드 내리기
    fun onKeyboardDown(context: Context, et: EditText) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        et.clearFocus()
        inputMethodManager.hideSoftInputFromWindow(et.windowToken, 0)
    }
}