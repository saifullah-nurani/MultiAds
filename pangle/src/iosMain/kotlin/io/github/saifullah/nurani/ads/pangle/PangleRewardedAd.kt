package io.github.saifullah.nurani.ads.pangle

import PAGAdSDK.PAGRewardedAd
import PAGAdSDK.PAGRewardedAdDelegateProtocol
import PAGAdSDK.PAGRewardedRequest
import PAGAdSDK.PAGRewardModel
import PAGAdSDK.PAGAdProtocolProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class PangleRewardedAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : RewardedAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val adUnitId: String = adUnitId
    private var mRewardedAd: PAGRewardedAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mRewardedAd != null

    override fun loadAd() {
        if (mRewardedAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!PangleAds.isInitialized()) {
            val adError = AdError(
                code = 0,
                message = "Pangle SDK is not initialized yet."
            )
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            return
        }
        if (!adConfig.isTestModeEnabled) {
            require(adUnitId.isNotEmpty()) { "$TAG: adUnitId must not be empty." }
        }
        val finalAdUnitId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
        PAGRewardedAd.loadAdWithSlotID(finalAdUnitId, PAGRewardedRequest()) { rewardedAd, error ->
            if (error != null || rewardedAd == null) {
                adStateManager.onAdFailedToLoad(AdError(0, error?.localizedDescription ?: "Unknown error"))
                adLoadListener?.onAdFailedToLoad(AdError(0, error?.localizedDescription ?: "Unknown error"))
                if (adStateManager.shouldPreserveOnFailure) {
                    mRewardedAd = null
                }
            } else {
                mRewardedAd = rewardedAd
                val delegate = object : NSObject(), PAGRewardedAdDelegateProtocol {
                    override fun adDidShow(ad: PAGAdProtocolProtocol) {
                        adStateManager.onAdDisplayed()
                        adScreenContentCallback?.onAdShowed()
                        adScreenContentCallback?.onAdDisplayed()
                    }
                    override fun adDidClick(ad: PAGAdProtocolProtocol) {
                        adStateManager.onAdClicked()
                        adScreenContentCallback?.onAdClicked()
                    }
                    override fun adDidDismiss(ad: PAGAdProtocolProtocol) {
                        adStateManager.onAdDismissed()
                        adScreenContentCallback?.onAdDismissed()
                        clean()
                    }
                    override fun rewardedAd(rewardedAd: PAGRewardedAd, userDidEarnReward: PAGRewardModel) {
                        println("PangleRewardedAd [iOS]: rewardedAd delegate method called. userRewardedCallback is $userRewardedCallback")
                        userRewardedCallback?.invoke()
                    }
                    override fun rewardedAd(rewardedAd: PAGRewardedAd, userEarnRewardFailWithError: NSError) {}
                }
                adDelegate = delegate
                mRewardedAd?.setDelegate(delegate)
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }
        }
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
            mRewardedAd?.presentFromRootViewController(owner)
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

        fun with(adUnitId: String): PangleRewardedAd {
            return PangleRewardedAd(adUnitId, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): PangleRewardedAd {
            return PangleRewardedAd(adUnitId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "980088192"
        const val TAG: String = "PangleRewardedAd"
    }
}
