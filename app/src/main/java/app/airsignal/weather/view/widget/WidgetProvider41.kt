package app.airsignal.weather.view.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import app.airsignal.weather.R
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.util.LoggerUtil
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-07-04 오후 4:27
 **/
open class WidgetProvider41 : AppWidgetProvider() {

    init { LoggerUtil().getInstance() }

    companion object {
        const val REFRESH_BUTTON_CLICKED = "app.airsignal.weather.view.widget.REFRESH_DATA"
        const val ENTER_APPLICATION = "app.airsignal.weather.view.widget.ENTER_APP"
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Timber.tag(StaticDataObject.TAG_W).i("onEnabled")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Timber.tag(StaticDataObject.TAG_W).i("onDisabled")
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Timber.tag(StaticDataObject.TAG_W).i("onAppWidgetOptionsChanged")
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        Timber.tag(StaticDataObject.TAG_W).i("onRestored")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Timber.tag(StaticDataObject.TAG_W).i("onDeleted")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_layout_4x1)
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                RDBLogcat.writeWidgetHistory(context.applicationContext,"error", e.stackTraceToString())
            }
        }
    }
}