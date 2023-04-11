package com.example.airsignal_app.db.room

import android.content.Context
import androidx.room.Room
import com.example.airsignal_app.db.room.model.GpsModel
import com.example.airsignal_app.db.room.scheme.GpsRepository

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오후 5:34
 **/
class SchemeGPS(private val context: Context) {
    private fun getInstance(): GpsRepository {
        val db = Room.databaseBuilder(context.applicationContext, AppDataBase::class.java, "db-gps")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

        return db.gpsRepository()
    }

    fun update(model: GpsModel.GpsEntity) : SchemeGPS{
        getInstance().updateGPS(model)
        return this
    }

    fun insert(model: GpsModel.GpsEntity) : SchemeGPS {
        getInstance().insertNewGPS(model)
        return this
    }

    fun findAll() : SchemeGPS {
        getInstance().findAll()
        return this
    }

    fun findById(id: Int) : SchemeGPS {
        getInstance().findById(id)
        return this
    }

    fun deleteFromAddress(addr: String) : SchemeGPS {
        getInstance().deleteFromAddr(addr)
        return this
    }
}