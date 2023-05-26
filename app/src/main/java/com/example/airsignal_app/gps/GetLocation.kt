package com.example.airsignal_app.gps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.*
import android.location.LocationListener
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLogCause
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLogNotLogin
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.util.ConvertDataType
import com.example.airsignal_app.util.GetDeviceInfo
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.util.*


class GetLocation(private val context: Context) {
    private val sp by lazy { SharedPreferenceManager(context) }


    /** GPS 의 위치정보를 불러온 후 이전 좌표와의 거리를 계산합니다 **/
    @SuppressLint("MissingPermission")
    fun getLocationInBackground() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    getAddress(it.latitude, it.longitude)
                }
            }.addOnFailureListener {
                it.printStackTrace()
                it.localizedMessage?.let { it1 ->
                    writeLogCause(
                        email = "Error",
                        isSuccess = "주소 불러오기 실패",
                        log = it1
                    )
                }
                Logger.t(TAG_D).e("Fail to Get Location")
            }
    }

    /** 현재 주소를 불러옵니다 **/
    fun getAddress(lat: Double, lng: Double): String {
        val email = sp.getString(userEmail)
        lateinit var address: List<Address>
        try {
            val geocoder = Geocoder(context, ConvertDataType.getLocale(context))
            @Suppress("DEPRECATION")
            address = geocoder.getFromLocation(lat, lng, 1) as List<Address>
            if (address.isNotEmpty()) {
                return address[0].getAddressLine(0)

//                Log.i(TAG_D, newAddress) // 건물 주소를 제외한 주소 출력
//
//                try {
//                    updateCurrentAddress (
//                        address[0].latitude, address[0].longitude, newAddress
//                    )
//
//                    writeRdbLog(address[0].latitude, address[0].longitude, newAddress)
////                            renewTopic(sp.getString("WEATHER_CURRENT"), lastAddress)
//                    return newAddress
//                } catch (e: Exception) {
//                    Timber.tag("Location").e("Location Contains null")
//                }
            }
        } catch (e: IOException) {
            Timber.tag("Location").e("주소를 가져오는 도중 오류가 발생했습니다")
            writeLogCause(
                email,
                "Background Location Exception",
                "Error : ${e.localizedMessage}"
            )
        }
        return ""
    }

    fun formattingFullAddress(fullAddr: String): String {
        val addressParts = fullAddr.split(" ").toTypedArray() // 공백을 기준으로 주소 요소 분리
        var formattedAddress = ""
        for (i in 0 until addressParts.size - 1) {
            formattedAddress += addressParts[i].trim { it <= ' ' } // 건물 주소를 제외한 나머지 요소 추출
            if (i < addressParts.size - 2) {
                formattedAddress += " " // 요소 사이에 공백 추가
            }
        }

        return  if (formattedAddress.contains("null")) {
            formattedAddress.split("null")[0].replace("대한민국","")
        } else {
            formattedAddress.replace("대한민국", "")
        }
    }

    /** 현재 주소 DB에 업데이트 **/
    fun updateCurrentAddress(lat: Double, lng: Double, addr: String) {
        val roomDB = GpsRepository(context)
        sp.setString(lastAddress, addr)
        val model = GpsEntity()
        Log.d(TAG_D, roomDB.findAll().toString())
        model.name = CURRENT_GPS_ID
        model.lat = lat
        model.lng = lng
        model.addr = addr
        if (roomDB.findAll().isEmpty()) {
            roomDB.insert(model)
            Logger.t(TAG_D).d("Insert GPS In GetLocation")
        } else {
            roomDB.update(model)
            Logger.t(TAG_D).d("Update GPS In GetLocation")
        }
    }

    private fun renewTopic(old: String, new: String) {
        SubFCM().unSubTopic(old).subTopic(new)
        Thread.sleep(100)
        sp.setString("WEATHER_CURRENT", new)
    }

    @SuppressLint("MissingPermission")
    fun getGpsInBackground() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 위치 업데이트가 발생했을 때 실행되는 코드
                val latitude = location.latitude
                val longitude = location.longitude
                updateCurrentAddress(latitude,longitude,getAddress(latitude,longitude))
                writeLogCause(
                    email = sp.getString(userEmail),
                    isSuccess = "WorkManager Location",
                    log = "새로운 위치 : ${latitude},${longitude}"
                )
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                Log.d(TAG_D, "provider is changed : provider : $provider , status : $status")
            }

            override fun onProviderEnabled(provider: String) {
                Log.d(TAG_D, "provider is Enabled")
            }

            override fun onProviderDisabled(provider: String) {
                Log.d(TAG_D, "provider is Disabled")
            }
        }
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            100f,
            locationListener
        )
    }

    fun writeRdbLog(lat: Double, lng: Double, addr: String) {
        val email = sp.getString(userEmail)
        if (email != "") {
            writeLogCause(
                email = email,
                isSuccess = "Background Location",
                log = "$lat , $lng \t " +
                        addr
            )
        } else {
            writeLogNotLogin(
                "비로그인",
                GetDeviceInfo().androidID(context),
                isSuccess = "Background Location",
                log = "$lat , $lng \t " +
                        addr
            )
        }
    }

    fun isGPSConnection(): Boolean  {
        val lm = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        Log.i("Location Enable","위치정보 호출 여부 : ${lm.isProviderEnabled(LocationManager.GPS_PROVIDER)}")
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun isNetWorkConnection(): Boolean {
        val lm = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        Log.i("Location Enable","네트워크 호출 여부 : ${lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)}")
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun requestGPSEnable() {
        Toast.makeText(context, "핸드폰 GPS를 켜주세요", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
}

