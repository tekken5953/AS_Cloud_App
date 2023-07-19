package com.example.airsignal_app.firebase.db

import android.app.Activity
import com.example.airsignal_app.util.`object`.DataTypeParser.formatEmailToRDB
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.DataTypeParser.millsToString
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserEmail
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
        private val db = Firebase.database
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

        /** 카카오 로그인 로그 저장 **/
        fun sendLogInWithEmailForKakao(activity: Activity, isSuccess: String, sort: String, isAuto: String) {
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

    fun writeWidgetLog(email: String?,s1: String?, s2: String?) {
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

    fun writeLogNotLogin(email: String,androidId: String, isSuccess: String, log: String) {
        myRef.child(formatEmailToRDB(email))
            .child(androidId)
            .child(isSuccess)
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(millsToString(getCurrentTime(), "HH:mm:ss"))
            .setValue(log)
    }

    fun writeBadRequest(sort: String, log: String) {
        myRef.child("Retrofit Error")
            .child(sort)
            .child(millsToString(getCurrentTime(), "yyyy-MM-dd"))
            .child(millsToString(getCurrentTime(), "HH:mm:ss"))
            .setValue(log)
    }

}