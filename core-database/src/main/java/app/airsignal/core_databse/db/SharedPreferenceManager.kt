package app.airsignal.core_databse.db

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CloseableCoroutineDispatcher
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

    private val prefs: SharedPreferences =  context.getSharedPreferences(
    PREFERENCES_NAME,
    Context.MODE_PRIVATE
    )
    private val editor: SharedPreferences.Editor = prefs.edit()

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
    fun getString(key: String): String {
        return prefs.getString(
            key,
            DEFAULT_VALUE_STRING
        ) ?: ""
    }

    /**Boolean 값 호출**/
    fun getBoolean(key: String): Boolean {
        return prefs.getBoolean(
            key,
            DEFAULT_VALUE_BOOLEAN
        )
    }

    /**Integer 값 호출**/
    fun getInt(key: String): Int {
        return prefs.getInt(key, DEFAULT_VALUE_INT)
    }

    /**Long 값 호출**/
    fun getLong(key: String): Long {
        return prefs.getLong(
            key,
            DEFAULT_VALUE_LONG
        )
    }

    /**Float 값 호출**/
    fun getFloat(key: String): Float {
        return prefs.getFloat(
            key,
            DEFAULT_VALUE_FLOAT
        )
    }

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
        try {
            for (i in 0 until prefs.all.size) {
                jsonObject.put(prefs.all.keys.toString(), prefs.all.values)
            }
            return toMap(jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    /**키를 제이슨 형태로 변환**/
    @Throws(JSONException::class)
    fun toMap(`object`: JSONObject): Map<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        val keysItr = `object`.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            val value = `object`[key]
            map[key] = value
        }
        return map
    }
}