package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.FragmentAddDeviceCompleteBinding
import app.airsignal.weather.databinding.FragmentAddDeviceWifiPasswordBinding
import app.airsignal.weather.util.KeyboardController

class AddDeviceWifiPasswordFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding : FragmentAddDeviceWifiPasswordBinding

    private var isPwdVisible = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_device_wifi_password, container, false)
        parentActivity.changeTitleWithAnimation(binding.addWifiPwdTitle, "Wi-Fi 비밀번호를\n입력해주세요",true)

        binding.addWifiPwdEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.length >= 8) {
                        binding.addWifiPwdBtn.isEnabled = true
                        binding.addWifiPwdBtn.setTextColor(requireContext().getColor(R.color.white))
                    } else {
                        binding.addWifiPwdBtn.isEnabled = false
                        binding.addWifiPwdBtn.setTextColor(requireContext().getColor(R.color.eye_btn_disable_color))
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.addWifiPwdEt.setOnTouchListener { _, motionEvent ->
            try {
                if (motionEvent.action == MotionEvent.ACTION_UP &&
                    motionEvent.rawX >= binding.addWifiPwdEt.right
                    - binding.addWifiPwdEt.compoundDrawablesRelative[2].bounds.width()
                ) {
                    if (isPwdVisible) {
                        isPwdVisible = false
                        binding.addWifiPwdEt.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,null, ResourcesCompat.getDrawable(requireContext().resources,
                            R.drawable.invisible,null),null)
                        binding.addWifiPwdEt.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        isPwdVisible = true
                        binding.addWifiPwdEt.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,null, ResourcesCompat.getDrawable(requireContext().resources,
                                R.drawable.visible,null),null)
                        binding.addWifiPwdEt.inputType = 129
                    }
                    return@setOnTouchListener true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            false
        }

        binding.addWifiPwdBtn.setOnClickListener {
            if (binding.addWifiPwdBtn.isEnabled) {
                parentActivity.transactionFragment(AddDeviceCompleteFragment())
                KeyboardController.onKeyboardDown(requireContext(), binding.addWifiPwdEt)
            }
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}