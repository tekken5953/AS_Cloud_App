package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.FragmentAddDeviceWifiPasswordBinding
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.util.TimberUtil
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.coroutines.*
import java.util.*

@SuppressLint("MissingPermission")
class AddDeviceWifiPasswordFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding: FragmentAddDeviceWifiPasswordBinding

    private var isPwdVisible = false

    private val ble by lazy { parentActivity.ble }

    private val mainDispatcher = CoroutineScope(Dispatchers.Main)

    private var isInit = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ble.disconnect()
        parentActivity.hidePb()
        parentActivity.showTopBar()
    }

    private val connectCallback = object : BleGattCallback() {
        override fun onStartConnect() {
            TimberUtil().d("testtest", "onStartConnect Ble to ${ble.serial}")
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            TimberUtil().e("testtest", "onConnectFail Ble to ${bleDevice?.device?.name}")
            mainDispatcher.launch {
                if (isInit) {
                    binding.addWifiPwdTitle.text = "AS-Eye와 연결이 끊어졌습니다\n다시 시도해주세요"
                    ble.destroyBle()
                    delay(1500)
                    parentActivity.finish()
                } else {
                    delay(5000)
                    reconnect()
                }
            }
        }

        override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
            TimberUtil().d("testtest", "onConnectSuccess Ble to ${bleDevice?.device?.name}")
            mainDispatcher.launch {
                if (isInit) {
                    parentActivity.changeTitleWithAnimation(
                        binding.addWifiPwdTitle,
                        "Wi-Fi 비밀번호를\n입력해주세요",
                        true
                    )
                    binding.addWifiPwdEt.visibility = View.VISIBLE
                    binding.addWifiPwdEt.animation =
                        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                    binding.addWifiPwdBtn.visibility = View.VISIBLE
                    binding.addWifiPwdBtn.animation =
                        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                } else {
                    // 와이파이 연결상태 불러오기
                    val readCallback = object : BleReadCallback() {
                        override fun onReadSuccess(data: ByteArray?) {
                            if (data?.toString(Charsets.UTF_8) == "1") {
                                // 연결 됨
                                confirmWifiConnect()
                            } else {
                                // 연결 안됨
                                mainDispatcher.launch {
                                    binding.addWifiPwdTitle.text = "입력하신 Wifi 정보가 올바르지 않습니다\n다시 시도해주세요"
                                    ble.disconnect()
                                    delay(2000)
                                    parentActivity.transactionFragment(AddDeviceWifiFragment())
                                }
                            }
                        }

                        override fun onReadFailure(exception: BleException?) {
                            // 읽기 실패
                            mainDispatcher.launch {
                                binding.addWifiPwdTitle.text = "AS-Eye와 연결이 끊어졌습니다\n다시 시도해주세요"
                                ble.disconnect()
                                delay(2000)
                                parentActivity.transactionFragment(AddDeviceWifiFragment())
                            }
                        }
                    }

                    ble.readConnected(readCallback)
                }
            }
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            TimberUtil().e("testtest", "onDisConnected Ble from ${device?.device?.name}")
        }
    }

    private val writePwdCallback = object : BleWriteCallback() {
        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
            mainDispatcher.launch {
                delay(3000)
                startComplete()
            }
        }

        override fun onWriteFailure(exception: BleException?) {
            if (ble.isConnected()) {
                parentActivity.changeTitleWithAnimation(
                    binding.addWifiPwdTitle,
                    "전송에 실패했습니다\n다시 시도해주세요",
                    true
                )
            } else {
                parentActivity.changeTitleWithAnimation(
                    binding.addWifiPwdTitle,
                    "블루투스 연결이 불안정합니다\n다시 시도해주세요",
                    true
                )
            }

            parentActivity.transactionFragment(AddDeviceBleFragment())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_device_wifi_password,
            container,
            false
        )
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
                    binding.addWifiPwdEt.clearFocus()
                    KeyboardController.onKeyboardDown(requireContext(), binding.addWifiPwdEt)
                    if (isPwdVisible) {
                        isPwdVisible = false
                        binding.addWifiPwdEt.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null, null, ResourcesCompat.getDrawable(
                                requireContext().resources,
                                R.drawable.invisible, null
                            ), null
                        )
                        binding.addWifiPwdEt.inputType =
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        isPwdVisible = true
                        binding.addWifiPwdEt.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null, null, ResourcesCompat.getDrawable(
                                requireContext().resources,
                                R.drawable.visible, null
                            ), null
                        )
                        binding.addWifiPwdEt.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
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
                parentActivity.changeTitleWithAnimation(
                    binding.addWifiPwdTitle,
                    "Wifi 정보를 전송중입니다", true
                )
                KeyboardController.onKeyboardDown(requireContext(),binding.addWifiPwdEt)
                ble.postSsid(binding.addWifiPwdEt.text.toString(), writePwdCallback)
            }
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ble.device?.let {
            ble.connectDevice(it, connectCallback)
        } ?: run {
            mainDispatcher.launch {
                binding.addWifiPwdTitle.text = "블루투스 연결에 실패했습니다\n다시 시도해주세요"
                ble.destroyBle()
                delay(1500)
                parentActivity.finish()
            }
        }
    }

    private fun reconnect() {
        if (!ble.isConnected()) {
            isInit = false
            ble.device?.let { device ->
                ble.connectDevice(device, connectCallback)
            }
        }
    }

    private fun startComplete() {
        binding.addWifiPwdBtn.visibility = View.GONE
        binding.addWifiPwdBtn.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_out)
        binding.addWifiPwdEt.visibility = View.GONE
        binding.addWifiPwdBtn.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_out)

        parentActivity.changeTitleWithAnimation(binding.addWifiPwdTitle, "기기로 등록 정보를\n전송중입니다",true)
        binding.addCompleteContent.visibility = View.VISIBLE
        parentActivity.changeTitleWithAnimation(binding.addCompleteContent,"전송완료 후 기기가 재부팅됩니다",true)
        parentActivity.changeProgressWithAnimation(90)
        parentActivity.showPb()

        CoroutineScope(Dispatchers.Main).launch {
            reconnect()
        }
    }

    private fun confirmWifiConnect() {
        mainDispatcher.launch {
            parentActivity.changeProgressWithAnimation(100)
            parentActivity.hidePb()
            delay(500)
            parentActivity.hideTopBar()
            binding.addWifiPwdTitle.visibility = View.GONE
            binding.addWifiPwdTitle.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_out)
            binding.addCompleteContent.visibility = View.GONE
            binding.addCompleteContent.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_out)

            withContext(Dispatchers.Main) {
                delay(2000)
                binding.addCompleteLinear.visibility = View.VISIBLE
                binding.addCompleteLinear.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in)
                delay(2500)
                binding.addCompleteLinear.visibility = View.GONE
                binding.addCompleteLinear.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_out)
                delay(2500)
                parentActivity.finish()
            }
        }
    }
}