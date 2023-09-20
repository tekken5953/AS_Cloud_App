package app.airsignal.weather.dao

object ErrorCode {
    const val ERROR_NETWORK = "Network Error"
    const val ERROR_NOT_SERVICED_LOCATION = "NOT SERVICED Location"
    const val ERROR_API_PROTOCOL = "API ERROR OCCURRED"
    const val ERROR_NULL_DATA = "API DATA IS NULL"
    const val ERROR_SERVER_CONNECTING = "Server Error OCCURRED"
    const val ERROR_TIMEOUT = "Timeout Error"
    const val ERROR_GET_LOCATION_FAILED = "Get Location Error"
    const val ERROR_GPS_CONNECTED = "GPS Connect Error"
    const val ERROR_GET_DATA = "IndexOutOfBoundsException"
    const val ERROR_LOCATION_IOException = "주소 - IOException"
    const val ERROR_LOCATION_FAILED = "GPS 위치정보 갱신실패"
    const val ERROR_UNKNOWN = "Unknown Error"
}