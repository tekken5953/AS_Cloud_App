package app.airsignal.weather.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kakao.sdk.common.util.Utility
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-03-06 오후 5:10
 **/
class LoggerUtil {
    fun getInstance() : LoggerUtil {
        Logger.addLogAdapter(AndroidLogAdapter())
        Timber.plant(Timber.DebugTree())
        return this
    }

    /** 앱 키해시 불러오기 */
    fun getKeyHash(context: Context) {
        Timber.tag("TAG_LOGIN").d("key_hash : ${Utility.getKeyHash(context)}")
    }

    /**
     * @param tag Generate Tag with Timber
     * @param json  Write JsonString for Parsing with PrettyPrinting
     */
    fun logJsonTimberDebug(tag: String, json: String) {
        Timber.tag(tag).d(
            GsonBuilder().setPrettyPrinting().create().toJson(
                Gson().fromJson(json, JsonObject::class.java)
            )
        )
    }

    fun logJsonTimberInfo(tag: String, json: String) {
        Timber.tag(tag).i(
            GsonBuilder().setPrettyPrinting().create().toJson(
                Gson().fromJson(json, JsonArray::class.java)
            )
        )
    }

    fun logJsonTimberWarning(tag: String, json: String) {
        Timber.tag(tag).w(
            GsonBuilder().setPrettyPrinting().create().toJson(
                Gson().fromJson(json, JsonArray::class.java)
            )
        )
    }

    fun logJsonTimberError(tag: String, json: String) {
        Timber.tag(tag).e(
            GsonBuilder().setPrettyPrinting().create().toJson(
                Gson().fromJson(json, JsonArray::class.java)
            )
        )
    }
}