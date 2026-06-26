package io.github.saifullah.nurani.ads.vungle

import VungleAdsSDK.VungleInterstitial
import VungleAdsSDK.VungleInterstitialDelegateProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class VungleInterstitialAd(
    placementId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val placementId: String = placementId
    private var mInterstitialAd: VungleInterstitial? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mInterstitialAd?.canPlayAd() ?: false

    override fun loadAd() {
        if (mInterstitialAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!VungleAds.isInitialized()) {
            val adError = io.github.saifullah.nurani.ads.core.AdError(
                code = 0,
                message = "Vungle SDK is not initialized yet."
            )
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            return
        }
        if (adConfig?.isTestModeEnabled != true) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId.isNotEmpty()) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else placementId
        mInterstitialAd = VungleInterstitial(placementId = finalPlacementId)
        val delegate = object : NSObject(), VungleInterstitialDelegateProtocol {
            override fun interstitialAdDidLoad(interstitial: VungleInterstitial) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            override fun interstitialAdDidFailToLoad(interstitial: VungleInterstitial, withError: NSError) {
                adStateManager.onAdFailedToLoad(AdError(0, withError.localizedDescription ?: "Unknown error"))
                adLoadListener?.onAdFailedToLoad(AdError(0, withError.localizedDescription ?: "Unknown error"))
                if (adStateManager.shouldPreserveOnFailure) {
                    mInterstitialAd = null
                }
            }

            override fun interstitialAdWillPresent(interstitial: VungleInterstitial) {}

            override fun interstitialAdDidPresent(interstitial: VungleInterstitial) {
                adStateManager.onAdDisplayed()
            }

            override fun interstitialAdDidFailToPresent(interstitial: VungleInterstitial, withError: NSError) {
                adStateManager.onAdFailedToShow(AdError(0, withError.localizedDescription ?: "Unknown error"))
            }

            override fun interstitialAdDidClick(interstitial: VungleInterstitial) {
                adStateManager.onAdClicked()
            }

            override fun interstitialAdDidClose(interstitial: VungleInterstitial) {
                adStateManager.onAdDismissed()
                clean()
            }
            
            override fun interstitialAdDidTrackImpression(interstitial: VungleInterstitial) {}
            override fun interstitialAdWillClose(interstitial: VungleInterstitial) {}
            override fun interstitialAdWillLeaveApplication(interstitial: VungleInterstitial) {}
        }
        adDelegate = delegate
        mInterstitialAd?.setDelegate(delegate)
        mInterstitialAd?.load(null)
    }

    override fun clean() {
        mInterstitialAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        if (isAdAvailable) {
            mInterstitialAd?.presentWith(owner)
        }
    }

    override fun tryShowAd(): Boolean {
        return false
    }

    companion object {

        fun with(placementId: String): VungleInterstitialAd {
            return VungleInterstitialAd(placementId, null, null)
        }

        fun with(placementId: String, adConfig: AdConfig): VungleInterstitialAd {
            return VungleInterstitialAd(placementId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "I1-8348515"
        const val TAG: String = "VungleInterstitialAd"
    }
}
