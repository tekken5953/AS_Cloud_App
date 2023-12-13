package app.core_databse.db.room.scheme

import androidx.room.*
import app.core_databse.db.room.model.GpsEntity

@Dao
interface GpsScheme {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGPS(gps: GpsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGPSWithCoroutine(gps: GpsEntity)

    @Update
    suspend fun updateCurrentGPS(model: GpsEntity)

    @Query("SELECT * FROM gps_table")
    fun findAll(): List<GpsEntity>

    @Query("SELECT * FROM gps_table ORDER BY name ASC")
    fun findAllWithAsc(): List<GpsEntity>

    @Query("SELECT * FROM gps_table ORDER BY name ASC")
    suspend fun findAllWithAscWithCoroutine(): List<GpsEntity>

    @Query("SELECT * FROM gps_table WHERE name= :name")
    fun findByName(name: String) : GpsEntity

    @Query("SELECT * FROM gps_table WHERE name= :name")
    suspend fun findByNameWithCoroutine(name: String) : GpsEntity

    @Query("DELETE FROM gps_table WHERE addrKr= :addrKr")
    fun deleteFromName(addrKr: String)

    @Query("DELETE FROM gps_table WHERE addrKr= :addrKr")
    suspend fun deleteFromAddrWithCoroutine(addrKr: String)

    @Query("DELETE FROM gps_table")
    fun clearDB()
}