package com.example.airsignal_app.db.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * @author : Lee Jae Young
 * @since : 2023-03-21 오후 1:31
 **/
class PreferenceModel {
    /**
     * 유저의 로그인 정보를 저장하는 테이블
     *
     * @property userId 유저 로그인 ID
     * @property email Email 주소
     * @property login 로그인 플랫폼 > 0 - google, 1 - kakao, 2 - naver
     * @property mobile 핸드폰 번호
     */
    @Entity
    data class UserPreference(
        @PrimaryKey(autoGenerate = false) val userId: String,
        val login: Int,
        val email: String,
        val mobile: String
        )

    /**
     * 유저의 앱 설정 정보를 저장하는 테이블

     *
     * @property theme 테마 정보 > 0 - light, 1 - dark, 2 - system
     * @property notyNormal 일반 알림 수신 > 0 - 거절, 1 - 허용
     * @property notyEvent 이벤트 알림 수신 > 0 - 거절, 1 - 허용
     * @property notyNight 야간 알림 수신 > 0 - 거절, 1 - 허용
     * @property language 언어 정보 > 0 - 한국어, 1 - 영어
     */
    @Entity
    data class ApplicationPreference (
        @PrimaryKey(autoGenerate = false) val id: Int,
        val theme: Int,
        val notyNormal: Int,
        val notyEvent: Int,
        val notyNight: Int,
        val language: Int
        )
}