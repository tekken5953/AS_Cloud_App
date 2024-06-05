package app.airsignal.weather.utils

import timber.log.Timber

object TimberUtil {
    fun getInstance() = Timber.plant(Timber.DebugTree())

    fun d(tag: String, msg: String?) = Timber.tag(tag).d(msg)

    fun i(tag: String, msg: String?) = Timber.tag(tag).i(msg)

    fun w(tag: String, msg: String?) = Timber.tag(tag).w(msg)

    fun e(tag: String, msg: String?) = Timber.tag(tag).e(msg)
}