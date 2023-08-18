package app.airsignal.weather.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.airsignal.weather.db.room.model.GpsEntity
import app.airsignal.weather.db.room.scheme.GpsScheme

@Database(entities = [GpsEntity::class], version = 3)
abstract class GpsDataBase : RoomDatabase() {
    abstract fun gpsRepository(): GpsScheme

    companion object {
        private const val dbName = "room-gps"
        private var INSTANCE: GpsDataBase? = null

        fun getInstance(context: Context): GpsDataBase? {
            if (INSTANCE == null) {
                synchronized(GpsDataBase::class.java) {
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