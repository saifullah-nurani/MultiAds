package io.github.saifullah.nurani.ads.applovin

import AppLovinSDK.MAAdDelegateProtocol
import AppLovinSDK.MAAd
import AppLovinSDK.MAError
import AppLovinSDK.MAInterstitialAd
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import io.github.saifullah.nurani.ads.core.AdError

@OptIn(ExperimentalForeignApi::class)
class AppLovinInterstitialAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val adUnitId: String = adUnitId
    private var mInterstitialAd: MAInterstitialAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mInterstitialAd?.isReady() ?: false

    override fun loadAd() {
        if (mInterstitialAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (adConfig?.isTestModeEnabled != true) {
            require(adUnitId.isNotEmpty()) { "$TAG: adUnitId must not be empty." }
        }
        val finalAdUnitId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else adUnitId
        mInterstitialAd = MAInterstitialAd(finalAdUnitId)
        val delegate = object : NSObject(), MAAdDelegateProtocol {
            override fun didLoadAd(ad: MAAd) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            override fun didFailToLoadAdForAdUnitIdentifier(adUnitIdentifier: String, withError: MAError) {
                adStateManager.onAdFailedToLoad(AdError(0, withError.toString()))
                adLoadListener?.onAdFailedToLoad(AdError(0, withError.toString()))
                if (adStateManager.shouldPreserveOnFailure) {
                    mInterstitialAd = null
                }
            }

            override fun didDisplayAd(ad: MAAd) {
                adStateManager.onAdDisplayed()
                adScreenContentCallback?.onAdShowed()
                adScreenContentCallback?.onAdDisplayed()
            }

            override fun didHideAd(ad: MAAd) {
                adStateManager.onAdDismissed()
                adScreenContentCallback?.onAdDismissed()
                clean()
            }

            override fun didClickAd(ad: MAAd) {
                adStateManager.onAdClicked()
                adScreenContentCallback?.onAdClicked()
            }

            override fun didFailToDisplayAd(ad: MAAd, withError: MAError) {
                val adError = AdError(0, withError.toString())
                adStateManager.onAdFailedToShow(adError)
                adScreenContentCallback?.onAdFailedToShow(adError)
            }
        }
        adDelegate = delegate
        mInterstitialAd?.delegate = delegate
        mInterstitialAd?.loadAd()
    }

    override fun clean() {
        mInterstitialAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        if (isAdAvailable) {
            mInterstitialAd?.showAdForPlacement(null)
        }
    }

    override fun tryShowAd(): Boolean {
        if (isAdAvailable) {
            mInterstitialAd?.showAdForPlacement(null)
            return true
        }
        return false
    }

    companion object {

        fun with(adUnitId: String): AppLovinInterstitialAd {
            return AppLovinInterstitialAd(adUnitId, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): AppLovinInterstitialAd {
            return AppLovinInterstitialAd(adUnitId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "YOUR_MAX_TEST_AD_UNIT_ID"
        const val TAG: String = "AppLovinInterstitialAd"
    }
}
