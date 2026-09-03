package io.github.saifullah.nurani.ads.inmobi

import InMobiSDK.IMBanner
import InMobiSDK.IMBannerDelegateProtocol
import InMobiSDK.IMRequestStatus
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.AdStateManager
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIResponder
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class InMobiBannerUIView : UIView(frame = CGRectZero.readValue()) {

    init {
        backgroundColor = UIColor.clearColor
        opaque = false
    }

    var logger: AdLogger? = null
    var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = AdFailedRetryRule.exponentialDefault()
    var keepAdSlot = true
    var isTestModeEnabled = false
    var adListener: BannerAdListener? = null
    private var currentAdSize: AdSize = AdSize.BANNER
    val adSize get() = currentAdSize

    private var placementId: Long? = null
    private var bannerView: IMBanner? = null
    private var bannerAd: BannerAd<AdSize>? = null
    private var adStateManager: AdStateManager? = null
    private var adDelegate: NSObject? = null
    private val bannerTag = "InMobiBannerUIView"
    private var requestTag: String? = null

    fun setPlacementId(id: Long) {
        placementId = id
    }

    fun setBannerAd(ad: BannerAd<AdSize>) {
        currentAdSize = ad.getSize()
        bannerAd = ad
    }

    fun setRequestTag(tag: String?) {
        requestTag = tag
    }

    fun loadAd() {
        if (!InMobiAds.isInitialized()) {
            InMobiAds.runWhenInitialized {
                loadAd()
            }
            return
        }
        if (adStateManager == null) {
            adStateManager = AdStateManager(
                reloadPolicies,
                retryRule,
                AdRefreshStrategy.disable(),
                null,
                Scheduler(),
                requestTag ?: bannerTag
            ) {
                loadAdInternally()
            }
        }
        adStateManager!!.loadAd()
    }

    private val testAdUnitId = 10000718551L

    private fun loadAdInternally() {
        if (!InMobiAds.isInitialized()) {
            val adError = AdError(
                code = 0,
                message = "InMobi SDK is not initialized yet."
            )
            adStateManager?.onAdFailedToLoad(adError)
            if (adStateManager?.isRetryingAdFailedLoad != true) {
                adListener?.onAdFailedToLoad(adError)
            }
            return
        }
        if (!isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId != 0L) { "placementId must not be 0." }
        }
        destroyBanner(resetStateManager = false)
        val (bannerWidth, bannerHeight) = when {
            currentAdSize.height >= 250 -> 300.0 to 250.0
            currentAdSize.height >= 100 -> 320.0 to 100.0
            currentAdSize.height >= 90 && currentAdSize.width >= 728 -> 728.0 to 90.0
            currentAdSize.height >= 60 && currentAdSize.width >= 468 -> 468.0 to 60.0
            currentAdSize.width > 0 && currentAdSize.height > 0 -> currentAdSize.width.toDouble() to currentAdSize.height.toDouble()
            else -> 320.0 to 50.0
        }
        val bannerFrame = CGRectMake(0.0, 0.0, bannerWidth, bannerHeight)

        if (bannerView == null) {
            val finalPlacementId = if (isTestModeEnabled) (placementId?.takeIf { it != 0L } ?: testAdUnitId) else placementId ?: 0L
            bannerView = IMBanner(frame = bannerFrame, placementId = finalPlacementId)
            bannerView!!.translatesAutoresizingMaskIntoConstraints = false
            val delegate = object : NSObject(), IMBannerDelegateProtocol {
                override fun bannerDidFinishLoading(banner: IMBanner) {
                    log("Banner loaded")
                    adStateManager?.onAdLoaded()
                    adListener?.onAdLoaded()
                    fadeIn()
                }

                override fun banner(banner: IMBanner, didFailToLoadWithError: IMRequestStatus) {
                    log("Load failed ${didFailToLoadWithError.toString()}")
                    val error = AdError(0, didFailToLoadWithError.toString())
                    adStateManager?.onAdFailedToLoad(error)
                    if (adStateManager?.isRetryingAdFailedLoad != true) {
                        adListener?.onAdFailedToLoad(error)
                    }
                    if (!keepAdSlot) hidden = true
                }

                override fun bannerDidPresentScreen(banner: IMBanner) {
                    adStateManager?.onAdDisplayed()
                }

                override fun bannerDidDismissScreen(banner: IMBanner) {
                    adStateManager?.onAdDismissed()
                }

                override fun banner(banner: IMBanner, didInteractWithParams: Map<Any?, *>?) {
                    adStateManager?.onAdClicked()
                }
            }
            adDelegate = delegate
            bannerView!!.setDelegate(delegate)
            
            addSubview(bannerView!!)
            NSLayoutConstraint.activateConstraints(
                listOf(
                    bannerView!!.centerXAnchor.constraintEqualToAnchor(centerXAnchor),
                    bannerView!!.centerYAnchor.constraintEqualToAnchor(centerYAnchor),
                    bannerView!!.widthAnchor.constraintEqualToConstant(bannerWidth),
                    bannerView!!.heightAnchor.constraintEqualToConstant(bannerHeight)
                )
            )
        }
        bannerView!!.setFrame(bannerFrame)
        bannerView!!.load()
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()
    }

    private fun fadeIn() {
        if (!keepAdSlot) alpha = 0.0
        hidden = false
        if (!keepAdSlot) {
            UIView.animateWithDuration(
                duration = 0.25,
                animations = { this.alpha = 1.0 }
            )
        }
    }

    fun resume() {
        adStateManager?.onStart()
    }

    fun pause() {
        adStateManager?.onStop()
    }

    fun destroy() {
        destroyBanner(resetStateManager = true)
    }

    private fun destroyBanner(resetStateManager: Boolean) {
        bannerView?.removeFromSuperview()
        bannerView = null
        adDelegate = null
        if (resetStateManager) {
            adStateManager?.onDestroy()
            adStateManager = null
        }
    }

    private fun log(msg: String) {
        logger?.d("$bannerTag : $msg")
    }
}
