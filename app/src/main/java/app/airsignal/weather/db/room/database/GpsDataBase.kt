package app.airsignal.weather.db.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.airsignal.weather.db.room.model.GpsEntity
import app.airsignal.weather.db.room.scheme.GpsScheme

@Database(entities = [GpsEntity::class], version = 6, exportSchema = false)
abstract class GpsDataBase : RoomDatabase() {
    abstract fun gpsRepository(): GpsScheme

    companion object {
        private const val dbName = "room-gps"
        private var INSTANCE: GpsDataBase? = null

        fun getInstance(context: Context): GpsDataBase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): GpsDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                GpsDataBase::class.java, dbName
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}