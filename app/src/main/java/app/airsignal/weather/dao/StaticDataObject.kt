package app.airsignal.weather.dao

object StaticDataObject {
    const val TAG_R = "Tag_Retrofit"                      // 서버통신 기본 태그 Key
    const val TAG_N = "Tag_Notification"                  // FCM 기본 태그 Key
    const val TAG_D = "TAG_DB"                            // Room DB
    const val TAG_P = "TAG_Permission"
    const val TAG_LOGIN = "TAG_LOGIN"                     // Logger 태그 키값
    const val REQUEST_LOCATION = 0x0000001                       // 위치권한 요청 Result Code
    const val REQUEST_NOTIFICATION = 0x0000002                    // 알림권한 요청 Result Code
    const val REQUEST_BACKGROUND_LOCATION = 0x0000003
    const val NOTIFICATION_CHANNEL_ID = "500"             // FCM 채널 ID
    const val NOTIFICATION_CHANNEL_NAME = "AIRSIGNAL"     // FCM 채널 NAME
    const val CURRENT_GPS_ID = "Current"                  // 현재 주소 아이디
    const val NOTIFICATION_ADDRESS = "Notification_address" // 노티피케이션 용 주소
    const val INITIALIZED_LOC_PERMISSION = "initialized_loc_permission"
    const val INITIALIZED_NOTI_PERMISSION = "initialized_noti_permission"
    const val LAST_REFRESH_WIDGET_TIME = "widget_last_refresh"
    const val IS_INIT_BACK_LOC_PERMISSION = "is_back_location_enable"
    const val IS_PERMED_BACK_LOG = "is_permed_back_log"
    const val WARNING_FIXED = "warning_fixed"
    const val LAST_LAT = "last_lat"
    const val LAST_LNG = "last_lng"
    const val SHOWING_LOADING_FLOAT = 0.5f
    const val NOT_SHOWING_LOADING_FLOAT = 1f
    const val PM2p5_INDEX = 0
    const val PM10_INDEX = 1
    const val CO_INDEX = 2
    const val SO2_INDEX = 3
    const val NO2_INDEX = 4
    const val O3_INDEX = 5
    const val IN_COMPLETE_ADDRESS = "불완전한 주소"
    const val LANG_KR = "korea"
    const val LANG_EN = "english"
    const val LANG_SYS = "system"
    const val TEXT_SCALE_BIG = "big"
    const val TEXT_SCALE_SMALL = "small"
    const val TEXT_SCALE_DEFAULT = "default"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val SHARE_ADDR = 0
    const val SHARE_SKY = 10
    const val SHARE_TEMP = 20
    const val SHARE_RAIN = 30
    const val SHARE_HUMID = 40
}