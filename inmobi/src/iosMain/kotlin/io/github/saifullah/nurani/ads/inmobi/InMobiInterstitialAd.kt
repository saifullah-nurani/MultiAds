package io.github.saifullah.nurani.ads.inmobi

import InMobiSDK.IMInterstitial
import InMobiSDK.IMInterstitialDelegateProtocol
import InMobiSDK.IMRequestStatus
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class InMobiInterstitialAd(
    placementId: Long,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val placementId: Long = placementId
    private var mInterstitialAd: IMInterstitial? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mInterstitialAd?.isReady() ?: false

    override fun loadAd() {
        if (mInterstitialAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!InMobiAds.isInitialized()) {
            val adError = io.github.saifullah.nurani.ads.core.AdError(
                code = 0,
                message = "InMobi SDK is not initialized yet."
            )
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            return
        }
        val delegate = object : NSObject(), IMInterstitialDelegateProtocol {
            override fun interstitialDidFinishLoading(interstitial: IMInterstitial) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            @kotlinx.cinterop.ObjCSignatureOverride
            override fun interstitial(interstitial: IMInterstitial, didFailToLoadWithError: IMRequestStatus) {
                adStateManager.onAdFailedToLoad(AdError(0, didFailToLoadWithError.toString()))
                adLoadListener?.onAdFailedToLoad(AdError(0, didFailToLoadWithError.toString()))
                if (adStateManager.shouldPreserveOnFailure) {
                    mInterstitialAd = null
                }
            }

            override fun interstitialDidPresent(interstitial: IMInterstitial) {
                adStateManager.onAdDisplayed()
            }

            override fun interstitialDidDismiss(interstitial: IMInterstitial) {
                adStateManager.onAdDismissed()
                clean()
            }

            @kotlinx.cinterop.ObjCSignatureOverride
            override fun interstitial(interstitial: IMInterstitial, didFailToPresentWithError: IMRequestStatus) {
                adStateManager.onAdFailedToShow(AdError(0, didFailToPresentWithError.toString()))
            }

            override fun interstitial(interstitial: IMInterstitial, didInteractWithParams: Map<Any?, *>?) {
                adStateManager.onAdClicked()
            }
        }
        adDelegate = delegate
        if (adConfig?.isTestModeEnabled != true) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId != 0L) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID.toLong() else placementId
        mInterstitialAd = IMInterstitial(placementId = finalPlacementId, delegate = delegate)
        mInterstitialAd?.load()
    }

    override fun clean() {
        mInterstitialAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        if (isAdAvailable) {
            mInterstitialAd?.showFrom(owner)
        }
    }

    override fun tryShowAd(): Boolean {
        return false
    }

    companion object {

        fun with(placementId: Long): InMobiInterstitialAd {
            return InMobiInterstitialAd(placementId, null, null)
        }

        fun with(placementId: Long, adConfig: AdConfig): InMobiInterstitialAd {
            return InMobiInterstitialAd(placementId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: Long = 1234567890L
        const val TAG: String = "InMobiInterstitialAd"
    }
}
