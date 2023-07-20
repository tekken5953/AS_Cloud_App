package com.example.airsignal_app.firebase.db

import android.app.Activity
import android.content.Context
import com.example.airsignal_app.util.`object`.DataTypeParser.formatEmailToRDB
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.DataTypeParser.millsToString
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserEmail
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient

/**
 * @author  Lee Jae Young
 * @since  2023-03-09 오후 5:44
 *
 * Firebase RTDB 에 로그를 저장하는 클래스
 */
object RDBLogcat {
    const val LOGIN_ON = "로그인"
    const val LOGIN_OFF = "비로그인"
    const val USER_PREF_SETUP = "설치"
    const val USER_PREF_DEVICE = "디바이스"
    const val LOGIN_PREF = "정보"
    const val LOGIN_HISTORY = "로그인 시도"
    const val AUTO_LOGIN = "자동 로그인"
    const val OPTIONAL_LOGIN = "수동 로그인"
    const val SUCCESS_LOGIN = "로그인 성공"
    const val FAILED_LOGIN = "로그인 실패"
    const val GPS_HISTORY = "위치"
    const val GPS_SEARCHED = "검색된 주소"
    const val GPS_NOT_SEARCHED = "실시간 주소"
    const val WIDGET_HISTORY = "위젯"
    const val WIDGET_INSTANCE = "인스턴스"
    const val WIDGET_ACTION = "액션"
    const val WIDGET_SCHEDULE = "스케쥴"
    const val NOTIFICATION_HISTORY = "알림"
    const val ERROR_HISTORY = "에러"
    const val ERROR_ANR = "ANR 에러"

    private val db = Firebase.database
    private val ref = db.getReference("User")

    private fun getDate(): String {
        return millsToString(getCurrentTime(), "yyyy-MM-dd")
    }

    private fun getTime(): String {
        return millsToString(getCurrentTime(), "HH:mm:ss")
    }

    private fun isLogin(context: Context): String {
        return if (getUserEmail(context) != "") LOGIN_ON else LOGIN_OFF
    }

    private fun getAndroidIdForLog(context: Context): String {
        return if (getUserEmail(context) != "") {
            getUserEmail(context)
        } else {
            GetSystemInfo.androidID(context)
        }
    }

    private fun default(context: Context): DatabaseReference {
        return ref.child(isLogin(context))
            .child(getAndroidIdForLog(context))
    }

    fun <T> writeUserPref(context: Context, isInstall: String, sort: String, value: T) {
        default(context)
            .child(isInstall)
            .setValue("$sort - ${value.toString()}")
    }

    fun <T> writeLoginPref(context: Context, sort: String, value: T) {
        ref.child(LOGIN_ON)
            .child(getAndroidIdForLog(context))
            .child(getUserLoginPlatform(context))
            .child(LOGIN_PREF)
            .setValue("$sort - ${value.toString()}")
    }

    fun writeLoginHistory(context: Context, email: String?, isAuto: Boolean, isSuccess: Boolean) {
        ref.child(LOGIN_ON)
            .child(email ?: getUserLoginPlatform(context))
            .child(getUserLoginPlatform(context))
            .child(LOGIN_HISTORY)
            .child(if (isAuto) AUTO_LOGIN else OPTIONAL_LOGIN)
            .child(getDate()).child(getTime())
            .setValue(if (isSuccess) SUCCESS_LOGIN else FAILED_LOGIN)
    }

    fun writeGpsHistory(
        context: Context,
        isSearched: Boolean,
        gpsValue: String,
        responseData: String?
    ) {
        val gpsRef = default(context)
            .child(GPS_HISTORY)
            .child(getDate())
            .child(getTime())
            .child(if (isSearched) GPS_SEARCHED else GPS_NOT_SEARCHED)

        if (responseData != null) {
            gpsRef.child(gpsValue).setValue(responseData)
        } else {
            gpsRef.setValue(gpsValue)
        }
    }

    fun writeWidgetPref(context: Context, sort: String, value: String) {
        default(context)
            .child(WIDGET_HISTORY)
            .child(getDate())
            .child(sort)
            .child(getTime())
            .setValue(value)
    }

    fun writeWidgetHistory(context: Context, sort: String, address: String, response: String?) {
        val widgetPref = default(context)
            .child(WIDGET_HISTORY)
            .child(getDate())
            .child(sort)
            .child(GPS_HISTORY)
            .child(getTime())

        if (response != null) {
            widgetPref
                .child(address)
                .setValue(response)
        } else {
            widgetPref
                .setValue(address)
        }
    }

    fun writeNotificationHistory(context: Context, topic: String, response: String?) {
        default(context)
            .child(NOTIFICATION_HISTORY)
            .child(getDate())
            .child(topic)
            .child(getTime())
            .setValue(response)
    }

    fun writeErrorANR(thread: String, msg: String) {
        ref.child(ERROR_HISTORY)
            .child(getDate())
            .child(ERROR_ANR)
            .child(thread)
            .child(getTime())
            .setValue(msg)
    }

    fun writeErrorNotANR(context: Context, sort: String, msg: String) {
        default(context)
            .child(ERROR_HISTORY)
            .child(getDate())
            .child(sort)
            .child(getTime())
            .setValue(msg)
    }

    /** 카카오 로그인 로그 저장 **/
    fun sendLogInWithEmailForKakao(
        activity: Activity,
        isSuccess: String,
        sort: String,
        isAuto: String
    ) {
        UserApiClient.instance.me { user, _ ->
            val email = user!!.kakaoAccount!!.email.toString()
            setUserEmail(activity, email)
            writeLog(
                email,
                isSuccess,
                "$sort $isAuto"
            )
        }
    }





    private val myRef = db.getReference("Log")

    /**
     * @param tag error - 에러발생 로그 확인,  debug - 성공이벤트발생 로그 확인
     * @param email 이메일
     * @param log 로그 내용
     */
    private fun writeLog(email: String, tag: String, log: String) {
        myRef.child(formatEmailToRDB(email))
            .child(formatEmailToRDB(tag))
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(millsToString(getCurrentTime(), "HH:mm:ss"))
            .setValue(log)
    }

    /**
     * @param isSuccess 성공 or 실패
     * @param sort 로그인 종류
     * @param isAuto 자동 or 수동
     */
    fun sendLogInWithEmail(isSuccess: String, email: String, sort: String, isAuto: String) {
        writeLog(
            email,
            isSuccess,
            "$sort $isAuto"
        )
    }

    /**
     * 로그아웃 결과 저장
     *
     * TODO 네이밍 변경 필요
     * @param email 이메일
     * @param isSuccess 성공 or 실패
     * @param sort 로그인 종류
     */
    fun sendLogOutWithEmail(email: String, isSuccess: String, sort: String) {
        writeLog(
            email,
            isSuccess,
            sort
        )
    }

    /** 경로를 탐색할 수 없는 실패 로그 전송 **/
    fun sendLogToFail(email: String, isSuccess: String, log: String) {
        writeLogCause(
            email = email,
            isSuccess = isSuccess,
            log = log
        )
    }

    fun writeWidgetLog(email: String?, s1: String?, s2: String?) {
        myRef.child(formatEmailToRDB(email!!))
            .child(s1!!)
            .child(s2!!)
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(millsToString(getCurrentTime(), "HH:mm:ss"))
            .setValue(s2)
    }

    /** 경로를 탐색 가능한 실패로그 전송 **/
    fun writeLogCause(email: String?, isSuccess: String, log: String) {
        myRef.child(formatEmailToRDB(email!!))
            .child(isSuccess)
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(millsToString(getCurrentTime(), "HH:mm:ss"))
            .setValue(log)
    }

    fun writeLogNotLogin(email: String, androidId: String, isSuccess: String, log: String) {
        myRef.child(formatEmailToRDB(email))
            .child(androidId)
            .child(isSuccess)
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(millsToString(getCurrentTime(), "HH:mm:ss"))
            .setValue(log)
    }
}