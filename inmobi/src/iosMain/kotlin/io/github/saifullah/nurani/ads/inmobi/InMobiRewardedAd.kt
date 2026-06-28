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
class InMobiRewardedAd(
    placementId: Long,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : RewardedAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val placementId: Long = placementId
    private var mRewardedAd: IMInterstitial? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mRewardedAd?.isReady() ?: false

    override fun loadAd() {
        if (mRewardedAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!InMobiAds.isInitialized()) {
            val adError = AdError(
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
                    mRewardedAd = null
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

            override fun interstitial(interstitial: IMInterstitial, rewardActionCompletedWithRewards: Map<Any?, *>) {
                userRewardedCallback?.invoke()
            }
        }
        adDelegate = delegate
        if (adConfig?.isTestModeEnabled != true) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId != 0L) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else placementId
        mRewardedAd = IMInterstitial(placementId = finalPlacementId, delegate = delegate)
        mRewardedAd?.load()
    }

    override fun clean() {
        mRewardedAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        showAd(owner, onUserRewarded = {})
    }

    override fun showAd(onUserRewarded: () -> Unit) {
        // Must use showAd(owner) instead
    }

    override fun showAd(owner: UIViewController, onUserRewarded: () -> Unit) {
        if (isAdAvailable) {
            userRewardedCallback = onUserRewarded
            mRewardedAd?.showFrom(owner)
        }
    }

    override fun tryShowAd(): Boolean {
        return false
    }

    companion object {

        fun with(placementId: Long): InMobiRewardedAd {
            return InMobiRewardedAd(placementId, null, null)
        }

        fun with(placementId: Long, adConfig: AdConfig): InMobiRewardedAd {
            return InMobiRewardedAd(placementId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: Long = 10000718552L
        const val TAG: String = "InMobiRewardedAd"
    }
}
