package com.example.bulletinboard.frag

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.bulletinboard.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Базовый класс фрагмента, где будет работа с картинками. От него наследуется ImageListFrag
 * Буду настраивать тут рекламный банер adView
 * Разгружаю тут код из ImageListFrag
 */
open class BaseAdsFrag : Fragment(), InterAdsClose {
    lateinit var adView: AdView
    var interAd: InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInterAd()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAds()
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onResume() {
        adView.resume()
        super.onResume()
    }


    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }


    private fun initAds() {
        MobileAds.initialize(activity as Activity)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

    }


    fun loadInterAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context as Activity,
            getString(R.string.ad_inter_id),
            adRequest,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    interAd = ad
                }
            })
    }


    fun showInterAd() {
        if (interAd != null) {

            interAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    onClose()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    onClose()
                }
            }
            interAd?.show(activity as Activity)
        } else {
            onClose()
        }
    }

    override fun onClose() {

    }
}