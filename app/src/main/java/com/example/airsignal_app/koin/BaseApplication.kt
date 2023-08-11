package com.example.airsignal_app.koin

import android.app.Application
import android.app.ApplicationExitInfo
import android.content.Context
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.repo.GetAppVersionRepo
import com.example.airsignal_app.repo.GetWeatherRepo
import com.example.airsignal_app.retrofit.HttpClient
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserEmail
import com.example.airsignal_app.vmodel.GetAppVersionViewModel
import com.example.airsignal_app.vmodel.GetWeatherViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.common.CrashlyticsReportDataCapture
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.system.exitProcess

class BaseApplication : Application(), Thread.UncaughtExceptionHandler {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(this)

        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(listOf(myModule))
        }
    }

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        RDBLogcat.writeErrorANR(thread = "Thread : ${p0.name}", msg = "Error Msg: ${p1.stackTraceToString()}" )
        FirebaseCrashlytics.getInstance().apply {
            try {
                setUserId(getUserEmail(applicationContext))
            } catch(e: NullPointerException) {
                e.printStackTrace()
            }
            recordException(p1) // Crashlytics 에 에러로그 기록
        }
            if (p0.name == "WidgetProvider") {
                HttpClient.getInstance(true).setClientBuilder()
            } else {
                Thread.sleep(100)
                exitProcess(1)
            }
    }

    /* single : 싱글톤 빈 정의를 제공. 즉 1번만 객체를 생성한다 */
    /* factory : 호출될 때마다 객체 생성 */
    /* viewModel : 뷰모델 의존성 제거 객체 생성 */

    private val myModule = module {
        single<Context> { applicationContext }
        single { GetLocation(get()) }
        single { HttpClient }
        single { GetWeatherRepo() }
        single { GetAppVersionRepo() }
        viewModel { GetAppVersionViewModel(get()) }
        viewModel { GetWeatherViewModel(get()) }
    }
}