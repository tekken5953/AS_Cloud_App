package app.airsignal.weather.as_eye.wifi

import android.net.wifi.WifiInfo

interface WifiConnectionCallback {
    fun onSuccess(info: WifiInfo?)
    fun onFailure()
}