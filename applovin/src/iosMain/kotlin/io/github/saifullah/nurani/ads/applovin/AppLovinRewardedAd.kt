package io.github.saifullah.nurani.ads.applovin

import AppLovinSDK.MAAdDelegateProtocol
import AppLovinSDK.MAAd
import AppLovinSDK.MAError
import AppLovinSDK.MARewardedAd
import AppLovinSDK.MARewardedAdDelegateProtocol
import AppLovinSDK.MAReward
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import io.github.saifullah.nurani.ads.core.AdError

@OptIn(ExperimentalForeignApi::class)
class AppLovinRewardedAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : RewardedAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val adUnitId: String = adUnitId
    private var mRewardedAd: MARewardedAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mRewardedAd?.isReady() ?: false

    override fun loadAd() {
        if (mRewardedAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (adConfig?.isTestModeEnabled != true) {
            require(adUnitId.isNotEmpty()) { "$TAG: adUnitId must not be empty." }
        }
        val finalAdUnitId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else adUnitId
        mRewardedAd = MARewardedAd.sharedWithAdUnitIdentifier(finalAdUnitId)
        val delegate = object : NSObject(), MARewardedAdDelegateProtocol {
            override fun didLoadAd(ad: MAAd) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            override fun didFailToLoadAdForAdUnitIdentifier(adUnitIdentifier: String, withError: MAError) {
                adStateManager.onAdFailedToLoad(AdError(0, withError.toString()))
                adLoadListener?.onAdFailedToLoad(AdError(0, withError.toString()))
                if (adStateManager.shouldPreserveOnFailure) {
                    mRewardedAd = null
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

            override fun didRewardUserForAd(ad: MAAd, withReward: MAReward) {
                println("AppLovinRewardedAd [iOS]: didRewardUserForAd delegate method called. userRewardedCallback is $userRewardedCallback")
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
        if (isAdAvailable) {
            userRewardedCallback = onUserRewarded
            mRewardedAd?.showAdForPlacement(null)
        }
    }

    override fun showAd(owner: UIViewController, onUserRewarded: () -> Unit) {
        if (isAdAvailable) {
            userRewardedCallback = onUserRewarded
            mRewardedAd?.showAdForPlacement(null)
        }
    }

    override fun tryShowAd(): Boolean {
        if (isAdAvailable) {
            mRewardedAd?.showAdForPlacement(null)
            return true
        }
        return false
    }

    companion object {

        fun with(adUnitId: String): AppLovinRewardedAd {
            return AppLovinRewardedAd(adUnitId, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): AppLovinRewardedAd {
            return AppLovinRewardedAd(adUnitId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "YOUR_MAX_TEST_AD_UNIT_ID"
        const val TAG: String = "AppLovinRewardedAd"
    }
}
