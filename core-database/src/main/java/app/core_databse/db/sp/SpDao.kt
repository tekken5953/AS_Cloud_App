package app.core_databse.db.sp

object SpDao {
    const val lastLoginPlatform = "last_login"
    const val lastLoginPhone = "phone_number"
    const val userLocation = "user_location"
    const val userId = "user_id"
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
    const val CHECK_GPS_BACKGROUND = "BACKGROUND_GPS_OK"  // GPS WorkManager ID 값
    const val LANDING_NOTIFICATION = "rending_notification"
    const val TEXT_SCALE_BIG = "big"
    const val TEXT_SCALE_SMALL = "small"
    const val TEXT_SCALE_DEFAULT = "default"
    const val IN_APP_MSG_NAME = "inAppMsg"
}