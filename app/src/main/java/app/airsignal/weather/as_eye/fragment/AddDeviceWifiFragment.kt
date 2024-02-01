package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.as_eye.adapter.AddDeviceWifiAdapter
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.FragmentAddDeviceWifiBinding
import app.airsignal.weather.util.OnAdapterItemClick
import kotlin.math.absoluteValue

@Suppress("DEPRECATION")
class AddDeviceWifiFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding: FragmentAddDeviceWifiBinding
    private val wifiList = ArrayList<EyeDataModel.Wifi>()
    private val wifiAdapter by lazy { AddDeviceWifiAdapter(requireContext(),wifiList) }

    private val wifiManager by lazy {parentActivity.application.getSystemService(Context.WIFI_SERVICE) as WifiManager}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_device_wifi, container,false)
        parentActivity.changeTitleWithAnimation(binding.addWifiTitle,"연결할 Wi-Fi를 선택해주세요",true)
        parentActivity.changeProgressWithAnimation(70)
        binding.addWifiConnectRv.adapter = wifiAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wifiAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick{
            override fun onItemClick(v: View, position: Int) {
                wifiList[position].capability?.let { cap ->
                    parentActivity.transactionFragment(AddDeviceWifiPasswordFragment())
                }
            }
        })

        binding.addWifiRefresh.setOnClickListener {
            changeScanResult()
        }

        changeScanResult()
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private fun changeScanResult() {
        binding.addWifiConnectRv.visibility = View.VISIBLE
        wifiList.clear()
        wifiAdapter.notifyDataSetChanged()
        wifiManager.scanResults.forEachIndexed { index, data ->
            if (data.SSID != "") {
                addWifi(data.SSID, data.level, data.capabilities)
                wifiAdapter.notifyItemInserted(index)
            }
        }
        val sortedByLevel = wifiList.sortedBy { it.level?.absoluteValue }
        wifiList.clear()
        wifiList.addAll(sortedByLevel)
        wifiAdapter.notifyItemRangeChanged(0, wifiList.size)
    }

    private fun addWifi(ssid: String, level: Int?, capability: String?) {
        val item = EyeDataModel.Wifi(ssid,level,capability)
        wifiList.add(item)
    }
}