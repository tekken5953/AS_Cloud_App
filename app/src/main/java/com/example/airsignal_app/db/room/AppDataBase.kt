package com.example.airsignal_app.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.airsignal_app.db.room.model.GpsModel
import com.example.airsignal_app.db.room.scheme.AppPrefRepository
import com.example.airsignal_app.db.room.scheme.GpsRepository
import com.example.airsignal_app.db.room.scheme.UserPrefRepository
import com.orhanobut.logger.Logger

@Database(entities = [GpsModel::class], version = 1, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {

    abstract fun gpsRepository(): GpsRepository
    abstract fun appPrefRepository(): AppPrefRepository
    abstract fun userPrefRepository(): UserPrefRepository
}