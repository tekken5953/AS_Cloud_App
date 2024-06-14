package app.airsignal.weather.db.room.repository

import android.content.Context
import app.airsignal.weather.db.room.database.GpsDataBase.Companion.getInstance
import app.airsignal.weather.db.room.model.GpsEntity
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
            getInstance(context).gpsRepository().insertGPSWithCoroutine(model)
        }
    }

    suspend fun findAll(): List<GpsEntity> = withContext(Dispatchers.IO) {
        return@withContext getInstance(context).gpsRepository().findAll()
    }

    suspend fun findByName(name: String): GpsEntity? = withContext(Dispatchers.IO) {
        return@withContext getInstance(context).gpsRepository().findByNameWithCoroutine(name)
    }

    fun deleteFromAddress(addr: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().deleteFromAddrWithCoroutine(addr)
        }
    }
}