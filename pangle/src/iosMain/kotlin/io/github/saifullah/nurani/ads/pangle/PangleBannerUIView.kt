package io.github.saifullah.nurani.ads.pangle

import PAGAdSDK.PAGBannerAd
import PAGAdSDK.PAGBannerAdDelegateProtocol
import PAGAdSDK.PAGBannerRequest
import PAGAdSDK.PAGAdProtocolProtocol
import PAGAdSDK.kPAGBannerSize300x250
import PAGAdSDK.kPAGBannerSize320x50
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
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIResponder
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class PangleBannerUIView : UIView(frame = CGRectZero.readValue()) {

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

    private var adUnitId: String? = null
    private var bannerAd: PAGBannerAd? = null
    private var bannerAdStrategy: BannerAd<AdSize>? = null
    private var adStateManager: AdStateManager? = null
    private var adDelegate: NSObject? = null
    private val bannerTag = "PangleBannerUIView"
    private var requestTag: String? = null

    fun setAdUnitId(id: String) {
        adUnitId = id
    }

    fun setBannerAd(ad: BannerAd<AdSize>) {
        currentAdSize = ad.getSize()
        bannerAdStrategy = ad
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

    private val testAdUnitId = "980099802"
    private val testAdUnitId300x250 = "983240210"

    private fun loadAdInternally() {
        if (!PangleAds.isInitialized()) {
            val adError = AdError(
                code = 0,
                message = "Pangle SDK is not initialized yet."
            )
            adStateManager?.onAdFailedToLoad(adError)
            adListener?.onAdFailedToLoad(adError)
            return
        }
        if (!isTestModeEnabled) {
            checkNotNull(adUnitId) { "adUnitId must be set." }
            require(adUnitId!!.isNotEmpty()) { "adUnitId must not be empty." }
        }
        destroy()
        
        val isMediumRectangle = currentAdSize.height >= 250
        val bannerSize = if (isMediumRectangle) {
            kPAGBannerSize300x250.readValue()
        } else {
            kPAGBannerSize320x50.readValue()
        }
        
        val finalAdUnitId = if (isTestModeEnabled) {
            if (isMediumRectangle) testAdUnitId300x250 else testAdUnitId
        } else {
            adUnitId ?: ""
        }
        
        val request = PAGBannerRequest.requestWithBannerSize(bannerSize)
        PAGBannerAd.loadAdWithSlotID(finalAdUnitId, request) { ad, error ->
            dispatch_async(dispatch_get_main_queue()) {
                if (error != null || ad == null) {
                    log("Load failed ${error?.localizedDescription}")
                    adStateManager?.onAdFailedToLoad(AdError(0, error?.localizedDescription ?: "Unknown error"))
                    adListener?.onAdFailedToLoad(AdError(0, error?.localizedDescription ?: "Unknown error"))
                    if (!keepAdSlot) hidden = true
                } else {
                    log("Banner loaded")
                    bannerAd = ad
                    bannerAd?.rootViewController = findViewController() ?: UIApplication.sharedApplication.keyWindow?.rootViewController
                    val delegate = object : NSObject(), PAGBannerAdDelegateProtocol {
                        override fun adDidShow(ad: PAGAdProtocolProtocol) {
                            adStateManager?.onAdDisplayed()
                        }
    
                        override fun adDidClick(ad: PAGAdProtocolProtocol) {
                            adStateManager?.onAdClicked()
                        }
    
                        override fun adDidDismiss(ad: PAGAdProtocolProtocol) {
                            adStateManager?.onAdDismissed()
                        }
                    }
                    adDelegate = delegate
                    bannerAd?.setDelegate(delegate)
                    
                    bannerAd?.bannerView?.let { view ->
                        if (view != null && view.translatesAutoresizingMaskIntoConstraints) {
                            view.translatesAutoresizingMaskIntoConstraints = false
                            addSubview(view)
                            val constraints = mutableListOf(
                                view.bottomAnchor.constraintEqualToAnchor(safeAreaLayoutGuide.bottomAnchor),
                                view.centerXAnchor.constraintEqualToAnchor(centerXAnchor)
                            )
                            if (currentAdSize.width > 0) {
                                constraints.add(view.widthAnchor.constraintEqualToConstant(currentAdSize.width.toDouble()))
                            } else {
                                constraints.add(view.widthAnchor.constraintEqualToAnchor(widthAnchor))
                            }
                            if (currentAdSize.height > 0) {
                                constraints.add(view.heightAnchor.constraintEqualToConstant(currentAdSize.height.toDouble()))
                            } else {
                                constraints.add(view.heightAnchor.constraintEqualToConstant(50.0))
                            }
                            NSLayoutConstraint.activateConstraints(constraints)
                        }
                    }
                    
                    adStateManager?.onAdLoaded()
                    adListener?.onAdLoaded()
                    fadeIn()
                }
            }
        }
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()
        if (window != null && bannerAd != null) {
            bannerAd?.rootViewController = findViewController()
        }
    }

    private fun fadeIn() {
        hidden = false
        alpha = 1.0
    }

    fun destroy() {
        bannerAd?.bannerView?.removeFromSuperview()
        bannerAd = null
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
