package com.example.airsignal_app.koin

import android.app.Application
import com.example.airsignal_app.firebase.db.RDBLogcat
import com.example.airsignal_app.vmodel.GetWeatherViewModel
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
            modules(listOf(weatherDataModule))
        }
    }

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        RDBLogcat.writeLogCause("ANR 발생", "Thread : ${p0.name}", "Error Msg: ${p1.localizedMessage}\t Error Trace: ${p1.printStackTrace()}")
        Thread.sleep(100)
        exitProcess(1)
    }

    /* single : 싱글톤 빈 정의를 제공. 즉 1번만 객체를 생성한다 */
    /* factory : 호출될 때마다 객체 생성 */
    /* viewModel : 뷰모델 의존성 제거 객체 생성 */

    private val weatherDataModule = module { viewModel { GetWeatherViewModel() } }
}