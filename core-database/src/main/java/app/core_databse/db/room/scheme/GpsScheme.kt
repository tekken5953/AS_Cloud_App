package app.core_databse.db.room.scheme

import androidx.room.*
import app.core_databse.db.room.model.GpsEntity

@Dao
interface GpsScheme {
    @Insert(GpsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertNewGPS(gps: GpsEntity)

    @Update(GpsEntity::class)
    fun updateCurrentGPS(gps: GpsEntity)

    @Query("SELECT * FROM GpsEntity")
    fun findAll(): List<GpsEntity>

    @Query("SELECT * FROM GpsEntity WHERE name= :name")
    fun findByName(name: String) : GpsEntity

    @Query("DELETE FROM GpsEntity WHERE addrKr= :addrKr")
    fun deleteFromAddr(addrKr: String)

    @Query("SELECT * FROM GpsEntity WHERE addrKr= :addrKr")
    fun findByAddress(addrKr: String) : GpsEntity

    @Query("DELETE FROM GpsEntity")
    fun clearDB()
}