package com.example.airsignal_app.repo


import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.firebase.db.RDBLogcat.ERROR_LOCATION_FAILED
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.gps.GpsDataModel
import com.example.airsignal_app.util.AddressFromRegex
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.google.android.gms.common.api.Api
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.orhanobut.logger.Logger

/**
 * @author : Lee Jae Young
 * @since : 2023-06-28 오전 10:02
 **/
class GetLocationRepo : BaseRepository() {
    var _getLocationResult = MutableLiveData<ApiState<GpsDataModel>>()

    @SuppressLint("MissingPermission")
    fun loadDataResult(context: Context) {
        val locationClass = GetLocation(context)
        if (locationClass.isGPSConnected()) {
            LocationServices.getFusedLocationProviderClient(context).run {
                this.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        location?.let { loc ->
                            val addr = locationClass.getAddress(loc.latitude, loc.longitude)
                            _getLocationResult.postValue(
                                ApiState.Success(GpsDataModel(
                                    loc.latitude, loc.longitude, addr, isGPS = true
                                ))
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
                    RDBLogcat.writeErrorNotANR(context, sort = ERROR_LOCATION_FAILED,
                    msg =  it.localizedMessage!!)

                    _getLocationResult.postValue(ApiState.Error("Get Location Error"))
                }
        } else if (!locationClass.isGPSConnected() && locationClass.isNetWorkConnected()) {
            val lm =
                context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location?.let { loc ->
                val addr = locationClass.getAddress(loc.latitude, loc.longitude)

                _getLocationResult.postValue(
                    ApiState.Success(
                    GpsDataModel(loc.latitude, loc.longitude, addr, isGPS = false)
                ))
            }
        } else {
            _getLocationResult.postValue(ApiState.Error("GPS Connect Error"))
            locationClass.requestSystemGPSEnable()
        }
    }
}