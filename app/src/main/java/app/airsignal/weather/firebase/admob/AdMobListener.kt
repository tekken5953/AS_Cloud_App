package app.airsignal.weather.firebase.admob

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError

open class AdMobListener : AdListener() {

    override fun onAdFailedToLoad(p0: LoadAdError) {
        super.onAdFailedToLoad(p0)
    }
}