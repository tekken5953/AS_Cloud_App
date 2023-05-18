package com.example.airsignal_app.gps

import android.location.Location

/**
 * @author : Lee Jae Young
 * @since : 2023-03-21 오후 3:50
 **/
interface GetLocationListener {

    /** Location 을 파라미터로 같는 인터페이스 메서드 입니다. **/
    fun onGetLocal()
}