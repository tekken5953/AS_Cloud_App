package app.airsignal.weather.api

object NetworkUtils {
    /** Current의 rainType의 에러 방지 **/
    fun modifyCurrentRainType(rainTypeCurrent: String, rainTypeReal: String): String {
        val rainList = listOf("비","눈","비/눈","소나기","없음")
        return if (rainTypeCurrent in rainList) rainTypeCurrent else rainTypeReal
    }

    /** Current의 Temperature의 에러 방지 **/
    fun modifyCurrentTempType(tempCurrent: Double?, tempReal: Double): Double {
        return try {
            tempCurrent?.let { tc -> if (tc < 50.0 && tc > -50.0) tc else tempReal } ?: tempReal
        } catch (e: Exception) { return tempReal }
    }

    fun modifyCurrentWindSpeed(windC: Double?, windR: Double): Double {
        return windC?.let { c -> if (c >= -100 && c <= 500) c else windR } ?: windR
    }

    fun modifyCurrentHumid(humidC: Double?, humidR: Double): Double {
        return humidC?.let { h -> if (h >= -100 && h <= 100) h else humidR } ?: humidR
    }
}