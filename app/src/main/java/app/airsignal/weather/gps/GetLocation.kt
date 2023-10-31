package app.airsignal.weather.gps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import app.airsignal.weather.R
import app.airsignal.weather.dao.ErrorCode
import app.airsignal.weather.dao.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.dao.ErrorCode.ERROR_LOCATION_IOException
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.dao.StaticDataObject.CURRENT_GPS_ID
import app.airsignal.weather.db.room.model.GpsEntity
import app.airsignal.weather.db.room.repository.GpsRepository
import app.airsignal.weather.firebase.db.RDBLogcat.writeErrorNotANR
import app.airsignal.weather.firebase.db.RDBLogcat.writeGpsHistory
import app.airsignal.weather.util.AddressFromRegex
import app.airsignal.weather.util.`object`.DataTypeParser.getCurrentTime
import app.airsignal.weather.util.`object`.GetSystemInfo
import app.airsignal.weather.util.`object`.SetAppInfo
import app.airsignal.weather.util.`object`.SetAppInfo.setNotificationAddress
import app.airsignal.weather.util.`object`.SetAppInfo.setUserLastAddr
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.navercorp.nid.NaverIdLoginSDK.applicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.util.*


class GetLocation(private val context: Context) {

    /** 현재 주소를 불러옵니다 **/
    fun getAddress(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, GetSystemInfo.getLocale(context))

            @Suppress("DEPRECATION")
            val address = geocoder.getFromLocation(lat, lng, 1) as List<Address>
            val fullAddr = address[0].getAddressLine(0)
            CoroutineScope(Dispatchers.IO).launch {
                val notiAddr = AddressFromRegex(fullAddr).getNotificationAddress()
                setNotificationAddress(context, notiAddr)
                setUserLastAddr(context, formattingFullAddress(fullAddr))
            }
            if (address.isNotEmpty() && address[0].getAddressLine(0) != "null") {
                address[0].getAddressLine(0)
            } else {
                "No Address"
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    writeErrorNotANR(
                        context,
                        sort = ERROR_LOCATION_IOException,
                        msg = e.localizedMessage!!
                    )
                    null
                }
                is IndexOutOfBoundsException -> {
                    writeErrorNotANR(context, sort = ERROR_GET_DATA, msg = e.localizedMessage!!)
                    null
                }
                else -> {
                    writeErrorNotANR(context, sort = ERROR_GET_DATA, msg = e.localizedMessage!!)
                    null
                }
            }
        }
    }

    /** getAddressLine으로 불러온 주소 포멧**/
    private fun formattingFullAddress(fullAddr: String): String {
        val addressParts = fullAddr.split(" ").toTypedArray() // 공백을 기준으로 주소 요소 분리
        var formattedAddress = ""
        for (i in 0 until addressParts.size - 1) {
            formattedAddress += addressParts[i].trim { it <= ' ' } // 건물 주소를 제외한 나머지 요소 추출
            if (i < addressParts.size - 2) {
                formattedAddress += " " // 요소 사이에 공백 추가
            }
        }

        return if (formattedAddress.contains("null")) {
            formattedAddress.split("null")[0].replace(context.getString(R.string.korea), "")
        } else {
            formattedAddress.replace(context.getString(R.string.korea), "")
        }
    }

    /** 현재 주소 DB에 업데이트 **/
    private fun updateCurrentAddress(lat: Double, lng: Double, addr: String) {
        CoroutineScope(Dispatchers.IO).launch {
            SetAppInfo.setLastLat(context, lat)
            SetAppInfo.setLastLng(context, lng)

            val roomDB = GpsRepository(context)
            val model = GpsEntity(
                name = CURRENT_GPS_ID,
                position = -1,
                lat = lat,
                lng = lng,
                addrKr = addr,
                timeStamp = getCurrentTime()
            )

            setUserLastAddr(context, addr)

            if (roomDB.findAll().isEmpty()) roomDB.insert(model)
             else roomDB.update(model)
        }
    }

    /** 백그라운드에서 위치 갱신 **/
    @SuppressLint("MissingPermission")
    fun getGpsInBackground() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val onSuccess: (Location?) -> Unit = { location ->
            location?.let { loc ->
                val latitude = loc.latitude
                val longitude = loc.longitude

                updateCurrentAddress(latitude, longitude, getAddress(latitude, longitude) ?: "")
                writeGpsHistory(
                    context, isSearched = false,
                    gpsValue = "WorkManager : ${latitude},${longitude} : " +
                            "${getAddress(latitude, longitude)}",
                    responseData = null
                )
            }
        }

        val onFailure: (e: Exception) -> Unit = {
            it.printStackTrace()
            writeGpsHistory(context,false,"WorkManager GPS is Failed to call", it.localizedMessage)
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    /** 디바이스 GPS 센서에 접근이 가능한지 확인 **/
    fun isGPSConnected(): Boolean {
        val lm = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
//        Timber.tag("Location Enable")
//            .i("위치정보 호출 여부 :  ${lm.isProviderEnabled(LocationManager.GPS_PROVIDER)} ")
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /** 디바이스 네트워크에 접근이 가능한지 확인 **/
    @Suppress("DEPRECATION")
    fun isNetWorkConnected(): Boolean {
        val cm: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = cm.activeNetworkInfo
        return networkInfo?.isConnected ?: false
    }

    /** 핸드폰 위치 서비스가 켜져있는지 확인 **/
    fun requestSystemGPSEnable() {
//        Toast.makeText(context, "핸드폰 GPS가 켜져있는지 확인해주세요", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    /** 디바이스 네트워크 프로바이더 접근 가능한지 확인 **/
    fun isNetworkProviderConnected(): Boolean {
        val lm = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

