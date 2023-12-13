package app.core_databse.db.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * 유저의 위치정보에 관한 데이터를 저장하는 테이블
 *
 * @property position
 * @property name
 * @property lat
 * @property lng
 * @property addrKr
 * @property addrEn
 */
@Entity(tableName = "gps_table")
data class GpsEntity(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "lat") var lat: Double?,
    @ColumnInfo(name = "lng") var lng: Double?,
    @ColumnInfo(name = "addrEn", defaultValue = "") var addrEn: String?,
    @ColumnInfo(name = "addrKr", defaultValue = "") var addrKr: String?
)