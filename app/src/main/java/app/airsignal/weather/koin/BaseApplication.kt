package app.airsignal.weather.koin

import android.app.Application
import android.content.Context
import app.airsignal.core_network.retrofit.ApiModel
import app.airsignal.core_network.retrofit.HttpClient
import app.airsignal.core_repository.GetAppVersionRepo
import app.airsignal.core_repository.GetWarningRepo
import app.airsignal.core_repository.GetWeatherRepo
import app.airsignal.core_viewmodel.GetAppVersionViewModel
import app.airsignal.core_viewmodel.GetWarningViewModel
import app.airsignal.core_viewmodel.GetWeatherViewModel
import app.airsignal.weather.dao.RDBLogcat
import app.core_databse.db.SharedPreferenceManager
import app.core_databse.db.database.GpsDataBase
import app.core_databse.db.room.repository.GpsRepository
import app.location.GetLocation
import app.utils.LoggerUtil
import app.utils.TimberUtil
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.system.exitProcess

class BaseApplication : Application(), Thread.UncaughtExceptionHandler {
    companion object {
        private lateinit var appContext: Context
        lateinit var timber: TimberUtil
        lateinit var logger: LoggerUtil
    }

    val database by lazy { GpsDataBase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        Thread.setDefaultUncaughtExceptionHandler(this)
        timber = TimberUtil()
        timber.getInstance()
        logger = LoggerUtil()
        logger.getInstance()

        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(listOf(myModule,coreDatabaseModule,coreNetworkModule))
        }
    }

    // ANR 에러 발생 시 로그 저장 후 종료
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        RDBLogcat.writeErrorANR(thread = "Thread is ${p0.name}", msg = "Error Msg is ${p1.stackTraceToString()}")
        p1.printStackTrace()
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

    private val coreDatabaseModule = module {
        single { GpsRepository(applicationContext) }
        single { SharedPreferenceManager(get()) }
    }

    private val coreNetworkModule = module {
        single { ApiModel() }
        single { HttpClient }
    }
}