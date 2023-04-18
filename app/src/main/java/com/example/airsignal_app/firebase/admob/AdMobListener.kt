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
        Timber.tag(TAG_AD).d("onAdClicked")
    }

    override fun onAdClosed() {
        super.onAdClosed()
        Timber.tag(TAG_AD).d("onAdClicked")
    }

    override fun onAdFailedToLoad(p0: LoadAdError) {
        super.onAdFailedToLoad(p0)
        Timber.tag(TAG_AD).d("onAdFailedToLoad ${p0.responseInfo}")
    }

    override fun onAdImpression() {
        super.onAdImpression()
        Timber.tag(TAG_AD).d("onAdImpression")
    }

    override fun onAdLoaded() {
        super.onAdLoaded()
        Timber.tag(TAG_AD).d("onAdLoaded")
    }

    override fun onAdOpened() {
        super.onAdOpened()
        Timber.tag(TAG_AD).d("onAdOpened")
    }

    override fun onAdSwipeGestureClicked() {
        super.onAdSwipeGestureClicked()
        Timber.tag(TAG_AD).d("onAdSwipeGestureClicked")
    }
}