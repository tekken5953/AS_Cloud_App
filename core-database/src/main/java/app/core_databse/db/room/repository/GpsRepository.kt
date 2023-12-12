package app.core_databse.db.room.repository

import android.content.Context
import android.util.Log
import app.core_databse.db.database.GpsDataBase.Companion.getInstance
import app.core_databse.db.room.model.GpsEntity
import kotlinx.coroutines.*

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오후 5:34
 **/
class GpsRepository(private val context: Context) {

    suspend fun update(model: GpsEntity) {
        getInstance(context).gpsRepository().updateCurrentGPS(model)
        Log.i("testtest","update $model")
    }

    fun insert(model: GpsEntity) {
        getInstance(context).gpsRepository().insertGPS(model)
        Log.i("testtest","insert $model")
    }

    fun insertWithCoroutine(model: GpsEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().insertGPSWithCoroutine(model)
            Log.i("testtest","insert $model")
        }
    }

    fun findAll(): List<GpsEntity> {
        val result = getInstance(context).gpsRepository().findAll()
        val sb = StringBuilder()
        sb.append("find all \n")
        result.forEach {
            sb.append("$it").append('\n')
        }
        sb.append("----")
        Log.i("testtest",sb.toString())
        return result
    }

    suspend fun findAllWithCoroutine(): List<GpsEntity> = withContext(Dispatchers.IO) {
        val result = getInstance(context).gpsRepository().findAll()
        val sb = StringBuilder()
        sb.append("find all \n")
        result.forEach {
            sb.append("$it").append('\n')
        }
        sb.append("----")
        Log.i("testtest",sb.toString())
        return@withContext result
    }

    fun findByName(name: String): GpsEntity {
        val result = getInstance(context).gpsRepository().findByName(name)
        Log.d("testtest", "find By name $result")
        return result
    }

    suspend fun findByNameWithCoroutine(name: String): GpsEntity  = withContext(Dispatchers.IO) {
        val result = getInstance(context).gpsRepository().findByNameWithCoroutine(name)
        Log.d("testtest", "find By name $result")
        return@withContext result
    }

    fun deleteFromAddress(addr: String) {
       getInstance(context).gpsRepository().deleteFromName(addr)
    }

    fun deleteFromAddressWithCoroutine(addr: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getInstance(context).gpsRepository().deleteFromAddrWithCoroutine(addr)
            Log.w("testtest","delete column : $addr")
        }
    }
}
