package app.airsignal.weather.db.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.airsignal.weather.db.room.Converters
import app.airsignal.weather.db.room.model.EyeGroupEntity
import app.airsignal.weather.db.room.scheme.EyeGroupScheme
import com.google.android.datatransport.runtime.dagger.Module
import com.google.android.datatransport.runtime.dagger.Provides

@Module
@Database(entities = [EyeGroupEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GroupDataBase : RoomDatabase() {
    abstract fun groupRepository(): EyeGroupScheme

    companion object {
        private const val dbName = "eye_group_table"
        private var INSTANCE: GroupDataBase? = null

        fun getGroupInstance(context: Context): GroupDataBase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        @Provides
        private fun buildDatabase(context: Context): GroupDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                GroupDataBase::class.java, dbName
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}