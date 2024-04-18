package app.airsignal.weather.db.sp

object SpDao {
    const val lastLoginPlatform = "last_login"
    const val lastLoginPhone = "phone_number"
    const val userLocation = "user_location"
    const val userId = "user_id"
    const val userTheme = "theme"
    const val userProfile = "user_profile"
    const val userEmail = "user_email"
    const val notiEnable = "notification_enable"
    const val notiVibrate = "notification_vibrate"
    const val notiSound = "notification_sound"
    const val lastAddress = "last_address"
    const val userFontScale = "scale"
    const val NOTIFICATION_ADDRESS = "Notification_address" // 노티피케이션 용 주소
    const val NOTIFICATION_TOPIC_DAILY = "Notification_Daily"
    const val INITIALIZED_LOC_PERMISSION = "initialized_loc_permission"
    const val INITIALIZED_NOTI_PERMISSION = "initialized_noti_permission"
    const val IS_INIT_BACK_LOC_PERMISSION = "is_back_location_enable"
    const val IS_PERMED_BACK_LOG = "is_permed_back_log"
    const val WARNING_FIXED = "warning_fixed"
    const val LAST_REFRESH42 = "last_refresh_42"
    const val LAST_REFRESH22 = "last_refresh_22"
    const val CURRENT_GPS_ID = "Current"
    const val LANG_KR = "korea"
    const val LANG_EN = "english"
    const val LANG_SYS = "system"
    const val TEXT_SCALE_BIG = "big"
    const val TEXT_SCALE_SMALL = "small"
    const val TEXT_SCALE_DEFAULT = "default"
    const val IN_APP_MSG = "inAppMsgImg"
    const val IN_APP_MSG_COUNT = "inAppMsgCount"
    const val IN_APP_MSG_REDIRECT = "inAppMsgRedirect"
    const val IN_APP_MSG_TIME = "inAppMsgTime"
    const val TUTORIAL_SKIP = "eye_tutorial_skip"
    const val PATCH_SKIP = "skip_patch"
    const val WEATHER_ANIMATION_ENABLE = "weather_animation_enabled"
    const val WEATHER_BOX_OPACITY = "weather_box_opacity"
    const val WEATHER_BOX_OPACITY2 = "weather_box_opacity2"
}