package com.example.airsignal_app.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * @author : Lee Jae Young
 * @since : 2023-05-11 오후 1:12
 **/
class KeyboardController(private val context: Context) {

    // 키보드 올리기
    fun onKeyboardUp(et: EditText) {
        Handler(Looper.getMainLooper()).postDelayed({
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            et.requestFocus()
            inputMethodManager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
        }, 300)
    }

    // 키보드 내리기
    fun onKeyboardDown(et: EditText) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(et.windowToken, InputMethodManager.SHOW_IMPLICIT)
    }
}