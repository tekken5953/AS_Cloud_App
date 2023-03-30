package com.example.airsignal_app.db.room.scheme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.airsignal_app.db.room.model.GpsModel

@Dao
interface GpsRepository {
    @Query("SELECT * FROM DBModel.GetGPS")
    fun findGps(): GpsModel

    @Query("SELECT * FROM DBModel.GetGPS WHERE `index`=`index`")
    fun findGpsByIndex(index: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGps(gps: GpsModel.GetGPS)

    @Query("DELETE FROM DBModel.GetGPS WHERE `index` = `index`")
    suspend fun deleteFromIndex(index: Int)

    @Query("DELETE FROM DBModel.GetGPS")
    suspend fun deleteAll()
}