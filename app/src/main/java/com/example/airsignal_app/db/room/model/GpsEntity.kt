package com.example.airsignal_app.db.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * 유저의 위치정보에 관한 데이터를 저장하는 테이블
 *
 * @property id String
 * @property lat Double
 * @property lng Double
 * @property addr String
 * @property timeStamp Long
 */
@Entity
data class GpsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val lat: Double?,
    val lng: Double?,
    val addr: String?,
    val timeStamp: Long
)