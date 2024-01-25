package app.airsignal.weather.firebase.admob

import app.airsignal.weather.dao.RDBLogcat.writeAdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError

open class AdMobListener : AdListener() {

    override fun onAdFailedToLoad(p0: LoadAdError) {
        super.onAdFailedToLoad(p0)
        writeAdError(p0.code.toString(),
            " response : ${p0.responseInfo}" +
                    " msg : ${p0.message}" +
                    " cause : ${p0.cause}"
        )
    }
}