package com.example.airsignal_app.db.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey


class GpsModel {
    /**
     * 유저의 위치정보에 관한 데이터를 저장하는 테이블
     *
     * @property id
     * @property xAxis
     * @property yAxis
     * @property mills
     */
    @Entity
    data class GetGPS(@PrimaryKey(autoGenerate = true)val id: Int, val xAxis: String, val yAxis: String, val mills: Long)

    @Entity
    data class GetBookMark(@PrimaryKey(autoGenerate = true)val id: Int, val sn: String)


}