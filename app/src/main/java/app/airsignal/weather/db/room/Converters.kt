package app.airsignal.weather.db.room

import androidx.room.TypeConverter
import app.airsignal.weather.as_eye.dao.EyeDataModel
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun listToJson(value: MutableList<EyeDataModel.Device>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): MutableList<EyeDataModel.Device> {
        return Gson().fromJson(value, Array<EyeDataModel.Device>::class.java).toMutableList()
    }
}