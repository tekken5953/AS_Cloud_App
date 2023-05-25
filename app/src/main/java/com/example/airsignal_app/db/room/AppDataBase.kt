package com.example.airsignal_app.db.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.scheme.GpsScheme
import com.orhanobut.logger.Logger

@Database(entities = [GpsEntity::class], version = 12)
abstract class AppDataBase : RoomDatabase() {
    abstract fun gpsRepository(): GpsScheme

    companion object {
        private const val dbName = "room-gps"
        private var INSTANCE: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase? {
            if (INSTANCE == null) {
                synchronized(AppDataBase::class.java) {
                    Log.d(TAG_D,"DB 인스턴스 생성")
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                    AppDataBase::class.java, dbName)
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}