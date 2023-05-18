package com.example.airsignal_app.db.room.repository

import android.content.Context
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.room.AppDataBase
import com.example.airsignal_app.db.room.AppDataBase.Companion.getInstance
import com.example.airsignal_app.db.room.model.GpsEntity
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오후 5:34
 **/
class GpsRepository(private val context: Context) {

    private val instance: AppDataBase = AppDataBase.getInstance(context)!!

    fun update(model: GpsEntity) {
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            getInstance(context)!!.gpsRepository().updateCurrentGPS(model)
            Logger.t(TAG_D).d("Update Model")
        }
    }

    fun insert(model: GpsEntity) {
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            getInstance(context)!!.gpsRepository().insertNewGPS(model)
            Logger.t(TAG_D).d("insert Model")
        }
    }

    fun findAll(): List<GpsEntity> {
        Logger.t(TAG_D).d("findAll Model")
        return getInstance(context)!!.gpsRepository().findAll()
    }

    fun findById(name: String): GpsEntity {
        Logger.t(TAG_D).d("findById Model")
        return getInstance(context)!!.gpsRepository().findById(name)
    }

    fun deleteFromAddress(addr: String) {
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {

        }
        getInstance(context)!!.gpsRepository().deleteFromAddr(addr)
        Logger.t(TAG_D).d("deleteFromAddress Model")
    }

    fun findByAddress(addr: String): GpsEntity {
        Logger.t(TAG_D).d("findByAddr Model")
        return getInstance(context)!!.gpsRepository().findByAddress(addr)
    }

    fun clearDB() {
        Logger.t(TAG_D).d("ClearDB Model")
        getInstance(context)!!.gpsRepository().clearDB()
    }
}