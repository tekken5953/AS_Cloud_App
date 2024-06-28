package app.airsignal.weather.api

object NetworkUtils {
    /** Current의 rainType의 에러 방지 **/
    fun modifyCurrentRainType(rainTypeCurrent: String, rainTypeReal: String): String =
        if (rainTypeCurrent in listOf("비","눈","비/눈","소나기","없음")) rainTypeCurrent else rainTypeReal

    /** Current의 Temperature의 에러 방지 **/
    fun modifyCurrentTempType(tempCurrent: Double?, tempReal: Double): Double =
        kotlin.runCatching {
            tempCurrent?.let { tc -> if (tc < 50.0 && tc > -50.0) tc else tempReal } ?: tempReal
        }.getOrElse { tempReal }

    fun modifyCurrentWindSpeed(windC: Double?, windR: Double): Double =
        windC?.let { c -> if (c >= -100 && c <= 500) c else windR } ?: windR

    fun modifyCurrentHumid(humidC: Double?, humidR: Double): Double =
        humidC?.let { h -> if (h >= -100 && h <= 100) h else humidR } ?: humidR
}