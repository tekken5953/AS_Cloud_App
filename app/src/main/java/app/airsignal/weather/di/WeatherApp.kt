package app.airsignal.weather.di

import android.app.Application
import android.content.Context
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.location.GetLocation
import app.airsignal.weather.api.retrofit.HttpClient
import app.airsignal.weather.repository.GetAppVersionRepo
import app.airsignal.weather.repository.GetWarningRepo
import app.airsignal.weather.repository.GetWeatherRepo
import app.airsignal.weather.viewmodel.GetAppVersionViewModel
import app.airsignal.weather.viewmodel.GetWarningViewModel
import app.airsignal.weather.viewmodel.GetWeatherViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(level = Level.DEBUG)
            androidContext(this@WeatherApp)
            modules(listOf(myModule))
        }
    }

    /* single : 싱글톤 빈 정의를 제공. 즉 1번만 객체를 생성한다 */
    /* factory : 호출될 때마다 객체 생성 */
    /* viewModel : 뷰모델 의존성 제거 객체 생성 */
    private val myModule = module {
        single<Context> { applicationContext }
        single { GetLocation(applicationContext) }
        single { HttpClient }
        factory { GetWeatherRepo() }
        factory { GetAppVersionRepo() }
        factory { GetWarningRepo() }
        factory { SubFCM() }
        viewModel { GetAppVersionViewModel(get()) }
        viewModel { GetWeatherViewModel(get()) }
        viewModel { GetWarningViewModel(get()) }
    }
}