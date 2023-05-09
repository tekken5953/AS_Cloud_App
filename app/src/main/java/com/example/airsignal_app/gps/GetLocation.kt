package com.example.airsignal_app.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.IgnoredKeyFile.userLocation
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.dao.StaticDataObject.TAG_L
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLogCause
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.util.ConvertDataType
import com.example.airsignal_app.util.ConvertDataType.getCurrentTime
import com.google.android.gms.location.LocationServices
import com.orhanobut.logger.Logger
import timber.log.Timber
import java.io.IOException
import java.util.*

class GetLocation(private val context: Context) : GetLocationListener {
    private val sp by lazy { SharedPreferenceManager(context)}

    /** GPS 의 위치정보를 불러온 후 이전 좌표와의 거리를 계산합니다 **/
    @SuppressLint("MissingPermission")
    fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    onGetLocal(it)
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
        val nowAddress = "현재 위치를 확인 할 수 없습니다"
        lateinit var address: List<Address>
        try {
            val geocoder = Geocoder(context, ConvertDataType.getLocale(context))
            @Suppress("DEPRECATION")
            address = geocoder.getFromLocation(lat, lng, 10) as List<Address>
            if (address.isNotEmpty()) {
                for (i: Int in 0 until (address.size)) {
                    val it = address[i]
                    if (it.locality != null && it.thoroughfare != null) {
                        Timber.tag("Location").w("${it.locality} ${it.thoroughfare}")
                        writeLogCause(
                            email = email,
                            isSuccess = "Background Location",
                            log = "${it.latitude} , ${it.longitude}\t " +
                                    "${it.locality} ${it.thoroughfare}"
                        )

                        updateCurrentAddress(
                            lat, lng,
                            "${it.locality} ${it.thoroughfare}", getCurrentTime()
                        )

//                        renewTopic(sp.getString("WEATHER_CURRENT"), lastAddress)
                        break
                    } else {
                        Timber.tag("Location").e("Address is Null : %s", it.getAddressLine(i))
                    }
                }
            } else {
                writeLogCause(
                    email,
                    "Background Location",
                    "Address is Empty : $nowAddress"
                )
            }
        } catch (e: IOException) {
            Timber.tag("Location").e("주소를 가져오는 도중 오류가 발생했습니다")
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

    private fun renewTopic(old: String, new: String) {
        SubFCM().unSubTopic(old).subTopic(new)
        Thread.sleep(100)
        sp.setString("WEATHER_CURRENT",new)
    }
}

