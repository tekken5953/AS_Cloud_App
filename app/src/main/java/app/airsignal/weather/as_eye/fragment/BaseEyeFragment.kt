package app.airsignal.weather.as_eye.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import app.airsignal.weather.R

abstract class BaseEyeFragment<VB : ViewDataBinding>(): Fragment() {
    protected lateinit var binding: VB
    abstract val resID: Int

    protected fun initBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = DataBindingUtil.inflate(inflater, R.layout.eye_detail_live_fragment, container, false)
    }

    fun blockTouch(b: Boolean) {
        if (b) requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        else requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}