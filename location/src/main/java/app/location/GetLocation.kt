package app.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.Settings
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import app.address.AddressFromRegex
import app.airsignal.regex_address.R
import app.core_databse.db.room.model.GpsEntity
import app.core_databse.db.room.repository.GpsRepository
import app.core_databse.db.sp.GetSystemInfo
import app.core_databse.db.sp.SetAppInfo.setNotificationAddress
import app.core_databse.db.sp.SetAppInfo.setUserLastAddr
import app.core_databse.db.sp.SpDao.CHECK_GPS_BACKGROUND
import app.core_databse.db.sp.SpDao.CURRENT_GPS_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class GetLocation(private val context: Context) {

    /** 현재 주소를 불러옵니다 **/
    fun getAddress(lat: Double, lng: Double): String {
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
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    /** getAddressLine 으로 불러온 주소 포멧**/
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

    // 비동기적으로 데이터베이스 작업을 처리하는 확장 함수
    fun updateDatabaseWithLocationData(
        mLat: Double,
        mLng: Double,
        mAddr: String?
    ) {
        val db = GpsRepository(context)
        val model = GpsEntity().apply {
            this.name = CURRENT_GPS_ID
            position = -1
            lat = mLat
            lng = mLng
            addrKr = mAddr
            addrEn = mAddr
            timeStamp = System.currentTimeMillis()
        }

        // Room DAO 를 사용하여 데이터베이스 업데이트 또는 삽입 수행
        CoroutineScope(Dispatchers.IO).launch {
            if (gpsDbIsEmpty(db)) db.insert(model)
            else db.update(model)
        }
    }

    // DB가 비어있는지 확인
    private suspend fun gpsDbIsEmpty(db: GpsRepository): Boolean {
        return db.findAll().isEmpty()
    }

    @SuppressLint("MissingPermission")
    fun getGpsInBackground() {
        val locationManager = context.applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager?
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 위치 업데이트가 발생했을 때 실행되는 코드
                val latitude = location.latitude
                val longitude = location.longitude
                updateDatabaseWithLocationData(latitude,longitude,
                    getAddress(latitude,longitude))
            }
            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000 * 60 * 30,
            500f,
            locationListener
        )
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

    fun createWorkManager() {
        val workManager = WorkManager.getInstance(context)
        val workRequest =
            PeriodicWorkRequest.Builder(GPSWorker::class.java, 30, TimeUnit.MINUTES)
                .build()

        workManager.enqueueUniquePeriodicWork(
            CHECK_GPS_BACKGROUND,
            ExistingPeriodicWorkPolicy.KEEP, workRequest
        )
    }
}

