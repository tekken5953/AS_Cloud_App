package com.example.airsignal_app.gps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.airsignal_app.util.ConvertDataType
import com.example.airsignal_app.util.ToastUtils
import com.google.android.gms.location.LocationServices
import com.orhanobut.logger.Logger
import java.io.IOException
import java.lang.Exception
import java.util.*

class GetLocation(mContext: Context, params: WorkerParameters) : GetLocationListener, CoroutineWorker(mContext,params) {
    private val context = mContext

    private val geocoder by lazy { Geocoder(mContext, Locale.KOREA) }

    /** GPS의 위치정보를 불러온 후 이전 좌표와의 거리를 계산합니다 **/
    @SuppressLint("MissingPermission")
    fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    onGetLocal(it)
                    val testLocal = Location("testPoint")
                    testLocal.apply {
                        latitude = 35.0
                        longitude = 120.0
                    }
                    Logger.t("Location").i("${ConvertDataType.millsToString(ConvertDataType.getCurrentTime(),"HH:mm")} - ${it.distanceTo(testLocal)}")
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                Logger.t("Location").e("Fail to Get Location")
            }
    }

    override fun onGetLocal(location: Location) {
        getAddress(location.latitude, location.longitude)
    }

    /** 현재 주소를 불러옵니다 **/
    private fun getAddress(lat: Double, lng: Double) : String {
        val nowAddress = "현재 위치를 확인 할 수 없습니다."
        lateinit var address: List<Address>
        try {
            address = geocoder.getFromLocation(lat, lng, 1)
            if (address != null && address.isNotEmpty()) {
                address.forEach {
                    Logger.t("Location").d("${it.latitude},${it.longitude}\n${it.getAddressLine(0)}")
                }
            }
        } catch (e: IOException) {
            ToastUtils(context as Activity).shortMessage("주소를 가져 올 수 없습니다.")
            e.printStackTrace()
        }

        return nowAddress
    }

    override suspend fun doWork(): Result {
        return try {
            Logger.t("Location").d("Start WorkManager")
            getLocation()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

