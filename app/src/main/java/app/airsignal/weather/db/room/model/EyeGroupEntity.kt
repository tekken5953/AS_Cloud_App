package app.airsignal.weather.db.room.model

import androidx.room.*
import app.airsignal.weather.as_eye.dao.EyeDataModel

@Entity(tableName = "eye_group_table")
data class EyeGroupEntity(
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "device") var device: MutableList<EyeDataModel.Device>
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}