package app.airsignal.core_databse.db.room.repository

import android.content.Context
import app.airsignal.core_databse.db.database.GpsDataBase.Companion.getInstance
import app.airsignal.core_databse.db.room.model.GpsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오후 5:34
 **/
class GpsRepository(private val context: Context) {

    fun update(model: GpsEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().updateCurrentGPS(model)
        }
    }

    fun insert(model: GpsEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().insertNewGPS(model)
        }
    }

    suspend fun findAll(): List<GpsEntity> = withContext(Dispatchers.IO) {
        return@withContext getInstance(context).gpsRepository().findAll()
    }

    fun findById(name: String): GpsEntity {
        return getInstance(context).gpsRepository().findById(name)
    }

    fun deleteFromAddress(addr: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().deleteFromAddr(addr)
        }
    }

    suspend fun findByAddressAsync(addr: String): GpsEntity {
        return withContext(Dispatchers.IO) {
            return@withContext getInstance(context).gpsRepository().findByAddress(addr)
        }
    }

    fun clearDB() {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().clearDB()
        }
    }
}
