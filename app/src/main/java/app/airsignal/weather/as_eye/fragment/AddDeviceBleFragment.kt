package app.airsignal.weather.as_eye.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.FragmentAddDeviceBleBinding
import app.airsignal.weather.util.TimberUtil
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.coroutines.*

@SuppressLint("MissingPermission")
class AddDeviceBleFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding : FragmentAddDeviceBleBinding
    private val animatorSet by lazy { AnimatorSet() }
    private var isAnimationEnabled = true

    private val mainDispatcher = CoroutineScope(Dispatchers.Main)

    private val ble by lazy { parentActivity.ble }
    private val serial by lazy {ble.serial}

    private val scanCallback = object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {
            if (success) {
                mainDispatcher.launch {
                    binding.addBleTitle.text = getString(R.string.searching_eye)
                    startTextAnimation()
                }
            } else {
                stopTextAnimation()
            }
        }

        override fun onScanning(bleDevice: BleDevice?) {
            bleDevice?.let {
                if (bleDevice.name != null) {
                    TimberUtil().d("testtest", "onScanning ${bleDevice.name}")
                    if (serial != "Unknown" && bleDevice.device?.name == serial) {
                        ble.device = bleDevice
                        mainDispatcher.launch {
                            delay(4000)
                            binding.addBleTitle.text = getString(R.string.find_eye)
                            binding.addBleEyeSerial.text = serial
                            ble.cancelScan()
                            delay(1000)
                            changeModelVisibility(true)
                            delay(4000)
                            ble.connectDevice(bleDevice,connectCallback)
                        }
                    }
                }
            }
        }

        override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
            scanResultList?.let {
                if (ble.device == null) {
                    binding.addBleTitle.text = "${serial}을\n찾지 못했습니다"
                    requestReconnect(false)
                    ble.scanning = false
                }
            }
        }
    }

    private val connectCallback = object : BleGattCallback() {
        override fun onStartConnect() {
            mainDispatcher.launch {
                binding.addBleTitle.text = getString(R.string.connecting_bt)
            }
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            binding.addBleTitle.postDelayed({
                binding.addBleTitle.text = getString(R.string.fail_to_connect_eye)
                requestReconnect(false)
                ble.scanning = false
            },4000)
        }

        override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
            mainDispatcher.launch {
                delay(4000)
                stopTextAnimation()
                binding.addBleTitle.text = getString(R.string.success_to_connect_eye)
                ble.disconnect()
                delay(3000)
                parentActivity.transactionFragment(AddDeviceWifiFragment())
            }
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            stopTextAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTextAnimation()
        mainDispatcher.cancel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_device_ble, container, false)
        parentActivity.changeProgressWithAnimation(50)

        binding.addBleReconnectBtn.setOnClickListener {
            isAnimationEnabled = true
            requestReconnect(true)
            startTextAnimation()
            ble.startScan(scanCallback)
        }
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainDispatcher.launch {
            binding.addBleTitle.text = getString(R.string.ready_for_connect_bt)
            startTextAnimation()
            delay(5000)
            ble.startScan(scanCallback)
        }
    }

    private fun changeModelVisibility(b: Boolean) {
        binding.addBleModelContainer.visibility = if (b) View.VISIBLE else View.INVISIBLE
        if (b) binding.addBleModelContainer.animation =
                AnimationUtils.loadAnimation(requireContext(),R.anim.trans_bottom_to_top_add_group)
    }

    private fun startTextAnimation() {
        if (isAnimationEnabled) {
            // Fade-out 애니메이션 설정
            val fadeOutAnimator = ObjectAnimator.ofFloat(binding.addBleTitle, "alpha", 1f, 0.4f)
            fadeOutAnimator.duration = 500
            fadeOutAnimator.interpolator = AccelerateInterpolator()

            // Fade-in 애니메이션 설정
            val fadeInAnimator = ObjectAnimator.ofFloat(binding.addBleTitle, "alpha", 0.4f, 1f)
            fadeInAnimator.duration = 500
            fadeInAnimator.interpolator = AccelerateInterpolator()

            // 애니메이션 반복 설정
            animatorSet.playSequentially(fadeOutAnimator, fadeInAnimator)
            animatorSet.duration = 2000
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    startTextAnimation()
                }

                override fun onAnimationCancel(animation: Animator) {
                    binding.addBleTitle.alpha = 1f
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })

            // 애니메이션 시작
            animatorSet.start()
        }
    }

    private fun stopTextAnimation() {
        isAnimationEnabled = false
        animatorSet.cancel()
        binding.addBleTitle.alpha = 1f
    }

    private fun requestReconnect(b: Boolean) {
        binding.addBleReconnectBtn.visibility = if (b) View.GONE else View.VISIBLE
        binding.addBleReconnectTv.visibility = if (b) View.GONE else View.VISIBLE
        if (!b) {
            stopTextAnimation()
        }
    }
}