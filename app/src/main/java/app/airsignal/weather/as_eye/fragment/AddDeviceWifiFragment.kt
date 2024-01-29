package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.HandlerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.as_eye.adapter.AddDeviceWifiAdapter
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.as_eye.wifi.WifiConnectionCallback
import app.airsignal.weather.as_eye.wifi.WifiReceiver
import app.airsignal.weather.databinding.FragmentAddDeviceWifiBinding
import app.airsignal.weather.util.OnAdapterItemClick
import app.airsignal.weather.util.TimberUtil

@Suppress("DEPRECATION")
class AddDeviceWifiFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding: FragmentAddDeviceWifiBinding
    private val wifiList = ArrayList<EyeDataModel.Wifi>()
    private val wifiAdapter by lazy { AddDeviceWifiAdapter(requireContext(),wifiList) }

    private val wifiManager by lazy {parentActivity.application.getSystemService(Context.WIFI_SERVICE) as WifiManager}
    private var wifiConnectionCallback: WifiConnectionCallback? = null
    private val wifiReceiver by lazy { WifiReceiver(requireContext(),wifiManager,wifiConnectionCallback)}

    private val onWifiConnectResult: WifiConnectionCallback =
        object :WifiConnectionCallback {
            override fun onSuccess(info: WifiInfo?) {
                Toast.makeText(requireContext(), "${info?.ssid} 연결 성공", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure() {
                Toast.makeText(requireContext(), "와이파이 연결 실패", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        wifiReceiver.unregisterWifiReceiver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_device_wifi, container,false)

        binding.wifiConnectRv.adapter = wifiAdapter

        wifiConnectionCallback = onWifiConnectResult

        wifiReceiver.registerWifiReceiver()
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
            val result: List<ScanResult> = wifiManager.scanResults
            result.forEachIndexed { index, data ->
                if (data.SSID != "") {
                    addWifi(data.SSID, data.level, data.capabilities)
                    wifiAdapter.notifyItemInserted(index)
                }
            }
        },1500)

        wifiAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick{
            override fun onItemClick(v: View, position: Int) {
                if (wifiAdapter.isCapability) {
                    Toast.makeText(context, "비밀번호 입력 모달 생성", Toast.LENGTH_SHORT).show()
                    wifiReceiver.connectToWifi(wifiList[position].ssid,"ebtech2023")
                } else {
                    Toast.makeText(context, "Eye 와이파이 연결", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun addWifi(ssid: String, level: Int, capability: String) {
        val item = EyeDataModel.Wifi(ssid,level,capability)
        wifiList.add(item)
    }
}