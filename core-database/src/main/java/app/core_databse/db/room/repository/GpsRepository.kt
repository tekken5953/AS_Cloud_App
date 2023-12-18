package app.core_databse.db.room.repository

import android.content.Context
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
        }
    }

    fun insert(model: GpsEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().insertGPSWithCoroutine(model)
        }
    }

    suspend fun findAll(): List<GpsEntity> = withContext(Dispatchers.IO) {
        val result = getInstance(context).gpsRepository().findAllWithAscWithCoroutine()
        val sb = StringBuilder()
        sb.append("find all \n")
        result.forEach {
            sb.append("$it").append('\n')
        }
        sb.append("----")
        return@withContext result
    }

    suspend fun findByName(name: String): GpsEntity  = withContext(Dispatchers.IO) {
        return@withContext getInstance(context).gpsRepository().findByNameWithCoroutine(name)
    }

    fun deleteFromAddress(addr: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().deleteFromAddrWithCoroutine(addr)
        }
    }
}
