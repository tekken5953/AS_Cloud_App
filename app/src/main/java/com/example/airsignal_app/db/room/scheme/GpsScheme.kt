package com.example.airsignal_app.db.room.scheme

import androidx.room.*
import com.example.airsignal_app.db.room.model.GpsEntity

@Dao
interface GpsScheme {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNewGPS(gps: GpsEntity)

    @Update
    fun updateCurrentGPS(gps: GpsEntity)

    @Query("SELECT * FROM GpsEntity")
    fun findAll(): List<GpsEntity>

    @Query("SELECT * FROM GpsEntity WHERE id = :itemIndex")
    fun findById(itemIndex: Int) : GpsEntity

    @Query("DELETE FROM GpsEntity WHERE addr = :addr")
    fun deleteFromAddr(addr: String)
}