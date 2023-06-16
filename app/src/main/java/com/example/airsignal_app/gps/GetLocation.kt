package com.example.airsignal_app.gps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.*
import android.location.LocationListener
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLogCause
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLogNotLogin
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.util.`object`.GetSystemInfo.androidID
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserLastAddr
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

    /** GPS 의 위치정보를 불러온 후 이전 좌표와의 거리를 계산합니다 **/
    @SuppressLint("MissingPermission")
    fun getLocationInBackground() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    updateCurrentAddress(it.latitude,it.longitude,getAddress(it.latitude, it.longitude)!!)
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
    fun getAddress(lat: Double, lng: Double): String? {
        lateinit var address: List<Address>
        try {
            val geocoder = Geocoder(context, GetSystemInfo.getLocale(context))
            @Suppress("DEPRECATION")
            address = geocoder.getFromLocation(lat, lng, 1) as List<Address>
            return if (address.isNotEmpty() && address[0].getAddressLine(0) != "null") {
                address[0].getAddressLine(0)
            } else { "Null Address" }
        } catch (e: IOException) {
            Toast.makeText(context, "주소를 가져오는 도중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            writeLogCause(
                getUserEmail(context),
                "Background Location Exception",
                "Error : ${e.localizedMessage}"
            )
            return context.getString(R.string.address)
        }
    }

    /** getAddressLine으로 불러온 주소 포멧**/
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
            formattedAddress.split("null")[0].replace(context.getString(R.string.korea),"")
        } else {
            formattedAddress.replace(context.getString(R.string.korea), "")
        }
    }

    /** 현재 주소 DB에 업데이트 **/
    fun updateCurrentAddress(lat: Double, lng: Double, addr: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val roomDB = GpsRepository(context)
            setUserLastAddr(context, addr)
            val model = GpsEntity()
            model.name = CURRENT_GPS_ID
            model.lat = lat
            model.lng = lng
            model.addr = addr
            model.timeStamp = getCurrentTime()
            if (roomDB.findAll().isEmpty()) {
                roomDB.insert(model)
                Logger.t(TAG_D).d("Insert GPS In GetLocation")
            } else {
                roomDB.update(model)
                Logger.t(TAG_D).d("Update GPS In GetLocation")
            }
        }
    }

//    /** 현재 위치 토픽 갱신 **/
//    private fun renewTopic(old: String, new: String) {
//        SubFCM().unSubTopic(old).subTopic(new)
//        Thread.sleep(100)
//        sp.setString("WEATHER_CURRENT", new)
//    }

    /** 백그라운드에서 위치 갱신 **/
    @SuppressLint("MissingPermission")
    fun getGpsInBackground() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 위치 업데이트가 발생했을 때 실행되는 코드
                val latitude = location.latitude
                val longitude = location.longitude
                updateCurrentAddress(latitude,longitude,getAddress(latitude,longitude)!!)
                writeLogCause(
                    email = getUserEmail(context),
                    isSuccess = "WorkManager Location",
                    log = "새로운 위치 : ${latitude},${longitude} : ${getAddress(latitude,longitude)}"
                )
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            500f,
            locationListener
        )
    }

    /** 파이어베이스 로그 커스텀 **/
    fun writeRdbCurrentLog(lat: Double?, lng: Double?, addr: String) {
        val email = getUserEmail(context)
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
                androidID(context),
                isSuccess = "Background Location",
                log = "$lat , $lng \t " +
                        addr
            )
        }
    }

    /** 파이어베이스 로그 커스텀 - 검색 **/
    fun writeRdbSearchLog(addr: String) {
        val email = getUserEmail(context)
        if (email != "") {
            writeLogCause(
                email = email,
                isSuccess = "Searched Location",
                log = addr
            )
        } else {
            writeLogNotLogin(
                "비로그인",
                androidID(context),
                isSuccess = "Searched Location",
                log = addr
            )
        }
    }

    /** 디바이스 GPS 센서에 접근이 가능한지 확인 **/
    fun isGPSConnected(): Boolean {
        val lm = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        Timber.tag("Location Enable")
            .i("위치정보 호출 여부 : " + lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /** 디바이스 네트워크에 접근이 가능한지 확인 **/
    fun isNetWorkConnected(): Boolean {
        val lm = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        Timber.tag("Location Enable")
            .i("네트워크 호출 여부 : " + lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /** 핸드폰 위치 서비스가 켜져있는지 확인 **/
    fun requestSystemGPSEnable() {
        Toast.makeText(context, "핸드폰 GPS가 켜져있는지 확인해주세요", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
}

