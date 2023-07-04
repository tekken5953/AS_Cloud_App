package com.example.airsignal_app.db.room.repository

import android.content.Context
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.room.AppDataBase.Companion.getInstance
import com.example.airsignal_app.db.room.model.GpsEntity
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오후 5:34
 **/
class GpsRepository(private val context: Context) {

    @OptIn(DelicateCoroutinesApi::class)
    fun update(model: GpsEntity) {
        GlobalScope.launch(Dispatchers.Default) {
            getInstance(context)!!.gpsRepository().updateCurrentGPS(model)
            Logger.t(TAG_D).d("Update Model : $model")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun insert(model: GpsEntity) {
        GlobalScope.launch(Dispatchers.Default) {
            getInstance(context)!!.gpsRepository().insertNewGPS(model)
            Logger.t(TAG_D).d("insert Model : $model")
        }
    }

    fun findAll(): List<GpsEntity> {
        Logger.t(TAG_D).d("findAll Model")
        return getInstance(context)!!.gpsRepository().findAll()
    }

    fun findById(name: String): GpsEntity {
        Logger.t(TAG_D).d("findById Model : $name")
        return getInstance(context)!!.gpsRepository().findById(name)
    }

    fun deleteFromAddress(addr: String) {
        CoroutineScope(Dispatchers.Default).launch {
            getInstance(context)!!.gpsRepository().deleteFromAddr(addr)
        }
        Logger.t(TAG_D).d("deleteFromAddress Model")
    }

    fun findByAddress(addr: String): GpsEntity {
        Logger.t(TAG_D).d("findByAddr Model")
        return getInstance(context)!!.gpsRepository().findByAddress(addr)
    }

    fun clearDB() {
        getInstance(context)!!.gpsRepository().clearDB()
        Logger.t(TAG_D).d("ClearDB Model")
    }
}