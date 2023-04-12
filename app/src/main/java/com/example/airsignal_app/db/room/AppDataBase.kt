package com.example.airsignal_app.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.scheme.GpsScheme

@Database(entities = [GpsEntity::class], version = 4, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {

    abstract fun gpsRepository(): GpsScheme
}