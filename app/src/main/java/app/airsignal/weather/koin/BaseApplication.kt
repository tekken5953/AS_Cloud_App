package app.airsignal.weather.koin

import android.app.Application
import android.content.Context
import app.airsignal.weather.location.GetLocation
import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.repository.*
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(listOf(myModule))
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
        single { GetWarningRepo() }
        viewModel { GetAppVersionViewModel(get()) }
        viewModel { GetWeatherViewModel(get()) }
        viewModel { GetWarningViewModel(get()) }
    }
}