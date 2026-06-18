package io.github.saifullah.nurani.ads.applovin

import AppLovinSDK.MAAdDelegateProtocol
import AppLovinSDK.MAAd
import AppLovinSDK.MAError
import AppLovinSDK.MAAppOpenAd
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import io.github.saifullah.nurani.ads.core.AdError

@OptIn(ExperimentalForeignApi::class)
class AppLovinAppOpenAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG), AppOpenAd {

    private val adUnitId: String = adUnitId
    private var mAppOpenAd: MAAppOpenAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mAppOpenAd?.isReady() ?: false

    override fun loadAd() {
        if (mAppOpenAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (adConfig?.isTestModeEnabled != true) {
            require(adUnitId.isNotEmpty()) { "$TAG: adUnitId must not be empty." }
        }
        val finalAdUnitId = if (adConfig?.isTestModeEnabled == true) TEST_AD_UNIT_ID else adUnitId
        mAppOpenAd = MAAppOpenAd(finalAdUnitId)
        val delegate = object : NSObject(), MAAdDelegateProtocol {
            override fun didLoadAd(ad: MAAd) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }

            override fun didFailToLoadAdForAdUnitIdentifier(adUnitIdentifier: String, withError: MAError) {
                adStateManager.onAdFailedToLoad(AdError(0, withError.toString()))
                adLoadListener?.onAdFailedToLoad(AdError(0, withError.toString()))
                if (adStateManager.shouldPreserveOnFailure) {
                    mAppOpenAd = null
                }
            }

            override fun didDisplayAd(ad: MAAd) {
                adStateManager.onAdDisplayed()
            }

            override fun didHideAd(ad: MAAd) {
                adStateManager.onAdDismissed()
                clean()
            }

            override fun didClickAd(ad: MAAd) {
                adStateManager.onAdClicked()
            }

            override fun didFailToDisplayAd(ad: MAAd, withError: MAError) {
                adStateManager.onAdFailedToShow(AdError(0, withError.toString()))
            }
        }
        adDelegate = delegate
        mAppOpenAd?.delegate = delegate
        mAppOpenAd?.loadAd()
    }

    override fun clean() {
        mAppOpenAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        if (isAdAvailable) {
            mAppOpenAd?.showAdForPlacement(null)
        }
    }

    override fun tryShowAd(): Boolean {
        if (isAdAvailable) {
            mAppOpenAd?.showAdForPlacement(null)
            return true
        }
        return false
    }

    companion object {

        fun with(adUnitId: String): AppLovinAppOpenAd {
            return AppLovinAppOpenAd(adUnitId, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): AppLovinAppOpenAd {
            return AppLovinAppOpenAd(adUnitId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "YOUR_MAX_TEST_AD_UNIT_ID"
        const val TAG: String = "AppLovinAppOpenAd"
    }
}
