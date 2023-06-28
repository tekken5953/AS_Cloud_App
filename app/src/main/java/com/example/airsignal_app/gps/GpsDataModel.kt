package com.example.airsignal_app.gps

/**
 * @author : Lee Jae Young
 * @since : 2023-06-28 오전 10:08
 **/
data class GpsDataModel(
    val lat: Double?,
    val lng: Double?,
    val addr: String?,
    val isGPS: Boolean
)