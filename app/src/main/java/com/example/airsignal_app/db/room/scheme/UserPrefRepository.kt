package com.example.airsignal_app.db.room.scheme

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.airsignal_app.db.room.model.PreferenceModel

/**
 * @author : Lee Jae Young
 * @since : 2023-03-29 오전 10:16
 **/
interface UserPrefRepository {
    @Query("SELECT * FROM PreferenceModel.UserPreference")
    fun findAll(): PreferenceModel.UserPreference

    @Query("SELECT * FROM PreferenceModel.UserPreference WHERE userId = userId")
    fun findById(id: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserPref(pref: PreferenceModel.UserPreference)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppPref(pref: PreferenceModel.ApplicationPreference)

    @Query("DELETE FROM PreferenceModel.UserPreference WHERE userId = userId")
    suspend fun deleteFromId(id: String)

    @Query("DELETE FROM PreferenceModel.UserPreference")
    suspend fun deleteAll()
}