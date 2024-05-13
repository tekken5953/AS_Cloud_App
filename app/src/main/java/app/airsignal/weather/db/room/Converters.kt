package app.airsignal.weather.db.room

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun listToJson(value: MutableList<String?>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): MutableList<String?> {
        return Gson().fromJson(value, Array<String?>::class.java).toMutableList()
    }
}