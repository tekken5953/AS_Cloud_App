package com.example.airsignal_app.db.room.repository

import android.content.Context
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.database.GpsDataBase.Companion.getInstance
import com.example.airsignal_app.db.room.model.GpsEntity
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오후 5:34
 **/
class GpsRepository(private val context: Context) {

    fun update(model: GpsEntity) {
        CoroutineScope(Dispatchers.Default).launch {
            getInstance(context)!!.gpsRepository().updateCurrentGPS(model)
            Logger.t(TAG_D).d("Update Model : $model")
        }
    }

    fun insert(model: GpsEntity) {
        CoroutineScope(Dispatchers.Default).launch {
            getInstance(context)!!.gpsRepository().insertNewGPS(model)
        }
    }

    fun findAll(): List<GpsEntity> {
        return getInstance(context)!!.gpsRepository().findAll()
    }

    fun findById(name: String): GpsEntity {
        return getInstance(context)!!.gpsRepository().findById(name)
    }

    fun deleteFromAddress(addr: String) {
        CoroutineScope(Dispatchers.Default).launch {
            getInstance(context)!!.gpsRepository().deleteFromAddr(addr)
        }
    }

    fun findByAddress(addr: String): GpsEntity {
        return getInstance(context)!!.gpsRepository().findByAddress(addr)
    }

    fun clearDB() {
        getInstance(context)!!.gpsRepository().clearDB()
    }
}