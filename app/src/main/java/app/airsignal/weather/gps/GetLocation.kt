package app.airsignal.weather.gps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import app.airsignal.weather.R
import app.airsignal.weather.dao.ErrorCode.ERROR_GET_DATA
import app.airsignal.weather.dao.ErrorCode.ERROR_LOCATION_IOException
import app.airsignal.weather.dao.StaticDataObject.CURRENT_GPS_ID
import app.airsignal.weather.db.room.model.GpsEntity
import app.airsignal.weather.db.room.repository.GpsRepository
import app.airsignal.weather.firebase.db.RDBLogcat.writeErrorNotANR
import app.airsignal.weather.firebase.db.RDBLogcat.writeGpsHistory
import app.airsignal.weather.util.AddressFromRegex
import app.airsignal.weather.util.`object`.DataTypeParser.getCurrentTime
import app.airsignal.weather.util.`object`.GetSystemInfo
import app.airsignal.weather.util.`object`.SetAppInfo.setNotificationAddress
import app.airsignal.weather.util.`object`.SetAppInfo.setUserLastAddr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class GetLocation(private val context: Context) {

    /** 현재 주소를 불러옵니다 **/
    fun getAddress(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, GetSystemInfo.getLocale(context))
            @Suppress( "DEPRECATION")
            val address = geocoder.getFromLocation(lat, lng, 1) as List<Address>
            val fullAddr = address[0].getAddressLine(0)
            CoroutineScope(Dispatchers.IO).launch {
                val notiAddr = AddressFromRegex(fullAddr).getNotificationAddress()
                setNotificationAddress(context, notiAddr)
                setUserLastAddr(context, formattingFullAddress(fullAddr))
            }
            if (address.isNotEmpty() && address[0].getAddressLine(0) != "null") {
                address[0].getAddressLine(0)
            } else { "No Address" }
        } catch (e: Exception) {
            when(e) {
                is IOException -> {
                    writeErrorNotANR(context, sort = ERROR_LOCATION_IOException, msg = e.localizedMessage!!)
                    null
                }
                is IndexOutOfBoundsException -> {
                    writeErrorNotANR(context, sort = ERROR_GET_DATA, msg = e.localizedMessage!!)
                    null
                } else -> {
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

        return  if (formattedAddress.contains("null")) {
            formattedAddress.split("null")[0].replace(context.getString(R.string.korea),"")
        } else {
            formattedAddress.replace(context.getString(R.string.korea), "")
        }
    }

    /** 현재 주소 DB에 업데이트 **/
    fun updateCurrentAddress(lat: Double, lng: Double, addr: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val roomDB = GpsRepository(context)
            setUserLastAddr(context, addr)
            val model = GpsEntity()
            model.name = CURRENT_GPS_ID
            model.position = -1
            model.lat = lat
            model.lng = lng
            model.addrKr = addr
            model.timeStamp = getCurrentTime()
            if (roomDB.findAll().isEmpty()) {
                roomDB.insert(model)
            } else {
                roomDB.update(model)
            }
        }
    }

    /** 백그라운드에서 위치 갱신 **/
    @SuppressLint("MissingPermission")
    fun getGpsInBackground(mills: Long, distance: Float) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

            val locationListener: LocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    // 위치 업데이트가 발생했을 때 실행되는 코드
                    val latitude = location.latitude
                    val longitude = location.longitude
                    updateCurrentAddress(latitude,longitude,getAddress(latitude,longitude)!!)
                    writeGpsHistory(context, isSearched = false,
                        gpsValue = "WorkManager : ${latitude},${longitude} : ${getAddress(latitude,longitude)}",
                        responseData = null)
                }
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            if (Build.VERSION.SDK_INT >= 31) {
                if (!locationManager!!.hasProvider(Context.LOCATION_SERVICE)) {
                    locationListener.let { listener ->
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            mills,
                            distance,
                            listener
                        )
                    }
                }
            } else {
                locationListener.let { listener ->
                    locationManager!!.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        mills,
                        distance,
                        listener
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

