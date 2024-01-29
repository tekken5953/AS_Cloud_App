package app.airsignal.weather.as_eye.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import app.airsignal.weather.util.TimberUtil
import kotlinx.coroutines.*


@Suppress("DEPRECATION")
class WifiReceiver(
    private val context: Context,
    private val manager: WifiManager,
    private val connectionCallback: WifiConnectionCallback?)
    : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            val networkInfo: NetworkInfo? =
                intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
            when (networkInfo?.state) {
                NetworkInfo.State.CONNECTED -> {
                    // 와이파이에 연결 성공
                    val wifiInfo: WifiInfo? = manager.connectionInfo
                    connectionCallback?.onSuccess(wifiInfo)
                    TimberUtil().i("testtest","CONNECTED : ${wifiInfo?.ssid}")
                }
                NetworkInfo.State.DISCONNECTED -> {
                    // 와이파이 연결 실패
                    connectionCallback?.onFailure()
                    TimberUtil().e("testtest","DISCONNECTED")
                }
                else -> {}
            }
        } else {
            TimberUtil().w("testtest","else receive ${intent.action}")
        }
    }

    fun connectToWifi(ssid: String, password: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            if (!manager.isWifiEnabled) {
//                manager.isWifiEnabled = true
//                TimberUtil().e("testtest", "!manager.isWifiEnabled")
//            }
//
//            withContext(Dispatchers.IO) {
//                val wifiConfig = WifiConfiguration()
//
//                wifiConfig.SSID = "\"" + ssid + "\""
//                wifiConfig.preSharedKey = "\"" + password + "\""
//
//                val netId: Int = manager.addNetwork(wifiConfig)
//                manager.run {
//                    disconnectFromWifi()
//                    delay(1000)
//                    enableNetwork(netId, true)
//                    reconnect()
//                }
//                TimberUtil().i("testtest", "new Config : ${wifiConfig.SSID},${wifiConfig.preSharedKey}")
//            }
//
//            CoroutineScope(Dispatchers.Main).launch {
//                delay(1000)  // 적절한 시간 설정
//                TimberUtil().d("testtest","${manager.connectionInfo}")
//            }
//        }
        context.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
    }

    private fun disconnectFromWifi() {
        val currentNetworkId = manager.connectionInfo.networkId
        TimberUtil().w("testtest", "disconnectFromWifi id is $currentNetworkId by ${manager.connectionInfo.ssid}")
        if (currentNetworkId != -1) {
            manager.disableNetwork(currentNetworkId)
            manager.disconnect()
        }
        TimberUtil().d("testtest", "After disconnect: ${manager.connectionInfo}")
    }

    fun registerWifiReceiver() {
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        context.registerReceiver(this, filter)
        TimberUtil().d("testtest","registerWifiReceiver")
    }

    fun unregisterWifiReceiver() {
        context.unregisterReceiver(this)
        TimberUtil().w("testtest","unregisterWifiReceiver")
    }
}


