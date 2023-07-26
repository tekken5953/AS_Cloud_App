package com.example.airsignal_app.view.widget

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.firebase.db.RDBLogcat.WIDGET_ACTION
import com.example.airsignal_app.firebase.db.RDBLogcat.WIDGET_DOZE_MODE
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.GetAppInfo
import com.example.airsignal_app.view.ToastUtils
import com.example.airsignal_app.view.widget.WidgetAction.WIDGET_ENABLE
import com.example.airsignal_app.view.widget.WidgetAction.WIDGET_UPDATE


open class WidgetProvider4x2 : AppWidgetProvider() {


    // 앱 위젯은 여러개가 등록 될 수 있는데, 최초의 앱 위젯이 등록 될 때 호출 됩니다. (각 앱 위젯 인스턴스가 등록 될때마다 호출 되는 것이 아님)
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        if (!isJobScheduled(context)) {
            NotiJobScheduler().scheduleJob(context)
        }
    }

    // onEnabled() 와는 반대로 마지막의 최종 앱 위젯 인스턴스가 삭제 될 때 호출 됩니다
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    // android 4.1 에 추가 된 메소드 이며, 앱 위젯이 등록 될 때와 앱 위젯의 크기가 변경 될 때 호출 됩니다.
    // 이때, Bundle 에 위젯 너비/높이의 상한값/하한값 정보를 넘겨주며 이를 통해 컨텐츠를 표시하거나 숨기는 등의 동작을 구현 합니다
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        newOptions.putInt(
            AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
            newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        )
        newOptions.putInt(
            AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
            newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        )
    }

    // 위젯 메타 데이터를 구성 할 때 updatePeriodMillis 라는 업데이트 주기 값을 설정하게 되며, 이 주기에 따라 호출 됩니다.
    // 또한 앱 위젯이 추가 될 떄에도 호출 되므로 Service 와의 상호작용 등의 초기 설정이 필요 할 경우에도 이 메소드를 통해 구현합니다
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (!isJobScheduled(context)) {
            NotiJobScheduler().scheduleJob(context)
        }
    }

    // 이 메소드는 앱 데이터가 구글 시스템에 백업 된 이후 복원 될 때 만약 위젯 데이터가 있다면 데이터가 복구 된 이후 호출 됩니다.
    // 일반적으로 사용 될 경우는 흔치 않습니다.
    // 위젯 ID 는 UID 별로 관리 되는데 이때 복원 시점에서 ID 가 변경 될 수 있으므로 백업 시점의 oldID 와 복원 후의 newID 를 전달합니다
    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    // 해당 앱 위젯이 삭제 될 때 호출 됩니다
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
    }

    // 앱의 브로드캐스트를 수신하며 해당 메서드를 통해 각 브로드캐스트에 맞게 메서드를 호출한다.
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        intent.action?.let {
            RDBLogcat.writeWidgetPref(
                context,
                sort = WIDGET_ACTION,
                value = intent.action.toString()
            )
            when (it) {
                WIDGET_ENABLE -> {
                    NotiJobService().getWidgetLocation(context)
                }
                WIDGET_UPDATE
                -> {
                    val isDozeMode = intent.getBooleanExtra(
                        "android.os.extra.IDLE_MODE",
                        false
                    )

                    RDBLogcat.writeWidgetPref(
                        context,
                        sort = WIDGET_DOZE_MODE,
                        value = "Doze Mode is $isDozeMode"
                    )
                    if (isDozeMode) {
                        NotiJobScheduler().scheduleJob(context)
                    } else {
                        if (isRefreshable(context)) {
                            NotiJobScheduler().scheduleJob(context)
                        } else {
                            ToastUtils(context).showMessage("마지막 갱신 후 1분 뒤에 가능합니다", 1)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    class NotiJobScheduler : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_SCREEN_ON -> {
                    scheduleJob(context)
                }
            }
        }

        fun scheduleJob(context: Context) {
            // JobScheduler 생성 및 설정
            val jobScheduler =
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            val componentName = ComponentName(context, NotiJobService::class.java)
            val jobInfo = JobInfo.Builder(JOB_ID, componentName)
                .setPeriodic(INTERVAL_MILLISECONDS)
                .setPersisted(true)
                .build()

            if (!WidgetProvider4x2().isJobScheduled(context)) {
                jobScheduler.schedule(jobInfo)
            } else {
                NotiJobService().getWidgetLocation(context)
            }
        }

        companion object {
            const val JOB_ID = 1001
            private const val INTERVAL_MILLISECONDS: Long = 30 * 60 * 1000
        }
    }

    fun isJobScheduled(context: Context): Boolean {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val pendingJobs = jobScheduler.allPendingJobs
        for (jobInfo in pendingJobs) {
            if (jobInfo.id == NotiJobScheduler.JOB_ID) {
                return true
            }
        }

        return false
    }

    private fun isRefreshable(context: Context): Boolean {
        return getCurrentTime() - GetAppInfo.getLastRefreshTime(context) >= 1000 * 60
    }
}