package com.example.airsignal_app.db.room.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime

/**
 * @author : Lee Jae Young
 * @since : 2023-07-12 오전 9:44
 **/
@Entity
data class AirQEntity(
    @PrimaryKey(autoGenerate = false) var name: String = "",
    var nameKR: String? = "",
    var value: String = "",
    var unit: String = "",
    var grade: Int = 4,
    var maxValue: Float,
    var timeStamp: Long
) {
    constructor() : this("","","","",4,0f, getCurrentTime())
}