package com.example.airsignal_app.firebase.db

import android.content.Context
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.DataTypeParser.millsToString
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.example.airsignal_app.util.`object`.GetSystemInfo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * @author  Lee Jae Young
 * @since  2023-03-09 오후 5:44
 *
 * Firebase RTDB 에 로그를 저장하는 클래스
 */
object RDBLogcat {
    const val LOGIN_ON = "로그인"
    const val LOGIN_OFF = "비로그인"
    const val SIGN_OUT = "로그아웃"
    const val USER_PREF_SETUP = "설치"
    const val USER_PREF_DEVICE = "디바이스"
    const val USER_PREF_SETUP_INIT = "초기 설치"
    const val USER_PREF_SETUP_LAST_LOGIN = "마지막 로그인 시간"
    const val USER_PREF_SETUP_COUNT = "총 설치 횟수"
    const val USER_PREF_DEVICE_APP_VERSION = "앱 버전"
    const val USER_PREF_DEVICE_DEVICE_MODEL = "디바이스 모델"
    const val USER_PREF_DEVICE_SDK_VERSION = "SDK 버전"
    const val LOGIN_PREF = "정보"
    const val LOGIN_PREF_EMAIL = "이메일"
    const val LOGIN_PREF_PHONE = "핸드폰"
    const val LOGIN_PREF_NAME = "이름"
    const val LOGIN_PREF_PROFILE = "프로필 이미지"
    const val AUTO_LOGIN = "자동 로그인"
    const val OPTIONAL_LOGIN = "수동 로그인"
    const val SUCCESS_LOGIN = "로그인 성공"
    const val FAILED_LOGIN = "로그인 실패"
    const val GPS_HISTORY = "위치"
    const val GPS_SEARCHED = "검색된 주소"
    const val GPS_NOT_SEARCHED = "실시간 데이터"
    const val WIDGET_HISTORY = "위젯"
    const val WIDGET_INSTANCE = "인스턴스"
    const val WIDGET_ACTION = "액션"
    const val WIDGET_SCHEDULE = "스케쥴"
    const val WIDGET_ERROR = "위젯 에러"
    const val LOGIN_GOOGLE = "구글"
    const val LOGIN_KAKAO = "카카오"
    const val LOGIN_KAKAO_EMAIL = "카카오 이메일"
    const val LOGIN_NAVER = "네이버"
    const val NOTIFICATION_HISTORY = "알림"
    const val ERROR_HISTORY = "에러"
    const val ERROR_ANR = "ANR 에러"
    const val ERROR_LOCATION_IOException = "네트워크 에러 - IOException"
    const val ERROR_LOCATION_FAILED = "GPS 위치정보 갱신실패"

    /** 유저 로그 레퍼런스 **/
    private val db = Firebase.database
    private val ref = db.getReference("User")

    /** 날짜 변환 **/
    private fun getDate(): String {
        return millsToString(getCurrentTime(), "yyyy-MM-dd")
    }

    /** 시간 변환 **/
    private fun getTime(): String {
        return millsToString(getCurrentTime(), "HH:mm:ss")
    }

    /** 로그인 여부 확인 **/
    private fun isLogin(context: Context): String {
        return if (getUserEmail(context) != "") LOGIN_ON else LOGIN_OFF
    }

    /** 유니크 아이디 받아오기 - 로그인(이메일) 비로그인(디바이스아이디) **/
    private fun getAndroidIdForLog(context: Context): String {
        return if (getUserEmail(context) != "") {
            getUserEmail(context).replace(".","_")
        } else {
            GetSystemInfo.androidID(context)
        }
    }

    /** 아이디까지의 레퍼런스 경로 **/
    private fun default(context: Context): DatabaseReference {
        return ref.child(isLogin(context))
            .child(getAndroidIdForLog(context))
    }

    /** 유저 설치 정보 **/
    fun <T> writeUserPref(context: Context, sort: String, title: String, value: T?) {
        val userRef = default(context)
            .child(sort)
            .child(title)
        if (sort == USER_PREF_SETUP_INIT) {
            if (!userRef.get().isSuccessful) {
                userRef.setValue(value.toString())
            }
        } else if (sort == USER_PREF_SETUP_COUNT) {
            userRef.setValue(userRef.get().result.value.toString().toInt() + 1)
        } else {
            userRef.setValue(value.toString())
        }
    }

    /** 유저 로그인 정보 **/
    fun writeLoginPref(context: Context, platform: String,
        email: String, phone: String?, name: String?, profile: String?) {
        val prefRef = ref.child(LOGIN_ON)
            .child(getAndroidIdForLog(context))
            .child(platform)
            .child(LOGIN_PREF)

        prefRef.run {
            child(LOGIN_PREF_EMAIL).setValue(email)
            child(LOGIN_PREF_PHONE).setValue(phone)
            child(LOGIN_PREF_NAME).setValue(name)
            child(LOGIN_PREF_PROFILE).setValue(profile)
        }
    }

    /** 로그인 기록 **/
    fun writeLoginHistory(isLogin: Boolean, platform: String, email: String,
                          isAuto: Boolean?, isSuccess: Boolean) {
        val formedMail = email.replace(".","_")
        if (isLogin) {
            ref .child(LOGIN_ON)
                .child(formedMail)
                .child(platform)
                .child(LOGIN_ON)
                .child(if (isAuto!!) AUTO_LOGIN else OPTIONAL_LOGIN)
                .child(getDate()).child(getTime())
                .setValue(if (isSuccess) SUCCESS_LOGIN else FAILED_LOGIN)
        } else {
            ref .child(LOGIN_ON)
                .child(formedMail)
                .child(platform)
                .child(SIGN_OUT)
                .child(getDate()).child(getTime())
                .setValue(if(isSuccess) "성공" else "실패")
        }
    }

    /** 위치 정보 기록 **/
    fun writeGpsHistory(
        context: Context,
        isSearched: Boolean,
        gpsValue: String,
        responseData: String?
    ) {
        val gpsRef = default(context)
            .child(GPS_HISTORY)
            .child(getDate())
            .child(if (isSearched) GPS_SEARCHED else GPS_NOT_SEARCHED)
            .child(getTime())

        if (responseData != null) {
            gpsRef.setValue("$responseData")
        } else {
            gpsRef.setValue(gpsValue)
        }
    }

    /** 위젯 정보 **/
    fun writeWidgetPref(context: Context, sort: String, value: String) {
        default(context)
            .child(WIDGET_HISTORY)
            .child(getDate())
            .child(sort)
            .child(getTime())
            .setValue(value)
    }

    /** 위젯 호출 기록 **/
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

    /** 알림 기록 **/
    fun writeNotificationHistory(context: Context, topic: String, response: String?) {
        default(context)
            .child(NOTIFICATION_HISTORY)
            .child(getDate())
            .child(topic)
            .child(getTime())
            .setValue(response)
    }

    /** 에러 로그 - 비정상 종료 **/
    fun writeErrorANR(thread: String, msg: String) {
        ref.child(ERROR_HISTORY)
            .child(getDate())
            .child(ERROR_ANR)
            .child(thread)
            .child(getTime())
            .setValue(msg)
    }

    /** 에러 로그 - 일반 **/
    fun writeErrorNotANR(context: Context, sort: String, msg: String) {
        default(context)
            .child(ERROR_HISTORY)
            .child(getDate())
            .child(sort)
            .child(getTime())
            .setValue(msg)
    }
}