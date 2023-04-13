package com.example.airsignal_app.db.room

import android.content.Context
import androidx.room.Room
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.scheme.GpsScheme
import com.orhanobut.logger.Logger

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오후 5:34
 **/
class GpsRepository(private val context: Context) {

    fun getInstance(): GpsScheme {
        Logger.t(TAG_D).d("Create DB Instance")
        val db = Room.databaseBuilder(context.applicationContext, AppDataBase::class.java, "room-gps")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

        return db.gpsRepository()
    }

    fun update(model: GpsEntity) {
        getInstance().updateCurrentGPS(model)
        Logger.t(TAG_D).d("Update Model")
    }

    fun insert(model: GpsEntity) {
        getInstance().insertNewGPS(model)
        Logger.t(TAG_D).d("insert Model")
    }

    fun findAll() : List<GpsEntity> {
        Logger.t(TAG_D).d("findAll Model")
        return getInstance().findAll()
    }

    fun findById(id: Int) : GpsEntity {
        Logger.t(TAG_D).d("findById Model")
        return getInstance().findById(id)
    }

    fun deleteFromAddress(addr: String) {
        getInstance().deleteFromAddr(addr)
        Logger.t(TAG_D).d("deleteFromAddress Model")
    }
}