package app.airsignal.weather.db.room.scheme

import androidx.room.*
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.db.room.model.EyeGroupEntity

@Dao
interface EyeGroupScheme {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupWithCoroutine(group: EyeGroupEntity)

    @Query("UPDATE eye_group_table SET name = :newValue WHERE name = :oldValue")
    suspend fun updateCurrentGroup(oldValue: String, newValue: String)

    @Query("SELECT * FROM eye_group_table")
    suspend fun findAll(): List<EyeGroupEntity>

    @Query("DELETE FROM eye_group_table WHERE name= :name")
    suspend fun deleteFromSerialWithCoroutine(name: String)

    @Query("SELECT * FROM eye_group_table WHERE name= :name")
    suspend fun findByCategoryName(name: String): EyeGroupEntity

    @Query("DELETE FROM eye_group_table")
    fun clearDB()
}