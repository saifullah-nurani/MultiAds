package io.github.saifullah.nurani.ads.ironsource

import IronSource.LPMAdInfo
import IronSource.LPMRewardedAd
import IronSource.LPMReward
import IronSource.LPMRewardedAdDelegateProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IronSourceRewardedAd(
    placementName: String?,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : RewardedAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val placementName: String? = placementName
    private var mRewardedAd: LPMRewardedAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mRewardedAd?.isAdReady() ?: false

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
            checkNotNull(placementName) { "placementName must be set." }
            require(placementName.isNotEmpty()) { "placementName must not be empty." }
        }
        val finalPlacementName = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else placementName
        finalPlacementName?.let { 
            mRewardedAd = LPMRewardedAd(adUnitId = it)
            val delegate = object : NSObject(), LPMRewardedAdDelegateProtocol {
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
                
                override fun didRewardAdWithAdInfo(adInfo: LPMAdInfo, reward: LPMReward) {
                    userRewardedCallback?.invoke()
                }

                override fun didChangeAdInfo(adInfo: LPMAdInfo) {}
            }
            adDelegate = delegate
            mRewardedAd?.setDelegate(delegate)
            mRewardedAd?.loadAd()
        }
    }

    override fun clean() {
        mRewardedAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        showAd(owner, onUserRewarded = {})
    }

    override fun showAd(onUserRewarded: () -> Unit) {}

    override fun showAd(owner: UIViewController, onUserRewarded: () -> Unit) {
        if (isAdAvailable) {
            userRewardedCallback = onUserRewarded
            mRewardedAd?.showAdWithViewController(owner, placementName = null)
        }
    }

    override fun tryShowAd(): Boolean {
        return false
    }

    companion object {

        fun with(placementName: String?): IronSourceRewardedAd {
            return IronSourceRewardedAd(placementName, null, null)
        }

        fun with(placementName: String?, adConfig: AdConfig): IronSourceRewardedAd {
            return IronSourceRewardedAd(placementName, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "24965128"
        const val TAG: String = "IronSourceRewardedAd"
    }
}
