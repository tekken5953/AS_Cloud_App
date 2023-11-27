package app.airsignal.weather.firebase.db

import android.content.Context
import app.airsignal.weather.util.`object`.DataTypeParser.getCurrentTime
import app.airsignal.weather.util.`object`.DataTypeParser.millsToString
import app.airsignal.weather.util.`object`.GetAppInfo.getUserEmail
import app.airsignal.weather.util.`object`.GetSystemInfo
import app.airsignal.weather.util.`object`.GetSystemInfo.androidID
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * @author  Lee Jae Young
 * @since  2023-03-09 오후 5:44
 *
 * Firebase RTDB 에 로그를 저장하는 클래스
 */
object RDBLogcat {
    private const val LOGIN_ON = "로그인"
    private const val LOGIN_OFF = "비로그인"
    private const val SIGN_OUT = "로그아웃"
    const val USER_PREF_SETUP = "설치"
    const val USER_PREF_DEVICE = "디바이스"
    const val USER_PREF_SETUP_INIT = "초기 설치"
    const val USER_PREF_SETUP_LAST_LOGIN = "마지막 접속 시간"
    private const val USER_PREF_SETUP_COUNT = "총 설치 횟수"
    const val USER_PREF_DEVICE_APP_VERSION = "앱 버전"
    const val USER_PREF_DEVICE_DEVICE_MODEL = "디바이스 모델"
    const val USER_PREF_DEVICE_SDK_VERSION = "SDK 버전"
    private const val LOGIN_PREF = "정보"
    private const val LOGIN_PREF_EMAIL = "이메일"
    private const val LOGIN_PREF_PHONE = "핸드폰"
    private const val LOGIN_PREF_NAME = "이름"
    private const val LOGIN_PREF_DEVICE_ID = "Android ID"
    private const val LOGIN_PREF_PROFILE = "프로필 이미지"
    private const val AUTO_LOGIN = "자동 로그인"
    private const val OPTIONAL_LOGIN = "수동 로그인"
    private const val SUCCESS_LOGIN = "로그인 성공"
    private const val FAILED_LOGIN = "로그인 실패"
    private const val GPS_HISTORY = "위치"
    private const val GPS_SEARCHED = "검색된 주소"
    private const val GPS_NOT_SEARCHED = "실시간 데이터"
    private const val WIDGET_HISTORY = "위젯"
    const val LOGIN_GOOGLE = "구글"
    const val LOGIN_KAKAO = "카카오"
    const val LOGIN_PHONE = "phone"
    const val LOGIN_KAKAO_EMAIL = "카카오 이메일"
    const val LOGIN_NAVER = "네이버"
    private const val NOTIFICATION_HISTORY = "알림"
    private const val ERROR_HISTORY = "에러"
    private const val ERROR_ANR = "ANR 에러"
    const val LOGIN_FAILED = "로그인 시도 실패"
    const val DATA_CALL_ERROR = "데이터 호출 실패"

    /** 유저 로그 레퍼런스 **/
    private val db = FirebaseDatabase.getInstance()
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
        return try {
            if (getUserEmail(context) != "") LOGIN_ON else LOGIN_OFF
        } catch(e: java.lang.NullPointerException) { LOGIN_OFF }
    }

    /** 유니크 아이디 받아오기 - 로그인(이메일) 비로그인(디바이스아이디) **/
    private fun getAndroidIdForLog(context: Context): String {
        return try {
            if (getUserEmail(context) != "")
                getUserEmail(context).replace(".","_")
            else GetSystemInfo.androidID(context)
        } catch (e: NullPointerException) { "" }
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
            if (!userRef.get().isSuccessful) userRef.setValue(value.toString())
        } else if (sort == USER_PREF_SETUP_COUNT)
            userRef.setValue(userRef.get().result.value.toString().toInt() + 1)
         else userRef.setValue(value.toString())

    }

    /** 유저 로그인 정보 **/
    fun writeLoginPref(context: Context, platform: String,
        email: String, phone: String?, name: String?, profile: String?) {
        val formEmail = email.replace(".","_")
        val prefRef = ref.child(LOGIN_ON)
            .child(getAndroidIdForLog(context))
            .child(LOGIN_ON)
            .child(platform)
            .child(LOGIN_PREF)

        prefRef.run {
            child(LOGIN_PREF_EMAIL).setValue(formEmail)
            child(LOGIN_PREF_PHONE).setValue(phone)
            child(LOGIN_PREF_NAME).setValue(name)
            child(LOGIN_PREF_PROFILE).setValue(profile)
            child(LOGIN_PREF_DEVICE_ID).setValue(androidID(context))
        }
    }

    /** 로그인 기록 **/
    fun writeLoginHistory(isLogin: Boolean, platform: String, email: String,
                          isAuto: Boolean?, isSuccess: Boolean) {
        val formedMail = email.replace(".","_")
        if (isLogin) {
            ref .child(LOGIN_ON)
                .child(formedMail)
                .child(LOGIN_ON)
                .child(platform)
                .child(if (isAuto!!) AUTO_LOGIN else OPTIONAL_LOGIN)
                .child(getDate()).child(getTime())
                .setValue(if (isSuccess) SUCCESS_LOGIN else FAILED_LOGIN)
        } else {
            ref .child(LOGIN_ON)
                .child(formedMail)
                .child(SIGN_OUT)
                .child(platform)
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
        try {
            val gpsRef = default(context)
                .child(GPS_HISTORY)
                .child(getDate())
                .child(if (isSearched) GPS_SEARCHED else GPS_NOT_SEARCHED)
                .child(getTime())
            if (responseData != null) gpsRef.setValue("$responseData")
            else gpsRef.setValue(gpsValue)
        } catch (e: DatabaseException) { e.printStackTrace() }
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
    fun writeWidgetHistory(context: Context, address: String, response: String?) {
        val widgetPref = default(context)
            .child(WIDGET_HISTORY)
            .child(getDate())
        widgetPref
            .child(address)
            .child(getTime())
            .setValue(response ?: "")
    }

    /** 알림 기록 **/
    fun writeNotificationHistory(context: Context, topic: String, response: String?) {
        try {
            default(context)
                .child(NOTIFICATION_HISTORY)
                .child(getDate())
                .child(topic)
                .child(getTime())
                .setValue(response)
        } catch (e: DatabaseException) {
            e.printStackTrace()
        }
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

    /** Admob 로드 에러 **/
    fun writeAdError(code: String,errorMsg: String) {
        ref.child("admob")
            .child("Fail to Load")
            .child(code)
            .setValue(errorMsg)
    }
}