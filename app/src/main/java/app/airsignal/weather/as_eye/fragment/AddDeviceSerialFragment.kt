package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.os.HandlerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.as_eye.nfc.NfcInfoFragment
import app.airsignal.weather.databinding.FragmentAddDeviceSerialBinding
import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.network.retrofit.MyApiImpl
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddDeviceSerialFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding : FragmentAddDeviceSerialBinding

    private var stateInspection = 0

    private val ble by lazy { parentActivity.ble }

    private val maxSerialLength = 14

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
                KeyboardController.onKeyboardDown(requireContext(), binding.addSerialEt)
                when (stateInspection) {
                    0 -> {
                        getOwners(binding.addSerialEt.text.toString())
                    }
                    1 -> {
                        parentActivity.transactionFragment(NfcInfoFragment())
                    }
                    2 -> {
                        parentActivity.transactionFragment(AddDeviceBleFragment())
                    }
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
                if (s != null && s.isNotEmpty()) {
                    stateInspection = 0
                    val containsLowerCase = s.contains(Regex("[a-z]"))
                    if (containsLowerCase) s.replace(0, s.length, s.toString().uppercase())
                }
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
                    HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                        KeyboardController.onKeyboardUp(requireContext(),binding.addSerialEt)
                    },500)
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


    private fun getOwners(sn: String) {
        parentActivity.showPb()
        HttpClient.setClientBuilder().getOwner(sn).enqueue(object : Callback<List<String>>{
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (response.isSuccessful) {
                        delay(2000)
                        val body = response.body()
                        TimberUtil().d("eyetest","serial : $sn body : $body")
                        ble.serial = sn
                        withContext(Dispatchers.Main) {
                            parentActivity.hidePb()
                            body?.let {
                                if (body.isNotEmpty()) {
                                    // 등록한 사용자가 있음
                                    stateInspection = 1
                                    binding.addSerialResultTitle.text = "'${it[0]}'님이 소유하신 기기입니다\n게스트로 등록하시겠습니까?"
                                    binding.addSerialResultCaution.text = ""
                                    binding.addSerialResultContainer.visibility = View.VISIBLE
                                    binding.addSerialResultContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in))
                                } else {
                                    // 등록한 사용자가 없음
                                    stateInspection = 2
                                    binding.addSerialResultTitle.text = "등록되지 않은 기기입니다\n새로 등록하시겠습니까?"
                                    binding.addSerialResultCaution.text = "올바른 시리얼 번호인지 확인해주세요"
                                    binding.addSerialResultContainer.visibility = View.VISIBLE
                                    binding.addSerialResultContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in))
                                }
                            } ?: run {
                                TimberUtil().e("eyetest","fail : ${response.errorBody()}")
                                parentActivity.changeTitleWithAnimation(binding.addSerialTitle,getString(R.string.input_serial_on_back),true)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                // 통신 실패
                TimberUtil().e("eyetest","fail : ${t.stackTraceToString()}")
                parentActivity.changeTitleWithAnimation(binding.addSerialTitle,getString(R.string.input_serial_on_back),true)
            }
        })
    }
}