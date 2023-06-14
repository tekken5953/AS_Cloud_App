package com.example.airsignal_app.util

import java.time.LocalDateTime
import kotlin.math.atan
import kotlin.math.pow

/**
 * @author : Lee Jae Young
 * @since : 2023-04-17 오전 9:22
 **/
class SensibleTempFormula {

    /**
     * 여름철 체감온도 (5월 ~ 9월)
     *
     * @param ta 기온
     * @param rh 상대습도
     */
    private fun getInSummer(ta: Double, rh: Double) : Double {
        val tw = getTw(ta, rh)
        return -0.2442 + (0.55399 * tw) + (0.45535 * ta) - (0.0022 * tw.pow(2.0)) + (0.00278 * tw * ta) + 3.0
    }

    /**
     * 겨울철 체감온도 (10월 ~ 익년 4월)
     *
     * @param ta 기온
     * @param v 10분 평균 풍속
     */
    private fun getInWinter(ta: Double, v: Double) : Double {
        return 13.12 + (0.6215 * ta) - (11.37 * v.pow(0.16)) + (0.3965 * v.pow(0.16) * ta)
    }

    /** 지금이 몇월인지에 따라 여름 및 겨울 계산공식 적용 **/
    fun getSensibleTemp(ta: Double, rh: Double, v: Double) : Double {
        return if (getCurrentSeason() in 5..9) getInSummer(ta, rh) else getInWinter(ta, v)
    }

    /**
     * 습구온도 계산공식
     *
     * @param ta 온도
     * @param rh 상대습도
     */
    private fun getTw(ta: Double, rh: Double) : Double {
        return ta * atan(0.151977 * (rh + 8.313659).pow(0.5)) +
                atan(ta+rh) - atan(rh-1.67633) +
                (0.00391838 * rh.pow(1.5) * atan(0.023101 * rh)) - 4.686035
    }

    /** 현재 월수 출력 **/
    private fun getCurrentSeason() : Int {
        return LocalDateTime.now().monthValue
    }
}