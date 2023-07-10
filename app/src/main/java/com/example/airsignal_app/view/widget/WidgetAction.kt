package com.example.airsignal_app.view.widget

import android.appwidget.AppWidgetManager
import android.content.res.Resources

/**
 * @author : Lee Jae Young
 * @since : 2023-04-28 오전 11:19
 **/
object WidgetAction {
    const val WIDGET_REFRESH_LOCATION = "com.example.airsignal_app.view.widget.RE_GPS"
    const val REFRESH_BUTTON_CLICKED = "com.example.airsignal_app.view.widget.REFRESH"
    const val WIDGET_ENABLE =  AppWidgetManager.ACTION_APPWIDGET_ENABLED
    const val WIDGET_UPDATE = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    const val WIDGET_OPTIONS_CHANGED = AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED

    fun calculateColumnCount(count: Int): Float {
        val deviceWidthInDp =
            Resources.getSystem().displayMetrics.widthPixels / Resources.getSystem().displayMetrics.density
        return deviceWidthInDp / count
    }
}