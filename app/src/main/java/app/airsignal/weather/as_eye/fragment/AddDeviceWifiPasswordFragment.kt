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
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.FragmentAddDeviceWifiPasswordBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.util.ToastUtils
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@SuppressLint("MissingPermission")
class AddDeviceWifiPasswordFragment : BaseEyeFragment<FragmentAddDeviceWifiPasswordBinding>() {
    override val resID: Int get() = R.layout.fragment_add_device_wifi_password
    private lateinit var parentActivity: AddEyeDeviceActivity

    private var isPwdVisible = false

    private val ble by lazy { parentActivity.ble }

    private val mainDispatcher = CoroutineScope(Dispatchers.Main)

    private var isInit = true

    private var isCapability = true

    private var ssid: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
        isCapability = arguments?.getBoolean("capability") ?: true
        ssid = arguments?.getString("ssid")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ble.disconnect()
        mainDispatcher.cancel()
        parentActivity.hidePb()
        parentActivity.showTopBar()
    }

    private val connectCallback = object : BleGattCallback() {
        override fun onStartConnect() {}

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            mainDispatcher.launch {
                binding.addWifiPwdTitle.text = getString(R.string.eye_fail_to_connect_bt)
                ble.destroyBle()
                delay(1500)
                parentActivity.finish()
            }
        }

        override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
            mainDispatcher.launch {
                if (isCapability) {
                    parentActivity.changeTitleWithAnimation(
                        binding.addWifiPwdTitle,
                        getString(R.string.eye_input_wifi_password),
                        true
                    )
                    binding.addWifiPwdEt.visibility = View.VISIBLE
                    binding.addWifiPwdEt.animation =
                        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                    binding.addWifiPwdBtn.visibility = View.VISIBLE
                    binding.addWifiPwdBtn.animation =
                        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                } else {
                    parentActivity.changeTitleWithAnimation(
                        binding.addWifiPwdTitle,
                        getString(R.string.sending_wifi_info), true
                    )
                    KeyboardController.onKeyboardDown(requireContext(), binding.addWifiPwdEt)
                    postSSID()
                }
            }
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            mainDispatcher.launch {
                binding.addWifiPwdTitle.text = getString(R.string.eye_disconnect_retry)
                ble.destroyBle()
                delay(1500)
                parentActivity.finish()
            }
        }
    }

    private fun inputDeviceAlias() {
        binding.addWifiPwdEt.text.clear()
        binding.addWifiPwdEt.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,null,null,null)

        binding.addWifiPwdEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.isNotEmpty()) {
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

        parentActivity.hidePb()
        parentActivity.changeTitleWithAnimation(
            binding.addWifiPwdTitle,
            getString(R.string.eye_input_alias),
            true
        )
        binding.addWifiPwdEt.apply {
            visibility = View.VISIBLE
            inputType = InputType.TYPE_CLASS_TEXT
            hint = context.getString(R.string.eye_example)
            doOnTextChanged { text, _, _, _ ->
                text?.let { binding.addWifiPwdBtn.isEnabled = it.isNotEmpty() }
            }
        }

        binding.addWifiPwdBtn.visibility = View.VISIBLE
        binding.addWifiPwdBtn.setOnClickListener {
            binding.addWifiPwdBtn.visibility = View.GONE
            binding.addWifiPwdEt.visibility = View.GONE

            // FCM 토픽 Subscribe
            SubFCM().subTopic(ble.serial)

            postDevice(
                SharedPreferenceManager(requireContext()).getString(SpDao.userEmail),
                EyeDataModel.PostDevice(serial = parentActivity.ble.serial,
            alias = binding.addWifiPwdEt.text.toString(), isMaster = "T"))
        }
    }

    private val readCallback = object : BleReadCallback() {
        override fun onReadSuccess(data: ByteArray?) {
            if (data?.toString(Charsets.UTF_8) == "1") inputDeviceAlias() // 연결 됨
            else {
                // 연결 안됨
                mainDispatcher.launch {
                    parentActivity.hidePb()
                    binding.addWifiPwdTitle.text = getString(R.string.eye_wifi_connect_fail)
                    ble.disconnect()
                    delay(2000)
                    parentActivity.transactionFragment(AddDeviceWifiFragment())
                }
            }
        }

        override fun onReadFailure(exception: BleException?) {
            // 읽기 실패
            mainDispatcher.launch {
                parentActivity.hidePb()
                binding.addWifiPwdTitle.text = getString(R.string.eye_disconnect_retry)
                ble.disconnect()
                delay(2000)
                parentActivity.transactionFragment(AddDeviceWifiFragment())
            }
        }
    }

    private val writeSsidCallback = object : BleWriteCallback() {
        override fun onWriteSuccess(
            current: Int,
            total: Int,
            justWrite: ByteArray?
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                ble.instance.removeConnectGattCallback(ble.device)
                delay(2000)
                if (isCapability) ble.postPwd(binding.addWifiPwdEt.text.toString(), writePwdCallback)
//                else ble.readConnected(readCallback = readCallback)
                else inputDeviceAlias()
            }
        }

        override fun onWriteFailure(exception: BleException?) {
            ble.device?.let {
                ble.instance.disconnect(it)
            }
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
                    getString(R.string.fail_to_send_retry),
                    true
                )
            } else {
                parentActivity.changeTitleWithAnimation(
                    binding.addWifiPwdTitle,
                    getString(R.string.confuse_bt_retry),
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

        binding.addWifiPwdEt.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (it.length >= 8) {
                    binding.addWifiPwdBtn.isEnabled = true
                    binding.addWifiPwdBtn.setTextColor(requireContext().getColor(R.color.white))
                } else {
                    binding.addWifiPwdBtn.isEnabled = false
                    binding.addWifiPwdBtn.setTextColor(requireContext().getColor(R.color.eye_btn_disable_color))
                }
            }
        }

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
                    getString(R.string.sending_wifi_info), true
                )
                binding.addWifiPwdBtn.isEnabled = false
                KeyboardController.onKeyboardDown(requireContext(),binding.addWifiPwdEt)
                postSSID()
            }
        }

        return binding.root
    }

    private fun postSSID() {
        ssid?.let {
            ble.postSsid(it, writeSsidCallback)
        } ?: run {
            binding.addWifiPwdBtn.isEnabled = true
            ToastUtils(requireContext()).showMessage(getString(R.string.no_right_wifi))
            parentActivity.transactionFragment(AddDeviceWifiFragment())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { arg ->
            val serial = arg.getString("serial")
            val deviceId = arg.getString("deviceId")
            val alias = arg.getString("alias")
            val isMaster = arg.getString("isMaster")
            val ssid = arg.getString("ssid")
            val capability = arg.getBoolean("capability")
            if (serial != null && deviceId != null && alias != null && isMaster != null) {
                postDevice(deviceId, EyeDataModel.PostDevice(serial, alias, isMaster))
            } else if (ssid != null) {
                ble.device?.let { device ->
                    ble.connectDevice(device, connectCallback)
                } ?: run {
                    mainDispatcher.launch {
                        binding.addWifiPwdTitle.text = getString(R.string.fail_to_connect_bt_retry)
                        ble.destroyBle()
                        delay(1500)
                        parentActivity.finish()
                    }
                }
            }
        } ?: run {
            ToastUtils(requireContext()).showMessage(getString(R.string.no_right_wifi))
            parentActivity.transactionFragment(AddDeviceWifiFragment())
        }
    }

    private fun reconnect() {
        if (!ble.isConnected()) {
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

        parentActivity.changeTitleWithAnimation(binding.addWifiPwdTitle, getString(R.string.transferring_data_to_device),true)
        parentActivity.changeProgressWithAnimation(90)
        parentActivity.showPb()
        isInit = false
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
//            ble.readConnected(readCallback)
            inputDeviceAlias()
        }
    }

    private fun confirmWifiConnect() {
        mainDispatcher.launch {
            parentActivity.changeProgressWithAnimation(100)
            delay(500)
            parentActivity.hideTopBar()
            binding.addWifiPwdTitle.visibility = View.GONE
            binding.addWifiPwdTitle.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_out)

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

    private fun postDevice(email: String, item: EyeDataModel.PostDevice) {
        HttpClient.retrofit.postDevice(email, item).enqueue(
            object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    try { if (response.isSuccessful) confirmWifiConnect()
                    } catch (e: Exception) { e.stackTraceToString() }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    t.stackTraceToString()
                }
            }
        )
    }
}