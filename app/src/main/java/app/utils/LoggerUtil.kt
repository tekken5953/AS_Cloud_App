package app.utils

import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

/**
 * @author : Lee Jae Young
 * @since : 2023-03-06 오후 5:10
 **/
class LoggerUtil {
    fun getInstance() : LoggerUtil {
        Logger.addLogAdapter(AndroidLogAdapter())
        return this
    }

//    /** 앱 키해시 불러오기 */
//    fun getKeyHash(context: Context) {
//        Timber.tag("TAG_LOGIN").d("key_hash : ${Utility.getKeyHash(context)}")
//    }

    fun d(tag: String, msg: String?) {
        Logger.t(tag).d(msg ?: "")
    }

    fun i(tag: String, msg: String?) {
        Logger.t(tag).i(msg ?: "")
    }

    fun w(tag: String, msg: String?) {
        Logger.t(tag).w(msg ?: "")
    }

    fun e(tag: String, msg: String?) {
        Logger.t(tag).e(msg ?: "")
    }
}