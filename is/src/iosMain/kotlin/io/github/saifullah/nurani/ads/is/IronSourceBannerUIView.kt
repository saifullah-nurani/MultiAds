package io.github.saifullah.nurani.ads.`is`

import IronSource.LPMAdInfo
import IronSource.LPMBannerAdView
import IronSource.LPMBannerAdViewDelegateProtocol
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.AdStateManager
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.BannerAdListener
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIResponder
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IronSourceBannerUIView : UIView(frame = CGRectZero.readValue()) {

    var logger: AdLogger? = null
    var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = AdFailedRetryRule.exponentialDefault()
    var keepAdSlot = true
    var isTestModeEnabled = false
    var adListener: BannerAdListener? = null
    private var currentAdSize: AdSize = AdSize.BANNER
    val adSize get() = currentAdSize

    private var placementId: String? = null
    private var bannerView: LPMBannerAdView? = null
    private var bannerAd: BannerAd<AdSize>? = null
    private var adStateManager: AdStateManager? = null
    private var adDelegate: NSObject? = null
    private var pendingLoad = false
    private val bannerTag = "IronSourceBannerUIView"

    fun setPlacementId(id: String) {
        placementId = id
    }

    fun setAdUnitId(id: String) {
        placementId = id
    }

    fun setBannerAd(ad: BannerAd<AdSize>) {
        currentAdSize = ad.getSize()
        bannerAd = ad
    }

    fun loadAd() {
        if (findViewController() == null) {
            pendingLoad = true
            return
        }
        pendingLoad = false

        if (adStateManager == null) {
            adStateManager = AdStateManager(
                reloadPolicies,
                retryRule,
                AdRefreshStrategy.disable(),
                null,
                Scheduler(),
                bannerTag
            ) {
                loadAdInternally()
            }
        }
        adStateManager!!.loadAd()
    }

    private val testAdUnitId = "24965124"

    private fun loadAdInternally() {
        if (!IronSourceAds.isInitialized()) {
            IronSourceAds.runWhenInitialized {
                loadAdInternally()
            }
            return
        }

        if (!isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId!!.isNotEmpty()) { "placementId must not be empty." }
        }
        destroyBanner(resetState = false)
        
        val viewController = findViewController() ?: UIApplication.sharedApplication.keyWindow?.rootViewController
        val finalPlacementId = if (isTestModeEnabled) testAdUnitId else placementId
        if (viewController != null && finalPlacementId != null) {
            val isBannerSize = when {
                currentAdSize.height >= 250 -> IronSource.LPMAdSize.mediumRectangleSize()
                currentAdSize.height >= 90 -> IronSource.LPMAdSize.leaderBoardSize()
                else -> IronSource.LPMAdSize.bannerSize()
            }
            val builder = IronSource.LPMBannerAdViewConfigBuilder()
            builder.setWithAdSize(isBannerSize)
            val config = builder.build()
            bannerView = LPMBannerAdView(adUnitId = finalPlacementId, config = config)
            bannerView?.translatesAutoresizingMaskIntoConstraints = false
            val delegate = object : NSObject(), LPMBannerAdViewDelegateProtocol {
                override fun didLoadAdWithAdInfo(adInfo: LPMAdInfo) {
                    log("Banner loaded")
                    adStateManager?.onAdLoaded()
                    adListener?.onAdLoaded()
                    fadeIn()
                }

                override fun didFailToLoadAdWithAdUnitId(adUnitId: String, error: NSError) {
                    log("Load failed ${error.localizedDescription}")
                    adStateManager?.onAdFailedToLoad(AdError(0, error.localizedDescription ?: "Unknown error"))
                    adListener?.onAdFailedToLoad(AdError(0, error.localizedDescription ?: "Unknown error"))
                    if (!keepAdSlot) hidden = true
                }

                override fun didClickAdWithAdInfo(adInfo: LPMAdInfo) {
                    adStateManager?.onAdClicked()
                    adListener?.onAdClicked()
                }

                override fun didDisplayAdWithAdInfo(adInfo: LPMAdInfo) {
                    adStateManager?.onAdDisplayed()
                    adListener?.onAdDisplayed()
                }
                
                override fun didFailToDisplayAdWithAdInfo(adInfo: LPMAdInfo, error: NSError) {
                    val adError = AdError(0, error.localizedDescription ?: "Unknown error")
                    adStateManager?.onAdFailedToShow(adError)
                    adListener?.onAdFailedToShow(adError)
                }
                
                override fun didLeaveAppWithAdInfo(adInfo: LPMAdInfo) {}
                override fun didExpandAdWithAdInfo(adInfo: LPMAdInfo) {}
                override fun didCollapseAdWithAdInfo(adInfo: LPMAdInfo) {}
            }
            adDelegate = delegate
            bannerView?.setDelegate(delegate)
            
            bannerView?.let {
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
            bannerView?.loadAdWithViewController(viewController)
        } else {
            pendingLoad = true
        }
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()
        if (window != null && pendingLoad) {
            loadAd()
        }
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
        destroyBanner(resetState = true)
    }

    private fun destroyBanner(resetState: Boolean) {
        if (bannerView != null) {
            bannerView?.destroy()
            bannerView?.removeFromSuperview()
            bannerView = null
        }
        adDelegate = null
        pendingLoad = false
        if (resetState) {
            adStateManager?.onDestroy()
            adStateManager = null
        }
    }

    private fun log(msg: String) {
        logger?.d("$bannerTag : $msg")
    }

    private fun findViewController(): UIViewController? {
        var responder: UIResponder? = this
        while (responder != null) {
            if (responder is UIViewController) return responder
            responder = responder.nextResponder
        }
        return UIApplication.sharedApplication.keyWindow?.rootViewController
    }
}
