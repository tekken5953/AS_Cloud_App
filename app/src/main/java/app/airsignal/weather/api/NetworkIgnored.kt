package app.airsignal.weather.api

object NetworkIgnored {
    /**  API 서버 엔드포인트 **/
    const val hostingServerURL = "https://ascloud.kr/api/"
    const val warningPoint = "forecast/broadcast"
    const val splashPoint = "version"
    const val notiPoint = "notice"
    const val weatherPoint = "forecast"

    const val weatherParamLeft = "lat"
    const val weatherParamCenter = "lng"
    const val weatherParamRight = "addr"
    const val weatherParamElse = "rcount"
    const val warningParam = "code"

    const val headerConnection = "close"
    const val headerPlatform = "android"

    const val connectTimeout = 8L
    const val readTimeout = 8L
    const val writeTimeout = 8L
}