package app.airsignal.weather.firebase.admob

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

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
    }

    fun loadAdView(adView: AdView) {
        adView.adListener = AdMobListener()
        adView.loadAd(adRequest)
        adView.requestLayout()
    }
}