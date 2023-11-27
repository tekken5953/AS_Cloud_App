package app.airsignal.weather.dao

object StaticDataObject {
    const val TAG_R = "Tag_Retrofit"                      // 서버통신 기본 태그 Key
    const val TAG_LOGIN = "TAG_LOGIN"                     // Logger 태그 키값
    const val CURRENT_GPS_ID = "Current"                  // 현재 주소 아이디
    const val NOTIFICATION_ADDRESS = "Notification_address" // 노티피케이션 용 주소
    const val NOTIFICATION_TOPIC_DAILY = "Notification_Daily"
    const val INITIALIZED_LOC_PERMISSION = "initialized_loc_permission"
    const val INITIALIZED_NOTI_PERMISSION = "initialized_noti_permission"
    const val IS_INIT_BACK_LOC_PERMISSION = "is_back_location_enable"
    const val IS_PERMED_BACK_LOG = "is_permed_back_log"
    const val WARNING_FIXED = "warning_fixed"
    const val LAST_LAT = "last_lat"
    const val LAST_LNG = "last_lng"
    const val LANG_KR = "korea"
    const val LANG_EN = "english"
    const val LANG_SYS = "system"
    const val LAST_REFRESH = "last_refresh"
    const val TEXT_SCALE_BIG = "big"
    const val TEXT_SCALE_SMALL = "small"
    const val TEXT_SCALE_DEFAULT = "default"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val REQUEST_LOCATION = 0x0000001                       // 위치권한 요청 Result Code
    const val REQUEST_NOTIFICATION = 0x0000002                    // 알림권한 요청 Result Code
    const val REQUEST_BACKGROUND_LOCATION = 0x0000003
    const val TAG_N = "Tag_Notification"                  // FCM 기본 태그 Key
    const val TAG_W = "Tag_Widget"
}