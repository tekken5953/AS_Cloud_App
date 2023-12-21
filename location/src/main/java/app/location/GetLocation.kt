package app.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.Settings
import app.address.AddressFromRegex
import app.core_databse.db.room.model.GpsEntity
import app.core_databse.db.room.repository.GpsRepository
import app.core_databse.db.sp.GetSystemInfo
import app.core_databse.db.sp.SetAppInfo.setNotificationAddress
import app.core_databse.db.sp.SetAppInfo.setUserLastAddr
import app.core_databse.db.sp.SpDao.CURRENT_GPS_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class GetLocation(private val context: Context) {

    /** 현재 주소를 불러옵니다 **/
    fun getAddress(lat: Double, lng: Double): String {
        val appContext = context.applicationContext
        return try {
            val geocoder = Geocoder(appContext, GetSystemInfo.getLocale(appContext))
            @Suppress("DEPRECATION")
            val address = geocoder.getFromLocation(lat, lng, 1) as List<Address>
            val fullAddr = address[0].getAddressLine(0)
            CoroutineScope(Dispatchers.IO).launch {
                val notiAddr = AddressFromRegex(fullAddr).getNotificationAddress()
                setNotificationAddress(appContext, notiAddr)
                setUserLastAddr(appContext, fullAddr)
            }
            if (address.isNotEmpty() && address[0].getAddressLine(0) != "null") {
                address[0].getAddressLine(0)
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    // 비동기적으로 데이터베이스 작업을 처리하는 확장 함수
    fun updateDatabaseWithLocationData(
        mLat: Double,
        mLng: Double,
        mAddr: String?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = GpsRepository(context)
            val model = GpsEntity(
                name = CURRENT_GPS_ID,
                lat = mLat,
                lng = mLng,
                addrKr = mAddr,
                addrEn = mAddr
            )

            if (gpsDbIsEmpty(db)) db.insert(model)
            else db.update(model)
        }
    }

    // DB가 비어있는지 확인
    private suspend fun gpsDbIsEmpty(db: GpsRepository): Boolean {
        return db.findAll().isEmpty()
    }

    @SuppressLint("MissingPermission")
    fun getForegroundLocation(): Location? {
        try {
            val locationManager = context.applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager?
            val locationGPS =
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val locationNetwork =
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            return if (locationGPS != null && locationNetwork != null) {
                // 두 위치 중 더 정확한 위치를 반환
                if (locationGPS.accuracy > locationNetwork.accuracy) locationGPS else locationNetwork
            } else locationGPS
                ?: locationNetwork
        } catch (e: SecurityException) {
            e.printStackTrace()
            return null
        }
    }

    /** 디바이스 GPS 센서에 접근이 가능한지 확인 **/
    fun isGPSConnected(): Boolean {
        val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager
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
        val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

