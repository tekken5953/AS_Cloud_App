package com.example.airsignal_app.gps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.dao.StaticDataObject.TAG_L
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.GpsRepository
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLogCause
import com.example.airsignal_app.util.ConvertDataType.getCurrentTime
import com.example.airsignal_app.util.ToastUtils
import com.google.android.gms.location.LocationServices
import com.orhanobut.logger.Logger
import java.io.IOException
import java.util.*

class GetLocation(private val context: Context) : GetLocationListener {
    private val geocoder by lazy { Geocoder(context, Locale.KOREA) }
    private val sp by lazy { SharedPreferenceManager(context) }

    /** GPS 의 위치정보를 불러온 후 이전 좌표와의 거리를 계산합니다 **/
    @SuppressLint("MissingPermission")
    fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    onGetLocal(it)
                    Logger.t(TAG_L).d("${it.latitude},${it.longitude}")
                    //TODO 백그라운드에서 토픽 교체

                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                Logger.t(TAG_L).e("Fail to Get Location")
            }
    }

    override fun onGetLocal(location: Location) {
        getAddress(location.latitude, location.longitude)
    }

    /** 현재 주소를 불러옵니다 **/
    private fun getAddress(lat: Double, lng: Double) {
        val email = sp.getString(userEmail)
        val nowAddress = "현재 위치를 확인 할 수 없습니다."
        lateinit var address: List<Address>
        try {
            @Suppress("DEPRECATION")
            address = geocoder.getFromLocation(lat, lng, 1) as List<Address>
            if (address.isNotEmpty()) {
                address.forEach {
                    writeLogCause(
                        email = email,
                        isSuccess = "Background Location",
                        log = "${it.latitude.toInt()} , ${it.longitude.toInt()}\t ${
                            it.getAddressLine(0)
                        }"
                    )

                    updateCurrentAddress(
                        lat, lng,
                        "${it.locality} ${it.thoroughfare}", getCurrentTime()
                    )
                }
            } else {
                writeLogCause(
                    email,
                    "Background Location",
                    "Address is Empty : $nowAddress"
                )
            }
        } catch (e: IOException) {
            ToastUtils(context as Activity).shortMessage("주소를 가져오는 도중 오류가 발생했습니다")
            writeLogCause(
                email,
                "Background Location",
                "Error : ${e.printStackTrace()}"
            )
        }
    }

    /** 현재 주소 DB에 업데이트 **/
    private fun updateCurrentAddress(lat: Double, lng: Double, addr: String, time: Long) {
        val roomDB = GpsRepository(context)
        val model = GpsEntity(CURRENT_GPS_ID, lat, lng, addr, time)
        try {
            if (roomDB.findById(CURRENT_GPS_ID).addr != null) {
                roomDB.update(model)
                Logger.t(TAG_D).d("Update GPS In GetLocation")
            }
        } catch (e: java.lang.NullPointerException) {
            roomDB.insert(model)
            Logger.t(TAG_D).d("Insert GPS In GetLocation")
        }
    }
}

