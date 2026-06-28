package io.github.saifullah.nurani.ads.man

import FBAudienceNetwork.FBAdView
import FBAudienceNetwork.FBAdViewDelegateProtocol
import FBAudienceNetwork.kFBAdSizeHeight50Banner
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
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIResponder
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class MetaBannerUIView : UIView(frame = CGRectZero.readValue()) {

    init {
        backgroundColor = platform.UIKit.UIColor.clearColor
    }

    var logger: AdLogger? = null
    var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = AdFailedRetryRule.exponentialDefault()
    var keepAdSlot = true
    var isTestModeEnabled = false
    var adListener: BannerAdListener? = null
    private var currentAdSize: AdSize = AdSize.BANNER
    val adSize get() = currentAdSize

    private var placementId: String? = null
    private var bannerView: FBAdView? = null
    private var bannerAd: BannerAd<AdSize>? = null
    private var adStateManager: AdStateManager? = null
    private var adDelegate: NSObject? = null
    private val bannerTag = "MetaBannerUIView"
    private var requestTag: String? = null

    fun setPlacementId(id: String) {
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

    private val testAdUnitId = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"

    private fun loadAdInternally() {
        if (!isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId!!.isNotEmpty()) { "placementId must not be empty." }
        }
        destroy()
        if (bannerView == null) {
            val finalPlacementId = if (isTestModeEnabled) testAdUnitId else placementId ?: ""
            bannerView = FBAdView(
                placementID = finalPlacementId,
                adSize = kFBAdSizeHeight50Banner.readValue(),
                rootViewController = findViewController() ?: UIApplication.sharedApplication.keyWindow?.rootViewController
            )
            bannerView!!.translatesAutoresizingMaskIntoConstraints = false
            val delegate = object : NSObject(), FBAdViewDelegateProtocol {
                override fun adViewDidLoad(adView: FBAdView) {
                    log("Banner loaded")
                    adStateManager?.onAdLoaded()
                    adListener?.onAdLoaded()
                    fadeIn()
                }

                override fun adView(adView: FBAdView, didFailWithError: NSError) {
                    log("Load failed ${didFailWithError.localizedDescription}")
                    adStateManager?.onAdFailedToLoad(AdError(0, didFailWithError.toString()))
                    adListener?.onAdFailedToLoad(AdError(0, didFailWithError.toString()))
                    if (!keepAdSlot) hidden = true
                }

                override fun adViewDidClick(adView: FBAdView) {
                    adStateManager?.onAdClicked()
                }

                override fun adViewWillLogImpression(adView: FBAdView) {
                    adStateManager?.onAdDisplayed()
                }
            }
            adDelegate = delegate
            bannerView!!.delegate = delegate
            
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
        bannerView!!.loadAd()
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

    private fun findViewController(): UIViewController? {
        var responder: UIResponder? = this
        while (responder != null) {
            if (responder is UIViewController) return responder
            responder = responder.nextResponder
        }
        return UIApplication.sharedApplication.keyWindow?.rootViewController
    }
}
