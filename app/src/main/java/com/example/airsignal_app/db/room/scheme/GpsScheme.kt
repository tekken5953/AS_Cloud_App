package com.example.airsignal_app.db.room.scheme

import androidx.room.*
import com.example.airsignal_app.db.room.model.GpsEntity

@Dao
interface GpsScheme {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewGPS(gps: GpsEntity)

    @Update
    fun updateCurrentGPS(gps: GpsEntity)

    @Query("SELECT * FROM GpsEntity")
    fun findAll(): List<GpsEntity>

    @Query("SELECT * FROM GpsEntity WHERE name= :name")
    fun findById(name: String) : GpsEntity

    @Query("DELETE FROM GpsEntity WHERE addrKr= :addrKr")
    fun deleteFromAddr(addrKr: String)

    @Query("SELECT * FROM GpsEntity WHERE addrKr= :addrKr")
    fun findByAddress(addrKr: String) : GpsEntity

    @Query("DELETE FROM GpsEntity")
    fun clearDB()
}