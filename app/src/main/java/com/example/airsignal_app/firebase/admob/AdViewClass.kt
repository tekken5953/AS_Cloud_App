package com.example.airsignal_app.firebase.admob

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

/**
 * @author : Lee Jae Young
 * @since : 2023-04-14 오후 5:15
 **/
class AdViewClass(private val context: Context) {
    //https://developers.google.com/admob/android/test-ads?hl=ko        // Test
    //https://support.google.com/admob/answer/6128543?hl=ko             // 정책
    //https://apps.admob.com/v2/apps/2919179286/overview?pli=1&sac=true // 콘솔

    fun loadAdView(adView: AdView) {
        MobileAds.initialize(context)
//        Timber.tag(TAG_AD).d("Google Mobile Ads SDK Version:  ${MobileAds.getVersion()}")
//        val testDeviceIds = listOf(adMobTestDeviceId)
//        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//        MobileAds.setRequestConfiguration(configuration)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}