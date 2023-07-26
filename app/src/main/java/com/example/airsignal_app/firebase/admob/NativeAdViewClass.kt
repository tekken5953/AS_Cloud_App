package com.example.airsignal_app.firebase.admob

import android.app.Activity
import android.widget.ImageView
import android.widget.TextView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.TAG_AD
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import timber.log.Timber

/**
* @author : Lee Jae Young
* @since : 2023-06-08 오후 1:39
**/
class NativeAdViewClass(private val activity: Activity) {
    private lateinit var adLoader: AdLoader

    fun load(nativeAdView: NativeAdView, title: TextView, description: ImageView) {
        adLoader = AdLoader.Builder(activity, activity.getString(R.string.adUnit_exit_Id))
            .forNativeAd { ad : NativeAd ->
                nativeAdView.setNativeAd(ad)
                ad.images.forEach {
                    description.setImageDrawable(it.drawable)
                    title.text = ad.headline
                }
                // Show the ad.
                if (adLoader.isLoading) {
                    // The AdLoader is still loading ads.
                    // Expect more adLoaded or onAdFailedToLoad callbacks.
//                    Timber.tag(TAG_AD).d( "Native Ad forNativeAd is Loading")
                } else {
                    // The AdLoader has finished loading ads.
//                    Timber.tag(TAG_AD).d("Native Ad forNativeAd is Ended")
                }

                if (activity.isDestroyed) {
                    ad.destroy()
                    return@forNativeAd
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
//                    Timber.tag(TAG_AD).d("onAdFailedToLoad")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                // Methods in the NativeAdOptions.Builder class can be
                // used here to specify individual options settings.
                .build())
            .build()

        adLoader.loadAds(AdRequest.Builder().build(), 3)
    }
}