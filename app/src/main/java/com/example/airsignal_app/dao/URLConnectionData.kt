package com.example.airsignal_app.dao

/**
 * @author : Lee Jae Young
 * @since : 2023-03-23 오전 11:32
 **/
object URLConnectionData {
    const val weatherApiURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"   // 공공데이터 날씨정보 엔드포인트
    const val airCondApiURL = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/"        // 공공데이터 공기질 정보 엔드포인트
    const val springServerURL = "http://192.168.0.177:8080/api/"                            // API 서버 엔드포인트
}