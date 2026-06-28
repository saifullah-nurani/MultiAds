package io.github.saifullah.nurani.ads.ironsource

import IronSource.LPMAdInfo
import IronSource.LPMInterstitialAd
import IronSource.LPMInterstitialAdDelegateProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IronSourceInterstitialAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val adUnitId: String = adUnitId
    private var mInterstitialAd: LPMInterstitialAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mInterstitialAd?.isAdReady() ?: false

    override fun loadAd() {
        if (isAdAvailable) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!IronSourceAds.isInitialized()) {
            IronSourceAds.runWhenInitialized {
                onAdLoad()
            }
            return
        }

        if (adConfig?.isTestModeEnabled != true) {
            require(adUnitId.isNotEmpty()) { "$TAG: adUnitId must not be empty." }
        }
        val finalAdUnitId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else adUnitId
        mInterstitialAd = LPMInterstitialAd(adUnitId = finalAdUnitId)
        val delegate = object : NSObject(), LPMInterstitialAdDelegateProtocol {
            override fun didLoadAdWithAdInfo(adInfo: LPMAdInfo) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            override fun didFailToLoadAdWithAdUnitId(adUnitId: String, error: NSError) {
                adStateManager.onAdFailedToLoad(AdError(0, error.localizedDescription ?: "Unknown error"))
                adLoadListener?.onAdFailedToLoad(AdError(0, error.localizedDescription ?: "Unknown error"))
            }

            override fun didDisplayAdWithAdInfo(adInfo: LPMAdInfo) {
                adStateManager.onAdDisplayed()
                adScreenContentCallback?.onAdShowed()
                adScreenContentCallback?.onAdDisplayed()
            }

            override fun didFailToDisplayAdWithAdInfo(adInfo: LPMAdInfo, error: NSError) {
                val adError = AdError(0, error.localizedDescription ?: "Unknown error")
                adStateManager.onAdFailedToShow(adError)
                adScreenContentCallback?.onAdFailedToShow(adError)
            }

            override fun didClickAdWithAdInfo(adInfo: LPMAdInfo) {
                adStateManager.onAdClicked()
                adScreenContentCallback?.onAdClicked()
            }

            override fun didCloseAdWithAdInfo(adInfo: LPMAdInfo) {
                adStateManager.onAdDismissed()
                adScreenContentCallback?.onAdDismissed()
                clean()
            }
            
            override fun didChangeAdInfo(adInfo: LPMAdInfo) {}
        }
        adDelegate = delegate
        mInterstitialAd?.setDelegate(delegate)
        mInterstitialAd?.loadAd()
    }

    override fun clean() {
        mInterstitialAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        if (isAdAvailable) {
            mInterstitialAd?.showAdWithViewController(owner, placementName = null)
        }
    }

    override fun tryShowAd(): Boolean {
        val root = platform.UIKit.UIApplication.sharedApplication.keyWindow?.rootViewController
        if (root != null && isAdAvailable) {
            showAd(root)
            return true
        }
        return false
    }

    companion object {

        fun with(adUnitId: String): IronSourceInterstitialAd {
            return IronSourceInterstitialAd(adUnitId, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): IronSourceInterstitialAd {
            return IronSourceInterstitialAd(adUnitId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "24965126"
        const val TAG: String = "IronSourceInterstitialAd"
    }
}
