package com.example.airsignal_app.db.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * @author : Lee Jae Young
 * @since : 2023-03-21 오후 1:35
 **/
class WeatherDataModel {
    /** GPS 의 위치에 따른 데이터 정보보**/
    @Entity
    data class GetData(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val timestamp: Long,
        val xAxis: Double,
        val yAxis: Double,
//        @SerializedName("pm25Grade1h") val pm2P5Grade1h: String,
//        @SerializedName("pm10Value24"),
//        @SerializedName("pm10Grade1h"),
//        @SerializedName("so2Value"),
//        @SerializedName("coGrade"),
//        @SerializedName("coValue"),
//        @SerializedName("khaiGrade"),
//        @SerializedName("khaiValue"),
//        @SerializedName("mangName"),
//        @SerializedName("no2Value"),
//        @SerializedName("o3Grade"),
//        @SerializedName("o3Value"),
//        @SerializedName("pm10Grade"),
//        @SerializedName("pm10Value"),
//        @SerializedName("pm25Grade"),
//        @SerializedName("pm25Value"),
//        @SerializedName("pm25Value24"),
//        @SerializedName("sidoName"),
//        @SerializedName("so2Grade"),
//        @SerializedName("stationName"),
        )
}