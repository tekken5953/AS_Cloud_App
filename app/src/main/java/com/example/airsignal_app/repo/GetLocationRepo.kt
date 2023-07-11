package com.example.airsignal_app.repo


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.airsignal_app.R
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.gps.GpsDataModel
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.view.ToastUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author : Lee Jae Young
 * @since : 2023-06-28 오전 10:02
 **/
class GetLocationRepo : BaseRepository() {
    var _getLocationResult = MutableLiveData<GpsDataModel>()

    @SuppressLint("MissingPermission")
    fun loadDataResult(context: Context) {
        val locationClass = GetLocation(context)
        CoroutineScope(Dispatchers.Default).launch {
            if (locationClass.isGPSConnected()) {
                LocationServices.getFusedLocationProviderClient(context).run {
                    this.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            location?.let { loc ->
                                    val addr = locationClass.getAddress(loc.latitude, loc.longitude)
                                    _getLocationResult.postValue(
                                        GpsDataModel(
                                            loc.latitude, loc.longitude, addr, isGPS = true
                                        )
                                    )
                                    Logger.t("Timber").d(
                                        GpsDataModel(
                                            loc.latitude, loc.longitude, addr, isGPS = true
                                        )
                                    )
                                }
                            }
                        }
                        .addOnFailureListener {
                            RDBLogcat.writeLogCause(
                                GetAppInfo.getUserEmail(context),
                                "GPS 위치정보 갱신실패",
                                it.localizedMessage!!
                            )
                        }
            } else if (!locationClass.isGPSConnected() && locationClass.isNetWorkConnected()) {
                val lm =
                    context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
                val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                location?.let { loc ->
                    val addr = locationClass.getAddress(loc.latitude, loc.longitude)
                    _getLocationResult.postValue(
                        GpsDataModel(loc.latitude, loc.longitude, addr, isGPS = false)
                    )
                }
            } else {
                locationClass.requestSystemGPSEnable()
            }
        }
    }
}