package app.airsignal.weather.db.sp

import android.content.Context
import org.json.JSONException
import org.json.JSONObject

/**
 * @author : Lee Jae Young
 * @since : 2023-03-07 오전 9:43
 **/

class SharedPreferenceManager(context: Context) {
    @Suppress("PrivatePropertyName") private val PREFERENCES_NAME = "rebuild_preference"
    @Suppress("PrivatePropertyName") private val DEFAULT_VALUE_STRING = ""
    @Suppress("PrivatePropertyName") private val DEFAULT_VALUE_BOOLEAN = false
    @Suppress("PrivatePropertyName") private val DEFAULT_VALUE_INT = -1
    @Suppress("PrivatePropertyName") private val DEFAULT_VALUE_LONG = -1L
    @Suppress("PrivatePropertyName") private val DEFAULT_VALUE_FLOAT = -1f

    private val prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    /**String 값 저장 **/
    fun setString(key: String, value: String) : SharedPreferenceManager {
        editor.putString(key, value)
        editor.apply()
        return this
    }

    /**Boolean 값 저장**/
    fun setBoolean(key: String, value: Boolean) : SharedPreferenceManager {
        editor.putBoolean(key, value)
        editor.apply()
        return this
    }

    /**Integer 값 저장**/
    fun setInt(key: String, value: Int) : SharedPreferenceManager {
        editor.putInt(key, value)
        editor.apply()
        return this
    }

    /**Long 값 저장**/
    fun setLong(key: String, value: Long) : SharedPreferenceManager {
        editor.putLong(key, value)
        editor.apply()
        return this
    }

    /**Float 값 저장**/
    fun setFloat(key: String, value: Float) : SharedPreferenceManager {
        editor.putFloat(key, value)
        editor.apply()
        return this
    }

    /**String 값 호출**/
    fun getString(key: String): String = prefs.getString(key, DEFAULT_VALUE_STRING) ?: ""

    /**Boolean 값 호출**/
    fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)


    /**Integer 값 호출**/
    fun getInt(key: String, defaultValue: Int): Int = prefs.getInt(key, defaultValue)

    /**Long 값 호출**/
    fun getLong(key: String): Long = prefs.getLong(key, DEFAULT_VALUE_LONG)

    /**Float 값 호출**/
    fun getFloat(key: String): Float = prefs.getFloat(key, DEFAULT_VALUE_FLOAT)

    /**키 값 삭제**/
    fun removeKey(key: String) {
        editor.remove(key)
        editor.apply()
    }

    /**모든 데이터 값 삭제**/
    fun clear() {
        editor.clear()
        editor.apply()
    }

    /** 키쌍 리스트 구하기**/
    fun getAllList(): Map<String, Any>? {
        val jsonObject = JSONObject()
        return kotlin.runCatching {
            for (i in 0 until prefs.all.size) {
                jsonObject.put(prefs.all.keys.toString(), prefs.all.values)
            }

            toMap(jsonObject)
        }.getOrNull()
    }

    /**키를 제이슨 형태로 변환**/
    @Throws(JSONException::class)
    private fun toMap(`object`: JSONObject): Map<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        val keysItr = `object`.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            map[key] = `object`[key]
        }
        return map
    }
}