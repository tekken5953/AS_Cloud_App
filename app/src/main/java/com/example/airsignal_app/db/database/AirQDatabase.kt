package com.example.airsignal_app.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.airsignal_app.dao.IgnoredKeyFile.dbVersion
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.room.model.AirQEntity
import com.example.airsignal_app.db.room.scheme.AirQScheme
import timber.log.Timber

@Database(entities = [AirQEntity::class], version = dbVersion)
abstract class AirQDatabase : RoomDatabase() {
    abstract fun airQRepository(): AirQScheme

    companion object {
        private const val dbName = "room-air"
        private var INSTANCE: AirQDatabase? = null

        fun getInstance(context: Context): AirQDatabase? {
            if (INSTANCE == null) {
                synchronized(AirQDatabase::class.java) {
                    Timber.tag(TAG_D).d("DB 인스턴스 생성")
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AirQDatabase::class.java, dbName
                    )
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}