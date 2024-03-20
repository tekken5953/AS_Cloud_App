package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.nfc.NfcAdapter
import android.os.Bundle
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
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.as_eye.nfc.NfcInfoFragment
import app.airsignal.weather.databinding.FragmentAddDeviceSerialBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.sp.SpDao.userEmail
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddDeviceSerialFragment : BaseEyeFragment<FragmentAddDeviceSerialBinding>() {
    override val resID: Int get() = R.layout.fragment_add_device_serial
    private lateinit var baseActivity: AddEyeDeviceActivity

    private var stateInspection = 0

    private val ble by lazy { baseActivity.ble }

    private val maxSerialLength = 14

    private val perm by lazy {RequestPermissionsUtil(baseActivity)}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) baseActivity = context
        if (!perm.isGrantBle()) perm.requestBlePermissions()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_device_serial, container, false)
        baseActivity.changeTitleWithAnimation(binding.addSerialTitle,getString(R.string.input_serial_on_back),false)
        baseActivity.changeProgressWithAnimation(25)
        binding.addSerialEt.visibility = View.VISIBLE
        binding.addSerialEt.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in_group_add)
        binding.addSerialBtn.visibility = View.VISIBLE
        binding.addSerialBtn.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.trans_bottom_to_top_add_group)

        val nextBtn = binding.addSerialBtn
        nextBtn.setOnClickListener {
            if (nextBtn.isEnabled) {
                KeyboardController.onKeyboardDown(requireContext(), binding.addSerialEt)
                when (stateInspection) {
                    0 -> { getOwners(binding.addSerialEt.text.toString()) }
                    1 -> { baseActivity.transactionFragment(NfcInfoFragment()) }
                    2 -> { baseActivity.transactionFragment(AddDeviceBleFragment()) }
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
        baseActivity.showPb()
        HttpClient.setClientBuilder().getOwner(sn).enqueue(object : Callback<List<ApiModel.Owner>>{
            override fun onResponse(call: Call<List<ApiModel.Owner>>, response: Response<List<ApiModel.Owner>>) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (response.isSuccessful) {
                        delay(2000)
                        val body = response.body()
                        TimberUtil().d("eyetest","serial : $sn body : $body")
                        ble.serial = sn
                        withContext(Dispatchers.Main) {
                            baseActivity.hidePb()
                            body?.let { responseBody ->
                                binding.addSerialResultContainer.visibility = View.VISIBLE
                                binding.addSerialResultContainer.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

                                if (responseBody.find { it.id == SharedPreferenceManager(requireContext()).getString(userEmail)} != null) {
                                    failApi("중복 등록 에러","이미 등록하신 기기입니다", "중복 등록은 불가능합니다")
                                } else {
                                    if (body.isNotEmpty()) {
                                        val master = responseBody.find { it.master }
                                        if (master != null) {
                                            // 등록한 사용자가 있음
                                            successApi(1,
                                                "'${master.id}'\n님이 소유하신 기기입니다",
                                                "게스트로 등록하시겠습니까?"
                                            )
                                        } else {
                                            // 게스트는 있지만 소유자가 없음
                                            successApi(2,
                                                "소유자가 없는 기기입니다\n새로 등록하시겠습니까?",
                                                "올바른 시리얼 번호인지 확인해주세요"
                                            )
                                        }
                                    } else {
                                        // 등록한 사용자가 없음
                                        successApi(2,
                                            "등록되지 않은 기기입니다\n새로 등록하시겠습니까?",
                                            "올바른 시리얼 번호인지 확인해주세요"
                                        )
                                    }
                                }
                            } ?: run {
                                failApi(response.errorBody().toString(), "데이터 호출에 실패했습니다\n올바른 시리얼 번호인지 확인해주세요","")
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<ApiModel.Owner>>, t: Throwable) {
                // 통신 실패
                failApi(t.stackTraceToString(), "서버와 통신에 실패했습니다\n잠시후에 다시 시도해주세요", "")
            }
        })
    }

    private fun successApi(inspectionCode: Int, title: String, caution: String) {
        binding.addSerialResultContainer.isActivated = false
        stateInspection = inspectionCode
        binding.addSerialResultTitle.text = title
        binding.addSerialResultCaution.text = caution
        binding.addSerialResultError.visibility = View.GONE
        binding.addSerialResultTitle.visibility = View.VISIBLE
        binding.addSerialResultCaution.visibility = if (caution != "") View.VISIBLE else View.INVISIBLE
        binding.addSerialResultContainer.visibility = View.VISIBLE
        binding.addSerialResultContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in))
    }

    private fun failApi(errorMsg: String, title: String, caution: String) {
        binding.addSerialResultContainer.isActivated = true
        stateInspection = 0
        TimberUtil().e("eyetest",errorMsg)
        binding.addSerialResultError.visibility = View.VISIBLE
        binding.addSerialResultTitle.visibility = View.VISIBLE
        binding.addSerialResultCaution.visibility = View.INVISIBLE
        binding.addSerialResultError.text = title
        binding.addSerialResultCaution.visibility = if (caution != "") View.VISIBLE else View.INVISIBLE
        binding.addSerialResultContainer.visibility = View.VISIBLE
        binding.addSerialResultContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in))
    }
}