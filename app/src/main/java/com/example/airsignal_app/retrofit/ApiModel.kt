package com.example.airsignal_app.retrofit

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

class ApiModel {
//    // 회원가입 시 Body에 넣어서 POST 할 데이터 모델
//    data class Member(val userId: String, val phone: String, val password: String)
//    // 로그인 시 Body에 넣어서 POST 할 데이터 모델
//    data class Login(val userId: String, val password: String)
//    // 로그인 시 발행된 AccessToken Body로 Get할 데이터 모델
//    data class LoginToken(var access: String, var refresh: String)
//    // 장치추가 시 Body에 넣어서 POST 할 데이터 모델
//    data class Device(val device: String, val id: String, val deviceName: String, val businessType: String)
//    // 장치생성 리턴 텍스트
//    data class ReturnPost(val result: String)
//    // 유저정보 GET할 데이터 모델
//    data class GetMyInfo(val userId: String, val email: String, val name: String, val authority: String)
//    // 유저 비밀번호 변경 모델
//    data class PutMyPassword(val password: String)
//    // 유저 이메일 변경 모델
//    data class PutMyEmail(val email: String)
//    // 장치데이터 리스트 GET할 데이터 모델
//    data class GetData(@SerializedName("TEMPval") val tempValue: String,
//                       @SerializedName("HUMIDval") val humidValue: String,
//                       @SerializedName("PM2P5val") val pmValue: String,
//                       @SerializedName("CO2val") val co2Value: String,
//                       @SerializedName("COval") val coValue: String,
//                       @SerializedName("TVOCval")val tvocValue: String,
//                       @SerializedName("CAIval") val cqiValue: String,
//                       @SerializedName("Virusval") val virusValue: String,
//                       @SerializedName("date") val timeStamp: Long)
//
//    // 날씨정보 공공데이터 GET할 데이터 모델
//    data class GetWeather(val category: String, val obsrValue: String, val baseDate: String, val baseTime: String)
//    // 북마크 PUT 바디
//    data class PutBookMark(val starred: Boolean)
//    data class RefreshToken(val refresh: String)

    data class RealTimeData(
        @SerializedName("base")
        val base: String,
        @SerializedName("forecast")
        val forecast: String,
        @SerializedName("x")
        val gridX: Int,
        @SerializedName("y")
        val gridY: Int,
        @SerializedName("rainProbability")
        val rainP: Double,
        @SerializedName("rainType")
        val rainType: String,
        @SerializedName("humidity")
        val humid: Double,
        @SerializedName("snowHourly")
        val snow: Double,
        @SerializedName("sky")
        val sky: String,
        @SerializedName("temperature")
        val temp: Double,
        @SerializedName("windSpeedEW")
        val windSpeedEW: Double,
        @SerializedName("windSpeedSN")
        val windSpeedSN: Double,
        @SerializedName("wave")
        val wave: Double,
        @SerializedName("vector")
        val vector: String,
        @SerializedName("windSpeed")
        val windSpeed: Double,
        @SerializedName("thunder")
        val thunder: Double
    )

    data class WeeklyData(
        @SerializedName("rainDate")
        val rainDate: String,
        @SerializedName("rnSt1Am")
        val rnSt1Am: Double,
        @SerializedName("rnSt1Pm")
        val rnSt1Pm: Double,
        @SerializedName("rnSt2Am")
        val rnSt2Am: Double,
        @SerializedName("rnSt2Pm")
        val rnSt2Pm: Double,
        @SerializedName("rnSt3Am")
        val rnSt3Am: Double,
        @SerializedName("rnSt3Pm")
        val rnSt3Pm: Double,
        @SerializedName("rnSt4Am")
        val rnSt4Am: Double,
        @SerializedName("rnSt4Pm")
        val rnSt4Pm: Double,
        @SerializedName("rnSt5Am")
        val rnSt5Am: Double,
        @SerializedName("rnSt5Pm")
        val rnSt5Pm: Double,
        @SerializedName("rnSt6Am")
        val rnSt6Am: Double,
        @SerializedName("rnSt6Pm")
        val rnSt6Pm: Double,
        @SerializedName("rnSt7Am")
        val rnSt7Am: Double,
        @SerializedName("wf0Am")
        val wf0Am: String,
        @SerializedName("wf0Pm")
        val wf0Pm: String,
        @SerializedName("wf1Am")
        val wf1Am: String,
        @SerializedName("wf1Pm")
        val wf1Pm: String,
        @SerializedName("wf2Am")
        val wf2Am: String,
        @SerializedName("wf2Pm")
        val wf2Pm: String,
        @SerializedName("wf3Am")
        val wf3Am: String,
        @SerializedName("wf3Pm")
        val wf3Pm: String,
        @SerializedName("wf4Am")
        val wf4Am: String,
        @SerializedName("wf4Pm")
        val wf4Pm: String,
        @SerializedName("wf5Am")
        val wf5Am: String,
        @SerializedName("wf5Pm")
        val wf5Pm: String,
        @SerializedName("wf6Am")
        val wf6Am: String,
        @SerializedName("wf6Pm")
        val wf6Pm: String,
        @SerializedName("wf7Am")
        val wf7Am: String,
        @SerializedName("wf7Pm")
        val wf7Pm: String,
        @SerializedName("tempDate")
        val tempDate: String,
        @SerializedName("taMin0")
        val taMin0: Double,
        @SerializedName("taMax0")
        val taMax0: Double,
        @SerializedName("taMin1")
        val taMin1: Double,
        @SerializedName("taMax1")
        val taMax1: Double,
        @SerializedName("taMin2")
        val taMin2: Double,
        @SerializedName("taMax2")
        val taMax2: Double,
        @SerializedName("taMin3")
        val taMin3: Double,
        @SerializedName("taMax3")
        val taMax3: Double,
        @SerializedName("taMin4")
        val taMin4: Double,
        @SerializedName("taMax4")
        val taMax4: Double,
        @SerializedName("taMin5")
        val taMin5: Double,
        @SerializedName("taMax5")
        val taMax5: Double,
        @SerializedName("taMin6")
        val taMin6: Double,
        @SerializedName("taMax6")
        val taMax6: Double,
        @SerializedName("taMin7")
        val taMin7: Double,
        @SerializedName("taMax7")
        val taMax7: Double
    )

    data class AirQualityData(
        @SerializedName("pm25Grade1h")
        val pm25Grade1h: Int,
        @SerializedName("pm10Value24")
        val pm10Value24: Double,
        @SerializedName("pm10Grade1h")
        val pm10Grade1h: Int,
        @SerializedName("pm25Value24")
        val pm25Value24: Int,
        @SerializedName("dataTime")
        val dateTime: String,
        @SerializedName("so2Grade")
        val so2Grade: Int,
        @SerializedName("so2Value")
        val so2Value: Double,
        @SerializedName("coGrade")
        val coGrade: Int,
        @SerializedName("coValue")
        val coValue: Double,
        @SerializedName("khaiGrade")
        val khaiGrade: Int,
        @SerializedName("khaiValue")
        val khaiValue: Int,
        @SerializedName("mangName")
        val mangName: String,
        @SerializedName("no2Grade")
        val no2Grade: Int,
        @SerializedName("no2Value")
        val no2Value: Double,
        @SerializedName("o3Grade")
        val o3Grade: Int,
        @SerializedName("o3Value")
        val o3Value: Double,
        @SerializedName("pm10Grade")
        val pm10Grade: Int,
        @SerializedName("pm10Value")
        val pm10Value: Double,
        @SerializedName("pm25Grade")
        val pm25Grade: Int,
        @SerializedName("pm25Value")
        val pm25Value: Int,
        @SerializedName("sidoName")
        val sidoName: String,
        @SerializedName("stationName")
        val stationName: String,
        @SerializedName("stationDetail")
        val stationDetail: StationDetailInner
    )

    data class StationDetailInner(
        @SerializedName("item")
        val stationDetailItem: String,
        @SerializedName("year")
        val stationDetailYear: String,
        @SerializedName("addr")
        val stationDetailAddr: String
    )

    data class SunData(
        @SerializedName("locdate")
        val locdate: String,
        @SerializedName("moonrise")
        val moonrise: String,
        @SerializedName("moonset")
        val moonset: String,
        @SerializedName("sunrise")
        val sunrise: String,
        @SerializedName("sunset")
        val sunset: String,
    )

    data class SunTomorrow(
        @SerializedName("locdate")
        val locdate: String,
        @SerializedName("moonrise")
        val moonrise: String,
        @SerializedName("moonset")
        val moonset: String,
        @SerializedName("sunrise")
        val sunrise: String,
        @SerializedName("sunset")
        val sunset: String,
    )

    data class TodayTemp(
        @SerializedName("min")
        val min: Double,
        @SerializedName("max")
        val max: Double
    )

    data class YesterdayTemp(
        @SerializedName("temperature")
        val temp: Double
    )

    data class UV(
        @SerializedName("value")
        val value: Int,
        @SerializedName("falg")
        val grade: String
    )

    data class Current(
//        @SerializedName("base")
//        val baseTime: LocalDateTime,
//        @SerializedName("forecast")
//        val forecastTime: LocalDateTime,
        @SerializedName("rainType")
        val rainType: String,
        @SerializedName("rainHourly")
        val rainHourly: Double,
        @SerializedName("humidity")
        val humidity: Double,
        @SerializedName("temperature")
        val temperature: Double,
//        @SerializedName("windSpeedEW")
//        val windSpeedEW: Double,
//        @SerializedName("windSpeedSN")
//        val windSpeedSN: Double,
        @SerializedName("vector")
        val vector: String,
        @SerializedName("windSpeed")
        val windSpeed: Double,
    )

    data class GetEntireData(
        @SerializedName("realtime")
        val realtime: List<RealTimeData>,
        @SerializedName("week")
        val week: WeeklyData,
        @SerializedName("quality")
        val quality: AirQualityData,
        @SerializedName("sun")
        val sun: SunData,
        @SerializedName("sun2")
        val sun_tomorrow: SunTomorrow,
        @SerializedName("today")
        val today: TodayTemp,
        @SerializedName("yesterday")
        val yesterday: YesterdayTemp,
        @SerializedName("uv")
        val uv: UV,
        @SerializedName("current")
        val current: Current
    )
}