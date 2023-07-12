package com.example.airsignal_app.db.room.repository

import android.content.Context
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.database.AirQDatabase
import com.example.airsignal_app.db.room.model.AirQEntity
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author : Lee Jae Young
 * @since : 2023-07-12 오전 9:44
 **/
class AirQRepository(private val context: Context) {
    fun update(model: AirQEntity) {
        CoroutineScope(Dispatchers.Default).launch {
            AirQDatabase.getInstance(context)!!.airQRepository().updateCurrentAir(model)
            Logger.t(TAG_D).d("Update Model : $model")
        }
    }

    fun insert(model: AirQEntity) {
        CoroutineScope(Dispatchers.Default).launch {
            AirQDatabase.getInstance(context)!!.airQRepository().insertNewAir(model)
            Logger.t(TAG_D).d("insert Model : $model")
        }
    }

    fun findAll(): List<AirQEntity> {
        Logger.t(TAG_D).d("findAll Model")
        return AirQDatabase.getInstance(context)!!.airQRepository().findAll()
    }

    fun findByName(name: String): AirQEntity {
        Logger.t(TAG_D).d("findByName Model : $name")
        return AirQDatabase.getInstance(context)!!.airQRepository().findByName(name)
    }

    fun deleteFromName(name: String) {
        CoroutineScope(Dispatchers.Default).launch {
            AirQDatabase.getInstance(context)!!.airQRepository().deleteFromName(name)
        }
        Logger.t(TAG_D).d("deleteFromNames Model")
    }

    fun clearDB() {
        AirQDatabase.getInstance(context)!!.airQRepository().clearDB()
        Logger.t(TAG_D).d("ClearDB Model")
    }
}