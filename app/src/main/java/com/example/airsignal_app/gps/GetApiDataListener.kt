package com.example.airsignal_app.gps

/**
 * @author : Lee Jae Young
 * @since : 2023-04-07 오전 10:36
 **/
interface GetApiDataListener {
    fun onGetApiData(lat: Double, lng: Double)
}