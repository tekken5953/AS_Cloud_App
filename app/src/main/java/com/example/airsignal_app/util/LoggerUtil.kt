package com.example.airsignal_app.util

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kakao.sdk.common.util.Utility
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import timber.log.Timber

/**
 * @user : USER
 * @autor : Lee Jae Young
 * @since : 2023-03-06 오후 5:10
 * @version : 1.0.0
 **/
class LoggerUtil {
    fun getInstance() {
        Logger.addLogAdapter(AndroidLogAdapter())
        Timber.plant(Timber.DebugTree())
    }

    /** 앱 키해시 불러오기 */
    fun getKeyHash(context: Context) {
        Timber.tag("TAG_LOGIN").d("keyhash : ${Utility.getKeyHash(context)}")
    }

    /**
     * @param tag Generate Tag with Timber
     * @param json  Write JsonString for Parsing with PrettyPrinting
     */
    fun logJsonTimberDebug(tag: String, json: String) {
        Timber.tag(tag).d(
            GsonBuilder().setPrettyPrinting().create().toJson(
                GsonBuilder().setPrettyPrinting().create().toJson(
                    json
                )
            )
        )
    }

    fun logJsonTimberInfo(tag: String, json: String) {
        Timber.tag(tag).i(
            GsonBuilder().setPrettyPrinting().create().toJson(
                JsonParser().parse(json)
            )
        )
    }

    fun logJsonTimberWarning(tag: String, json: String) {
        Timber.tag(tag).w(
            GsonBuilder().setPrettyPrinting().create().toJson(
                JsonParser().parse(json)
            )
        )
    }

    fun logJsonTimberError(tag: String, json: String) {
        Timber.tag(tag).e(
            GsonBuilder().setPrettyPrinting().create().toJson(
                JsonParser().parse(json)
            )
        )
    }
}