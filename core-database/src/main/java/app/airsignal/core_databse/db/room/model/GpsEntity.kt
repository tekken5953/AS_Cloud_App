package app.airsignal.core_databse.db.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * 유저의 위치정보에 관한 데이터를 저장하는 테이블
 *
 * @property id
 * @property name
 * @property lat
 * @property lng
 * @property addrKr
 * @property timeStamp
 */
@Entity
data class GpsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = 0,
    var position: Int,
    var name: String = "",
    var addrEn: String? = "",
    var lat: Double?,
    var lng: Double?,
    var addrKr: String? = "",
    var timeStamp: Long = System.currentTimeMillis()
) {
    constructor() : this(null,-1,"", "",null,null,null, System.currentTimeMillis())
}