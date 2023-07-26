package com.example.airsignal_app.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.scheme.GpsScheme
import timber.log.Timber

@Database(entities = [GpsEntity::class], version = 2)
abstract class GpsDataBase : RoomDatabase() {
    abstract fun gpsRepository(): GpsScheme

    companion object {
        private const val dbName = "room-gps"
        private var INSTANCE: GpsDataBase? = null

        fun getInstance(context: Context): GpsDataBase? {
            if (INSTANCE == null) {
                synchronized(GpsDataBase::class.java) {
//                    Timber.tag(TAG_D).d("DB 인스턴스 생성")
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        GpsDataBase::class.java, dbName
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