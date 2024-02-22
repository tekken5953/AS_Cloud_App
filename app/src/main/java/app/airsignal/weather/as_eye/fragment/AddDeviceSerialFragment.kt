package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.FragmentAddDeviceSerialBinding
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import kotlinx.coroutines.*
import java.util.*


class AddDeviceSerialFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding : FragmentAddDeviceSerialBinding

    private val ble by lazy { parentActivity.ble }

    private val maxSerialLength = 11

    private val perm by lazy {RequestPermissionsUtil(parentActivity)}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
        if (!perm.isGrantBle()) perm.requestBlePermissions()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_device_serial, container, false)
        parentActivity.changeTitleWithAnimation(binding.addSerialTitle,getString(R.string.input_serial_on_back),false)
        parentActivity.changeProgressWithAnimation(25)
        binding.addSerialEt.visibility = View.VISIBLE
        binding.addSerialEt.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in_group_add)
        binding.addSerialBtn.visibility = View.VISIBLE
        binding.addSerialBtn.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.trans_bottom_to_top_add_group)

        val nextBtn = binding.addSerialBtn
        nextBtn.setOnClickListener {
            if (nextBtn.isEnabled) {
                if (perm.isGrantBle()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        ble.serial = binding.addSerialEt.text.toString()
                        delay(1000)
                        parentActivity.transactionFragment(AddDeviceBleFragment())
                        KeyboardController.onKeyboardDown(requireContext(), binding.addSerialEt)
                    }
                } else {
                    perm.requestBlePermissions()
                }
            }
        }

        binding.addSerialEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (s.length == maxSerialLength) {
                        binding.addSerialBtn.isEnabled = true
                        binding.addSerialBtn.setTextColor(requireContext().getColor(R.color.white))
                    } else {
                        binding.addSerialBtn.isEnabled = false
                        binding.addSerialBtn.setTextColor(requireContext().getColor(R.color.eye_btn_disable_color))
                    }
                } ?: run {
                    binding.addSerialBtn.isEnabled = false
                    binding.addSerialBtn.setTextColor(requireContext().getColor(R.color.eye_btn_disable_color))
                }
            }

            override fun afterTextChanged(s: Editable?) {
//                if (s != null && s.isNotEmpty()) {
//                    val containsLowerCase = s.contains(Regex("[a-z]"))
//                    if (containsLowerCase) s.replace(0, s.length, s.toString().uppercase())
//                }
            }
        })

        binding.addSerialEt.setOnTouchListener { _, motionEvent ->
            try {
                if (motionEvent.action == MotionEvent.ACTION_UP &&
                    motionEvent.rawX >= binding.addSerialEt.right
                    - binding.addSerialEt.compoundDrawablesRelative[2].bounds.width()
                ) {
                    binding.addSerialEt.text.clear()
                    binding.addSerialEt.requestFocus()
                    KeyboardController.onKeyboardUp(requireContext(),binding.addSerialEt)
                    return@setOnTouchListener true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            false
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}