package com.example.airsignal_app.db.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.airsignal_app.util.ConvertDataType


/**
 * 유저의 위치정보에 관한 데이터를 저장하는 테이블
 *
 * @property id
 * @property name
 * @property lat
 * @property lng
 * @property addr
 * @property timeStamp
 */
@Entity
data class GpsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = 0,
    var name: String = "",
    var lat: Double?,
    var lng: Double?,
    var addr: String?,
    var timeStamp: Long = ConvertDataType.getCurrentTime()
) {
    constructor() : this(null,"",null,null,null,ConvertDataType.getCurrentTime())
}