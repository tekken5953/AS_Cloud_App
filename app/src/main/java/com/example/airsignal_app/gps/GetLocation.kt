package com.example.airsignal_app.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.location.LocationListener
import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
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
import timber.log.Timber
import java.io.IOException
import java.util.*


class GetLocation(private val context: Context) {
    private val sp by lazy { SharedPreferenceManager(context) }


    /** GPS 의 위치정보를 불러온 후 이전 좌표와의 거리를 계산합니다 **/
    @SuppressLint("MissingPermission")
    fun getLocation() {
//        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
//        val location: Location? = lm!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//        location?.let {
//
//            getAddress(it.latitude, it.longitude)
//        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    getAddress(it.latitude, it.longitude)
                    Log.w(
                        "TAG_D",
                        "version:${VERSION.SDK_INT}, ${it.latitude},${it.longitude},accuracy:${it.accuracy}"
                    )
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
                val it = address[0]
                val newAddress = it.getAddressLine(0).replace("대한민국", "")
                try {
                    updateCurrentAddress(
                        it.latitude, it.longitude, newAddress
                    )

                    writeRdbLog(it.latitude,it.longitude,newAddress)
//                            renewTopic(sp.getString("WEATHER_CURRENT"), lastAddress)
                    return newAddress
                } catch (e: Exception) {
                    Timber.tag("Location").e("Location Contains null")
                }
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

    /** 현재 주소 DB에 업데이트 **/
    fun updateCurrentAddress(lat: Double, lng: Double, addr: String) {
        val roomDB = GpsRepository(context)
        sp.setString(lastAddress,addr)
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
                writeLogCause(
                    email = "Test Background",
                    isSuccess = GetDeviceInfo().androidID(context),
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
            15 * 60 * 1000,
            0f,
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
}

