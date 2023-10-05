package app.airsignal.weather.firebase.admob

import app.airsignal.weather.firebase.db.RDBLogcat.writeAdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.orhanobut.logger.Logger

open class AdMobListener : AdListener() {
    override fun onAdFailedToLoad(p0: LoadAdError) {
        super.onAdFailedToLoad(p0)
        Logger.t("TAG_AD").e("Fail to load Admob :" +
                " response : ${p0.responseInfo}" +
                " code : ${p0.code}" +
                " msg : ${p0.message}" +
                " cause : ${p0.cause}")
        writeAdError(p0.code.toString(),
            " response : ${p0.responseInfo}" +
                    " msg : ${p0.message}" +
                    " cause : ${p0.cause}"
        )
    }

    override fun onAdLoaded() {
        super.onAdLoaded()
        Logger.t("TAG_AD").i("Success to load Admob")
    }
}