package app.airsignal.weather.firebase.admob

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.*
import kotlin.math.acos

/**
 * @author : Lee Jae Young
 * @since : 2023-04-14 오후 5:15
 **/
class AdViewClass(activity: Activity) {
    private var adRequest: AdRequest
    //https://developers.google.com/admob/android/test-ads?hl=ko        // Test
    //https://support.google.com/admob/answer/6128543?hl=ko             // 정책
    //https://apps.admob.com/v2/apps/2919179286/overview?pli=1&sac=true // 콘솔

    init {
        MobileAds.initialize(activity)
        adRequest = AdRequest.Builder().build()
        val conf = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("7be5367b-fd65-4c63-9457-64b80271f5b6")).build()
        MobileAds.setRequestConfiguration(conf)
    }

    fun loadAdView(adView: AdView) {
        adView.adListener = AdMobListener()
        adView.loadAd(adRequest)
        adView.requestLayout()
    }
}