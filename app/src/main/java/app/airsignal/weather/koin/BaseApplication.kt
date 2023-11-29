package app.airsignal.weather.koin

import android.app.Application
import android.content.Context
import app.airsignal.weather.firebase.db.RDBLogcat
import app.airsignal.weather.gps.GetLocation
import app.airsignal.weather.repo.GetAppVersionRepo
import app.airsignal.weather.repo.GetWarningRepo
import app.airsignal.weather.repo.GetWeatherRepo
import app.airsignal.weather.retrofit.HttpClient
import app.airsignal.weather.vmodel.GetAppVersionViewModel
import app.airsignal.weather.vmodel.GetWarningViewModel
import app.airsignal.weather.vmodel.GetWeatherViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.system.exitProcess

class BaseApplication : Application(), Thread.UncaughtExceptionHandler {
    companion object { private lateinit var appContext: Context }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        Thread.setDefaultUncaughtExceptionHandler(this)

        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(listOf(myModule))
        }
    }

    // ANR 에러 발생 시 로그 저장 후 종료
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        RDBLogcat.writeErrorANR(thread = "Thread is ${p0.name}", msg = "Error Msg is ${p1.stackTraceToString()}")
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
        factory<Context> { applicationContext }
        single { GetLocation(get()) }
        single { HttpClient }
        single { GetWeatherRepo() }
        single { GetAppVersionRepo() }
        single { GetWarningRepo() }
        viewModel { GetAppVersionViewModel(get()) }
        viewModel { GetWeatherViewModel(get()) }
        viewModel { GetWarningViewModel(get()) }
    }
}