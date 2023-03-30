package com.example.airsignal_app.db.room.scheme

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.airsignal_app.db.room.model.PreferenceModel

/**
 * @author : Lee Jae Young
 * @since : 2023-03-29 오전 10:16
 **/
interface AppPrefRepository {
    @Query("SELECT * FROM PreferenceModel.ApplicationPreference")
    fun findAll(): PreferenceModel.ApplicationPreference

    @Query("SELECT * FROM PreferenceModel.ApplicationPreference WHERE id = id")
    fun findById(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserPref(pref: PreferenceModel.ApplicationPreference)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppPref(pref: PreferenceModel.ApplicationPreference)

    @Query("DELETE FROM PreferenceModel.ApplicationPreference WHERE id = id")
    suspend fun deleteFromId(id: Int)

    @Query("DELETE FROM PreferenceModel.ApplicationPreference")
    suspend fun deleteAll()
}