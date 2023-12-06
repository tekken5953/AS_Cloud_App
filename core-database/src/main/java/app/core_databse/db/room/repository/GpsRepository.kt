package app.core_databse.db.room.repository

import android.content.Context
import android.util.Log
import app.core_databse.db.database.GpsDataBase.Companion.getInstance
import app.core_databse.db.room.model.GpsEntity
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
            Log.i("testtest","update $model")
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
        Log.d("testtest", getInstance(context).gpsRepository().findByName(name).toString())
        return getInstance(context).gpsRepository().findByName(name)
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
