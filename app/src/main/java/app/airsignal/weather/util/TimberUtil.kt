package app.airsignal.weather.util

import timber.log.Timber

class TimberUtil {

    fun getInstance() : TimberUtil {
        Timber.plant(Timber.DebugTree())
        return this
    }

    fun d(tag: String, msg: String?) {
        Timber.tag(tag).d(msg)
    }

    fun i(tag: String, msg: String?) {
        Timber.tag(tag).i(msg)
    }

    fun w(tag: String, msg: String?) {
        Timber.tag(tag).w(msg)
    }

    fun e(tag: String, msg: String?) {
        Timber.tag(tag).e(msg)
    }
}