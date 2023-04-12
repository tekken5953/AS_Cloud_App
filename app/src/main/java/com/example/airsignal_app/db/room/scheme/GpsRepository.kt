package com.example.airsignal_app.db.room.scheme

import androidx.room.*
import com.example.airsignal_app.db.room.model.GpsModel

@Dao
interface GpsRepository {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNewGPS(gps: GpsModel.GpsEntity)

    @Update
    fun updateGPS(gps: GpsModel.GpsEntity)

    @Query("SELECT * FROM GpsModel.GpsEntity")
    fun findAll(): List<GpsModel.GpsEntity>

    @Query("SELECT * FROM GpsModel.GpsEntity WHERE id = id")
    fun findById(id: Int)

    @Query("DELETE FROM GpsModel.GpsEntity WHERE addr = addr")
    fun deleteFromAddr(addr: String)
}