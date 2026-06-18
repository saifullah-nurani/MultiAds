package io.github.saifullah.nurani.ads.pangle

import PAGAdSDK.PAGLAppOpenAd
import PAGAdSDK.PAGLAppOpenAdDelegateProtocol
import PAGAdSDK.PAGAppOpenRequest
import PAGAdSDK.PAGAdProtocolProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class PangleAppOpenAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val adUnitId: String = adUnitId
    private var mAppOpenAd: PAGLAppOpenAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mAppOpenAd != null

    override fun loadAd() {
        if (mAppOpenAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            require(adUnitId.isNotEmpty()) { "$TAG: adUnitId must not be empty." }
        }
        val finalAdUnitId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
        PAGLAppOpenAd.loadAdWithSlotID(finalAdUnitId, PAGAppOpenRequest()) { appOpenAd, error ->
            if (error != null || appOpenAd == null) {
                adStateManager.onAdFailedToLoad(AdError(0, error?.localizedDescription ?: "Unknown error"))
                adLoadListener?.onAdFailedToLoad(AdError(0, error?.localizedDescription ?: "Unknown error"))
                if (adStateManager.shouldPreserveOnFailure) {
                    mAppOpenAd = null
                }
            } else {
                mAppOpenAd = appOpenAd
                val delegate = object : NSObject(), PAGLAppOpenAdDelegateProtocol {
                    override fun adDidShow(ad: PAGAdProtocolProtocol) {
                        adStateManager.onAdDisplayed()
                    }
                    override fun adDidClick(ad: PAGAdProtocolProtocol) {
                        adStateManager.onAdClicked()
                    }
                    override fun adDidDismiss(ad: PAGAdProtocolProtocol) {
                        adStateManager.onAdDismissed()
                        clean()
                    }
                }
                adDelegate = delegate
                mAppOpenAd?.setDelegate(delegate)
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }
        }
    }

    override fun clean() {
        mAppOpenAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        if (isAdAvailable) {
            mAppOpenAd?.presentFromRootViewController(owner)
        }
    }

    override fun tryShowAd(): Boolean {
        return false
    }

    companion object {

        fun with(adUnitId: String): PangleAppOpenAd {
            return PangleAppOpenAd(adUnitId, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): PangleAppOpenAd {
            return PangleAppOpenAd(adUnitId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "980088188"
        const val TAG: String = "PangleAppOpenAd"
    }
}
