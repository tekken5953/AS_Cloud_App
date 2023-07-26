package com.example.airsignal_app.firebase.admob

import com.example.airsignal_app.dao.StaticDataObject.TAG_AD
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import timber.log.Timber

/**
 * @author : Lee Jae Young
 * @since : 2023-04-14 오후 4:47
 **/
class AdMobListener : AdListener() {
    override fun onAdClicked() {
        super.onAdClicked()
    }

    override fun onAdClosed() {
        super.onAdClosed()
    }

    override fun onAdFailedToLoad(p0: LoadAdError) {
        super.onAdFailedToLoad(p0)
    }

    override fun onAdImpression() {
        super.onAdImpression()
    }

    override fun onAdLoaded() {
        super.onAdLoaded()
    }

    override fun onAdOpened() {
        super.onAdOpened()
    }

    override fun onAdSwipeGestureClicked() {
        super.onAdSwipeGestureClicked()
    }
}