package io.github.saifullah.nurani.ads.vungle

import VungleAdsSDK.VungleRewarded
import VungleAdsSDK.VungleRewardedDelegateProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class VungleRewardedAd(
    placementId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : RewardedAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val placementId: String = placementId
    private var mRewardedAd: VungleRewarded? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mRewardedAd?.canPlayAd() ?: false

    override fun loadAd() {
        if (mRewardedAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!VungleAds.isInitialized()) {
            val adError = AdError(
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
        mRewardedAd = VungleRewarded(placementId = finalPlacementId)
        val delegate = object : NSObject(), VungleRewardedDelegateProtocol {
            override fun rewardedAdDidLoad(rewarded: VungleRewarded) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            override fun rewardedAdDidFailToLoad(rewarded: VungleRewarded, withError: NSError) {
                adStateManager.onAdFailedToLoad(AdError(0, withError.localizedDescription ?: "Unknown error"))
                adLoadListener?.onAdFailedToLoad(AdError(0, withError.localizedDescription ?: "Unknown error"))
                if (adStateManager.shouldPreserveOnFailure) {
                    mRewardedAd = null
                }
            }

            override fun rewardedAdWillPresent(rewarded: VungleRewarded) {}

            override fun rewardedAdDidPresent(rewarded: VungleRewarded) {
                adStateManager.onAdDisplayed()
                adScreenContentCallback?.onAdShowed()
                adScreenContentCallback?.onAdDisplayed()
            }

            override fun rewardedAdDidFailToPresent(rewarded: VungleRewarded, withError: NSError) {
                val adError = AdError(0, withError.localizedDescription ?: "Unknown error")
                adStateManager.onAdFailedToShow(adError)
                adScreenContentCallback?.onAdFailedToShow(adError)
            }

            override fun rewardedAdDidClick(rewarded: VungleRewarded) {
                adStateManager.onAdClicked()
                adScreenContentCallback?.onAdClicked()
            }

            override fun rewardedAdDidClose(rewarded: VungleRewarded) {
                adStateManager.onAdDismissed()
                adScreenContentCallback?.onAdDismissed()
                clean()
            }
            
            override fun rewardedAdDidRewardUser(rewarded: VungleRewarded) {
                println("VungleRewardedAd [iOS]: rewardedAdDidRewardUser delegate method called. userRewardedCallback is $userRewardedCallback")
                userRewardedCallback?.invoke()
            }
            
            override fun rewardedAdDidTrackImpression(rewarded: VungleRewarded) {}
            override fun rewardedAdWillClose(rewarded: VungleRewarded) {}
            override fun rewardedAdWillLeaveApplication(rewarded: VungleRewarded) {}
        }
        adDelegate = delegate
        mRewardedAd?.setDelegate(delegate)
        mRewardedAd?.load(null)
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
            mRewardedAd?.presentWith(owner)
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

        fun with(placementId: String): VungleRewardedAd {
            return VungleRewardedAd(placementId, null, null)
        }

        fun with(placementId: String, adConfig: AdConfig): VungleRewardedAd {
            return VungleRewardedAd(placementId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "R1-5035381"
        const val TAG: String = "VungleRewardedAd"
    }
}
