package app.airsignal.weather.db.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nullable


/**
 * 유저의 위치정보에 관한 데이터를 저장하는 테이블
 *
 * @property name
 * @property lat
 * @property lng
 * @property addrKr
 * @property addrEn
 */
@Entity(tableName = "gps_table")
data class GpsEntity(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "lat") @Nullable var lat: Double?,
    @ColumnInfo(name = "lng") @Nullable var lng: Double?,
    @ColumnInfo(name = "addrEn", defaultValue = "") var addrEn: String?,
    @ColumnInfo(name = "addrKr", defaultValue = "") var addrKr: String?
)