package com.example.airsignal_app.firebase

import android.app.Activity
import com.example.airsignal_app.util.IgnoredKeyFile.lastLoginPhone
import com.example.airsignal_app.util.ConvertDataType.getCurrentTime
import com.example.airsignal_app.util.ConvertDataType.millsToString
import com.example.airsignal_app.util.SharedPreferenceManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient

/**
 * @author  Lee Jae Young
 * @since  2023-03-09 오후 5:44
 *
 * Firebase RTDB에 로그를 저장하는 클래스
 */
class RDBLogcat(ref: String) {
    private val db = Firebase.database
    private val myRef = db.getReference(ref)

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

    /**
     * @param isSuccess 성공 or 실패
     * @param sort 로그인 종류
     * @param isAuto 자동 or 수동
     */
    fun sendLogInWithPhone(isSuccess: String, phone: String, sort: String, isAuto: String) {
            writeLog(
                isSuccess,
                phone,
                "$sort $isAuto"
            )
    }

    /** 카카오 로그인 로그 저장 **/
    fun sendLogInWithPhoneForKakao(activity: Activity, isSuccess: String, sort: String, isAuto: String) {
        UserApiClient.instance.me { user, _ ->
            val phone = user!!.kakaoAccount!!.phoneNumber.toString().replace("+82 ","0")
            SharedPreferenceManager(activity).setString(lastLoginPhone, phone)
            writeLog(
                isSuccess,
                phone,
                "$sort $isAuto"
            )
        }
    }

    /**
     * 로그아웃 결과 저장
     *
     * TODO 네이밍 변경 필요
     * @param isSuccess 성공 or 실패
     * @param phoneNumber 핸드폰 번호
     * @param sort 로그인 종류
     */
    fun sendLogOutWithPhone(isSuccess: String, phoneNumber: String, sort: String) {
        writeLog(
            isSuccess,
            phoneNumber,
            sort
        )
    }

    /** 경로를 탐색할 수 없는 실패 로그 전송 **/
    fun sendLogToFail(isSuccess: String, log: String) {
        writeLogCause(
            isSuccess,
            log
        )
    }

    /** 경로를 탐색 가능한 실패로그 전송 **/
    private fun writeLogCause(isSuccess: String, log: String) {
        myRef.child(isSuccess)
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(millsToString(getCurrentTime(), "HH:mm:ss"))
            .push().setValue(log)
    }
}