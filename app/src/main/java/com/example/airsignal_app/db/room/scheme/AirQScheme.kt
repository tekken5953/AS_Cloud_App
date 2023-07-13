package com.example.airsignal_app.db.room.scheme

import androidx.room.*
import com.example.airsignal_app.db.room.model.AirQEntity

/**
 * @author : Lee Jae Young
 * @since : 2023-07-12 오전 9:44
 **/
@Dao
interface AirQScheme {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewAir(gps: AirQEntity)

    @Update
    fun updateCurrentAir(gps: AirQEntity)

    @Query("SELECT * FROM AirQEntity")
    fun findAll(): List<AirQEntity>

    @Query("SELECT * FROM AirQEntity WHERE name= :name")
    fun findByName(name: String) : AirQEntity

    @Query("DELETE FROM AirQEntity WHERE name= :name")
    fun deleteFromName(name: String)

    @Query("DELETE FROM AirQEntity")
    fun clearDB()
}