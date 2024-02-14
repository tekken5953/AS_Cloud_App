package app.airsignal.weather.db.room.repository

import android.content.Context
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import app.airsignal.weather.db.room.database.GroupDataBase.Companion.getGroupInstance
import app.airsignal.weather.db.room.model.EyeGroupEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EyeGroupRepository(private val context: Context) {

    fun update(model: EyeGroupEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            getGroupInstance(context).groupRepository().updateCurrentGroup(model)
        }
    }

    fun insert(model: EyeGroupEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            getGroupInstance(context).groupRepository().insertGroupWithCoroutine(model)
        }
    }

    suspend fun findAll(): List<EyeGroupEntity> = withContext(Dispatchers.IO) {
        return@withContext getGroupInstance(context).groupRepository().findAll()
    }

    suspend fun findByName(name: String): EyeGroupEntity = withContext(Dispatchers.IO) {
        return@withContext getGroupInstance(context).groupRepository().findByCategoryName(name)
    }

    fun deleteFromAddress(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getGroupInstance(context).groupRepository().deleteFromSerialWithCoroutine(name)
        }
    }
}
