package io.github.saifullah.nurani.ads.man

import FBAudienceNetwork.FBInterstitialAd
import FBAudienceNetwork.FBInterstitialAdDelegateProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class MetaInterstitialAd(
    placementId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : MetaFullScreenAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val placementId: String = placementId
    private var mInterstitialAd: FBInterstitialAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mInterstitialAd?.isAdValid() ?: false

    override fun loadAd() {
        if (mInterstitialAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (adConfig?.isTestModeEnabled != true) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId.isNotEmpty()) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else placementId
        mInterstitialAd = FBInterstitialAd(placementID = finalPlacementId)
        val delegate = object : NSObject(), FBInterstitialAdDelegateProtocol {
            override fun interstitialAdDidLoad(interstitialAd: FBInterstitialAd) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            override fun interstitialAd(interstitialAd: FBInterstitialAd, didFailWithError: NSError) {
                adStateManager.onAdFailedToLoad(AdError(0, didFailWithError.toString()))
                adLoadListener?.onAdFailedToLoad(AdError(0, didFailWithError.toString()))
                if (adStateManager.shouldPreserveOnFailure) {
                    mInterstitialAd = null
                }
            }

            override fun interstitialAdDidClick(interstitialAd: FBInterstitialAd) {
                adStateManager.onAdClicked()
                adScreenContentCallback?.onAdClicked()
            }

            override fun interstitialAdDidClose(interstitialAd: FBInterstitialAd) {
                adStateManager.onAdDismissed()
                adScreenContentCallback?.onAdDismissed()
                clean()
            }

            override fun interstitialAdWillLogImpression(interstitialAd: FBInterstitialAd) {
                adStateManager.onAdDisplayed()
                adScreenContentCallback?.onAdShowed()
                adScreenContentCallback?.onAdDisplayed()
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
            mInterstitialAd?.showAdFromRootViewController(owner)
        }
    }

    override fun tryShowAd(): Boolean {
        return false
    }

    companion object {

        fun with(placementId: String): MetaInterstitialAd {
            return MetaInterstitialAd(placementId, null, null)
        }

        fun with(placementId: String, adConfig: AdConfig): MetaInterstitialAd {
            return MetaInterstitialAd(placementId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
        const val TAG: String = "MetaInterstitialAd"
    }
}
