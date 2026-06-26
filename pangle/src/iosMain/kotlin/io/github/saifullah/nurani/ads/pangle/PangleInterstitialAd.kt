package io.github.saifullah.nurani.ads.pangle

import PAGAdSDK.PAGLInterstitialAd
import PAGAdSDK.PAGLInterstitialAdDelegateProtocol
import PAGAdSDK.PAGInterstitialRequest
import PAGAdSDK.PAGAdProtocolProtocol
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class PangleInterstitialAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private val adUnitId: String = adUnitId
    private var mInterstitialAd: PAGLInterstitialAd? = null
    private var adDelegate: NSObject? = null

    override val isAdAvailable: Boolean get() = mInterstitialAd != null

    override fun loadAd() {
        if (mInterstitialAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!PangleAds.isInitialized()) {
            val adError = io.github.saifullah.nurani.ads.core.AdError(
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
        PAGLInterstitialAd.loadAdWithSlotID(finalAdUnitId, PAGInterstitialRequest()) { interstitialAd, error ->
            if (error != null || interstitialAd == null) {
                adStateManager.onAdFailedToLoad(AdError(0, error?.localizedDescription ?: "Unknown error"))
                adLoadListener?.onAdFailedToLoad(AdError(0, error?.localizedDescription ?: "Unknown error"))
                if (adStateManager.shouldPreserveOnFailure) {
                    mInterstitialAd = null
                }
            } else {
                mInterstitialAd = interstitialAd
                val delegate = object : NSObject(), PAGLInterstitialAdDelegateProtocol {
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
                mInterstitialAd?.setDelegate(delegate)
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
            }
        }
    }

    override fun clean() {
        mInterstitialAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        if (isAdAvailable) {
            mInterstitialAd?.presentFromRootViewController(owner)
        }
    }

    override fun tryShowAd(): Boolean {
        return false
    }

    companion object {

        fun with(adUnitId: String): PangleInterstitialAd {
            return PangleInterstitialAd(adUnitId, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): PangleInterstitialAd {
            return PangleInterstitialAd(adUnitId, null, adConfig)
        }

        const val TEST_AD_UNIT_ID: String = "980088188"
        const val TAG: String = "PangleInterstitialAd"
    }
}
