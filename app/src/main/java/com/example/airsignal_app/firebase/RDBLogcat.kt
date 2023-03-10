package com.example.airsignal_app.firebase

import android.annotation.SuppressLint
import android.app.Activity
import com.example.airsignal_app.util.SharedPreferenceManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient
import java.text.SimpleDateFormat
import java.util.*

/**
 * @user : USER
 * @autor : Lee Jae Young
 * @since : 2023-03-09 오후 5:44
 * @version : 1.0.0
 **/

/**
 * Firebase RTDB에 로그를 저장하는 용도
 *
 * @param ref
 * 가장 최상단 루트 디렉터리의 이름 ex) "Log"
 */
class RDBLogcat(ref: String) {
    private val db = Firebase.database
    private val myRef = db.getReference(ref)
    private val mRef = ref

    /**
     * @param tag error - 에러발생 로그 확인, debug - 성공이벤트발생 로그 확인
     * @param phone 핸드폰 번호
     * @param log 로그 내용
     */
    private fun writeLog(tag: String, phone: String, log: String) {
        myRef.child(phone)
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(tag)
            .push().setValue("${millsToString(getCurrentTime(), "HH:mm:ss")} $log")
    }

    private fun writeLogCause(isSuccess: String, log: String) {
        myRef.child(isSuccess)
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(millsToString(getCurrentTime(), "HH:mm:ss"))
            .push().setValue(log)
    }


    private fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    private fun millsToString(mills: Long, pattern: String): String {
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat(pattern, Locale.KOREA)
        return format.format(Date(mills))
    }

    /**
     * @param isSuccess 성공 or 실패
     * @param sort 로그인 종류
     * @param isAuto 자동 or 수동
     */
    fun sendLogInWithPhone(isSuccess: String, phone: String, sort: String, isAuto: String) {
        val firebaseRDB = RDBLogcat(mRef)
            firebaseRDB.writeLog(
                isSuccess,
                phone,
                "$sort $isAuto"
            )
    }

    /** 카카오 로그인 로그 저장 **/
    fun sendLogInWithPhoneForKakao(activity: Activity, isSuccess: String, sort: String, isAuto: String) {
        val firebaseRDB = RDBLogcat(mRef)
        UserApiClient.instance.me { user, _ ->
            val phone = user!!.kakaoAccount!!.phoneNumber.toString().replace("+82 ","0")
            SharedPreferenceManager(activity).setString("phone_number", phone)
            firebaseRDB.writeLog(
                isSuccess,
                phone,
                "$sort $isAuto"
            )
        }
    }

    /**
     * @param isSuccess 성공 or 실패
     * @param phoneNumber 핸드폰 번호
     * @param sort 로그인 종류
     */
    fun sendLogOutWithPhone(isSuccess: String, phoneNumber: String, sort: String) {
        val firebaseRDB = RDBLogcat(mRef)
        firebaseRDB.writeLog(
            isSuccess,
            phoneNumber,
            sort
        )
    }

    /** 실패 로그 전송 **/
    fun sendLogToFail(isSuccess: String, log: String) {
        val firebaseRDB = RDBLogcat(mRef)
        firebaseRDB.writeLogCause(
            isSuccess,
            log
        )
    }
}