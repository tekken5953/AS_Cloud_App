package app.airsignal.weather.dao

object StaticDataObject {
    const val LANG_KR = "korea"
    const val LANG_EN = "english"
    const val LANG_SYS = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val REQUEST_LOCATION = 0x0000001                       // 위치권한 요청 Result Code
    const val REQUEST_NOTIFICATION = 0x0000002                    // 알림권한 요청 Result Code
    const val REQUEST_BACKGROUND_LOCATION = 0x0000003

    enum class FcmSort(val key: String) {
        FCM_DAILY("daily"), FCM_PATCH("patch"), FCM_EVENT("event"), FCM_ADMIN("admin")
    }

    enum class FcmChannel(val value: String) {
        NOTIFICATION_CHANNEL_ID("500"),             // FCM 채널 ID
        NOTIFICATION_CHANNEL_NAME("AIRSIGNAL"),     // FCM 채널 NAME
        NOTIFICATION_CHANNEL_DESCRIPTION("Channel description")
    }
}