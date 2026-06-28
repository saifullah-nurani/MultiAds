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
            adListener?.onAdFailedToLoad(adError)
            return
        }
        if (!isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId != 0L) { "placementId must not be 0." }
        }
        destroy()
        if (bannerView == null) {
            val finalPlacementId = if (isTestModeEnabled) testAdUnitId else placementId ?: 0L
            bannerView = IMBanner(frame = platform.CoreGraphics.CGRectZero.readValue(), placementId = finalPlacementId)
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
                    adStateManager?.onAdFailedToLoad(AdError(0, didFailToLoadWithError.toString()))
                    adListener?.onAdFailedToLoad(AdError(0, didFailToLoadWithError.toString()))
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
            val constraints = mutableListOf(
                bannerView!!.bottomAnchor.constraintEqualToAnchor(safeAreaLayoutGuide.bottomAnchor),
                bannerView!!.centerXAnchor.constraintEqualToAnchor(centerXAnchor)
            )
            if (currentAdSize.width > 0) {
                constraints.add(bannerView!!.widthAnchor.constraintEqualToConstant(currentAdSize.width.toDouble()))
            } else {
                constraints.add(bannerView!!.widthAnchor.constraintEqualToAnchor(widthAnchor))
            }
            if (currentAdSize.height > 0) {
                constraints.add(bannerView!!.heightAnchor.constraintEqualToConstant(currentAdSize.height.toDouble()))
            } else {
                constraints.add(bannerView!!.heightAnchor.constraintEqualToConstant(50.0))
            }
            NSLayoutConstraint.activateConstraints(constraints)
        }
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

    fun destroy() {
        bannerView?.removeFromSuperview()
        bannerView = null
        adDelegate = null
        adStateManager?.onDestroy()
        adStateManager = null
    }

    private fun log(msg: String) {
        logger?.d("$bannerTag : $msg")
    }
}
