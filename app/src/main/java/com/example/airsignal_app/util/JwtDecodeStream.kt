package com.example.airsignal_app.util

import android.os.Build
import androidx.annotation.RequiresApi
import okio.ByteString.Companion.decodeBase64
import org.json.JSONObject
import java.util.*


/*
 * JWT Payload 명세
 * KAKAO
 * {
"aud": "c6eb5158a7c0293e81b5ffdd83abadf0",
"sub": "2698802513",
"auth_time": 1678407324,
"iss": "https://kauth.kakao.com",
"nickname": "이재영",
"exp": 1678450524,
"iat": 1678407324,
"email": "tekken5953@naver.com"
}
 */
/**
 *
 * @author : Lee Jae Young
 * @since : 2023-03-10 오전 9:10
 *
 * JWT 토큰의 payload 로 전달된 데이터 추출 **/
class JwtDecodeStream {

    /**
     * JWT 토큰을 입력받아 디코딩하고 JsonObject 형태의 페이로드 중 하나의 데이터값을 불러오는 로직
     *
     * @param jwt JWT Token
     * @param type Payload Json Key
     * @return JWT Token의 Decoded Data중 하나의 Value
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSingleData(jwt: String, type: String): String {
        val jwtPayload =
            String(Base64.getUrlDecoder().decode(jwt.split(".")[1]))

        return JSONObject(jwtPayload).get(type).toString()
    }

    /**
     * JWT 토큰을 입력받아 디코딩하고 JsonObject 형태의 페이로드 전체를 불러오는 로직
     *
     * @param jwt JWT Token
     * @return JWT Token의 Decoded Data 전체
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllData(jwt: String) : String {
        val jwtPayload = String(Base64.getUrlDecoder().decode(jwt.split(".")[1]))
        return JSONObject(jwtPayload).toString()
    }
}