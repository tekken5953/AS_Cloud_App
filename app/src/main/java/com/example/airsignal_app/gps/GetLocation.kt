package com.example.airsignal_app.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.StaticDataObject
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLogCause
import com.example.airsignal_app.firebase.db.RDBLogcat.writeLogNotLogin
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.util.ConvertDataType
import com.example.airsignal_app.util.ConvertDataType.getCurrentTime
import com.example.airsignal_app.util.GetDeviceInfo
import com.google.android.gms.location.LocationServices
import com.orhanobut.logger.Logger
import timber.log.Timber
import java.io.IOException
import java.util.*


class GetLocation(private val context: Context) {
    private val sp by lazy { SharedPreferenceManager(context)}

    /** GPS 의 위치정보를 불러온 후 이전 좌표와의 거리를 계산합니다 **/
    @SuppressLint("MissingPermission")
    fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    getAddress(it.latitude,it.longitude)
                }
            }
            .addOnFailureListener {
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
                    try {
                        Timber.tag("Location").w("${it.locality} ${it.thoroughfare}")
                        if (!it.locality.contains("null") && !it.thoroughfare.contains("null")) {
                            if (email != "") {
                                writeLogCause(
                                    email = email,
                                    isSuccess = "Background Location",
                                    log = "${it.latitude} , ${it.longitude}\t " +
                                            "${it.locality} ${it.thoroughfare}"
                                )
                            } else {
                                writeLogNotLogin(
                                    "비로그인",
                                    GetDeviceInfo().androidID(context),
                                    isSuccess = "Background Location",
                                    log = "${it.latitude} , ${it.longitude}\t " +
                                            "${it.locality} ${it.thoroughfare}"
                                )
                            }

                            updateCurrentAddress(
                                lat, lng,
                                "${it.locality} ${it.thoroughfare}",
                                getCurrentTime()
                            )
//                            renewTopic(sp.getString("WEATHER_CURRENT"), lastAddress)
                        break
                        } else {
                            Timber.tag("Location").e("Address is Null : %s", it.getAddressLine(i))
                        }
                    } catch(e: java.lang.NullPointerException) {
                        Timber.tag("Location").e("Location Contains null")
                    }
                }
            } else {
                writeLogCause(
                    email,
                    "Background Location Empty",
                    "Address is Empty : $nowAddress"
                )
            }
        } catch (e: IOException) {
            Timber.tag("Location").e("주소를 가져오는 도중 오류가 발생했습니다")
            writeLogCause(
                email,
                "Background Location Exception",
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

