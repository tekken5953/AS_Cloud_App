package app.core_databse.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.core_databse.db.room.model.GpsEntity
import app.core_databse.db.room.scheme.GpsScheme

@Database(entities = [GpsEntity::class], version = 5, exportSchema = false)
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