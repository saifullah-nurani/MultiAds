package io.github.saifullah.nurani.ads.man

import FBAudienceNetwork.FBRewardedVideoAd
import FBAudienceNetwork.FBRewardedVideoAdDelegateProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class MetaRewardedAd(
    placementId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : MetaRewardedAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val placementId: String = placementId
    private var mRewardedAd: FBRewardedVideoAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mRewardedAd?.isAdValid() ?: false

    override fun loadAd() {
        if (mRewardedAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (adConfig?.isTestModeEnabled != true) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId.isNotEmpty()) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else placementId
        mRewardedAd = FBRewardedVideoAd(placementID = finalPlacementId)
        val delegate = object : NSObject(), FBRewardedVideoAdDelegateProtocol {
            override fun rewardedVideoAdDidLoad(rewardedVideoAd: FBRewardedVideoAd) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            override fun rewardedVideoAd(rewardedVideoAd: FBRewardedVideoAd, didFailWithError: NSError) {
                adStateManager.onAdFailedToLoad(AdError(0, didFailWithError.toString()))
                adLoadListener?.onAdFailedToLoad(AdError(0, didFailWithError.toString()))
                if (adStateManager.shouldPreserveOnFailure) {
                    mRewardedAd = null
                }
            }

            override fun rewardedVideoAdDidClick(rewardedVideoAd: FBRewardedVideoAd) {
                adStateManager.onAdClicked()
                adScreenContentCallback?.onAdClicked()
            }

            override fun rewardedVideoAdDidClose(rewardedVideoAd: FBRewardedVideoAd) {
                adStateManager.onAdDismissed()
                adScreenContentCallback?.onAdDismissed()
                clean()
            }

            override fun rewardedVideoAdWillLogImpression(rewardedVideoAd: FBRewardedVideoAd) {
                adStateManager.onAdDisplayed()
                adScreenContentCallback?.onAdShowed()
                adScreenContentCallback?.onAdDisplayed()
            }
            
            override fun rewardedVideoAdVideoComplete(rewardedVideoAd: FBRewardedVideoAd) {
                println("MetaRewardedAd [iOS]: rewardedVideoAdVideoComplete delegate method called. userRewardedCallback is $userRewardedCallback")
                userRewardedCallback?.invoke()
            }
        }
        adDelegate = delegate
        mRewardedAd?.delegate = delegate
        mRewardedAd?.loadAd()
    }

    override fun clean() {
        mRewardedAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        showAd(owner, onUserRewarded = {})
    }

    override fun showAd(onUserRewarded: () -> Unit) {
        val root = platform.UIKit.UIApplication.sharedApplication.keyWindow?.rootViewController
        if (root != null && isAdAvailable) {
            showAd(root, onUserRewarded)
        }
    }

    override fun showAd(owner: UIViewController, onUserRewarded: () -> Unit) {
        if (isAdAvailable) {
            userRewardedCallback = onUserRewarded
            mRewardedAd?.showAdFromRootViewController(owner)
        }
    }

    override fun tryShowAd(): Boolean {
        val root = platform.UIKit.UIApplication.sharedApplication.keyWindow?.rootViewController
        if (root != null && isAdAvailable) {
            showAd(root, {})
            return true
        }
        return false
    }

    companion object {

        fun with(placementId: String): MetaRewardedAd {
            return MetaRewardedAd(placementId, null, null)
        }

        fun with(placementId: String, adConfig: AdConfig): MetaRewardedAd {
            return MetaRewardedAd(placementId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "VID_HD_9_16_39S_APP_INSTALL#YOUR_PLACEMENT_ID"
        const val TAG: String = "MetaRewardedAd"
    }
}
