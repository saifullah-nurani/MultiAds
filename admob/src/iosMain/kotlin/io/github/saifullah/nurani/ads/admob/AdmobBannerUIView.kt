package io.github.saifullah.nurani.ads.admob

import GoogleMobileAds.GADAdSize
import GoogleMobileAds.GADAdSizeBanner
import GoogleMobileAds.GADAdSizeFromCGSize
import GoogleMobileAds.GADBannerView
import GoogleMobileAds.GADBannerViewDelegateProtocol
import GoogleMobileAds.GADRequest
import io.github.saifullah.nurani.ads.admob.utils.adErrorFrom
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.AdStateManager
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSError
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIResponder
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class AdmobBannerUIView : UIView(frame = CGRectZero.readValue()) {
    
    init {
        backgroundColor = UIColor.clearColor
        opaque = false
    }

    private val testAdUnitId = "ca-app-pub-3940256099942544/2435281174"
    private val bannerTag = "AdmobBannerUIView"
    private var bannerView: GADBannerView? = null
    private var bannerAd: BannerAd<CValue<GADAdSize>>? = null
    private var currentAdSize: CValue<GADAdSize> = GADAdSizeBanner.readValue()
    val adSize get() = currentAdSize
    private var adRequest: GADRequest = GADRequest()
    private var adUnitId: String? = null
    private var adStateManager: AdStateManager? = null
    var logger: AdLogger? = null
    var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = AdFailedRetryRule.exponentialDefault()
    var keepAdSlot = true
    var isTestModeEnabled = false
    var adListener: BannerAdListener? = null
    private var requestTag: String? = null

    fun setAdUnitId(id: String) {
        adUnitId = id
    }

    fun setBannerAd(ad: BannerAd<AdSize>) {
        bannerAd = ad.mapToBannerAd {
            GADAdSizeFromCGSize(
                CGSizeMake(
                    it.width.toDouble(),
                    it.height.toDouble()
                )
            )
        }
    }

    fun setRequestTag(tag: String?) {
        requestTag = tag
    }

    fun loadAd(request: GADRequest = GADRequest()) {

        adRequest = request

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

    private fun loadAdInternally() {

        if (!isTestModeEnabled) {
            checkNotNull(adUnitId) { "adUnitId must be set." }
        }
        destroy()
        if (bannerView == null) {

            currentAdSize = bannerAd?.getSize() ?: GADAdSizeBanner.readValue()

            bannerView = GADBannerView(adSize = currentAdSize)

            bannerView!!.adUnitID = if (isTestModeEnabled) testAdUnitId else adUnitId

            bannerView!!.translatesAutoresizingMaskIntoConstraints = false

            bannerView!!.delegate = object : NSObject(), GADBannerViewDelegateProtocol {

                override fun bannerViewDidReceiveAd(bannerView: GADBannerView) {
                    log("Banner loaded")

                    adStateManager?.onAdLoaded()
                    adListener?.onAdLoaded()

                    fadeIn()
                }

                override fun bannerView(
                    bannerView: GADBannerView,
                    didFailToReceiveAdWithError: NSError
                ) {

                    log("Load failed ${didFailToReceiveAdWithError.localizedDescription}")

                    val error = didFailToReceiveAdWithError.adErrorFrom()

                    adStateManager?.onAdFailedToLoad(error)
                    adListener?.onAdFailedToLoad(error)

                    if (!keepAdSlot) hidden = true
                }

                override fun bannerViewDidRecordImpression(bannerView: GADBannerView) {
                    log("Ad impression")

                    adStateManager?.onAdDisplayed()
                    adListener?.onAdDisplayed()
                }

                override fun bannerViewDidRecordClick(bannerView: GADBannerView) {
                    log("Ad clicked")

                    adStateManager?.onAdClicked()
                    adListener?.onAdClicked()
                }
            }

            addSubview(bannerView!!)
            currentAdSize.useContents {
                NSLayoutConstraint.activateConstraints(
                    listOf(
                        bannerView!!.bottomAnchor.constraintEqualToAnchor(safeAreaLayoutGuide.bottomAnchor),
                        bannerView!!.centerXAnchor.constraintEqualToAnchor(centerXAnchor),
                        bannerView!!.widthAnchor.constraintEqualToConstant(size.width),
                        bannerView!!.heightAnchor.constraintEqualToConstant(size.height)
                    )
                )
            }
        }

        bannerView!!.rootViewController =
            findViewController()
                ?: UIApplication.sharedApplication.keyWindow?.rootViewController

        bannerView!!.loadRequest(adRequest)

        log("Ad request sent")
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()

        if (window != null && bannerView != null && bannerView!!.rootViewController == null) {
            bannerView!!.rootViewController = findViewController()
        }
    }

    private fun fadeIn() {

        if (!keepAdSlot) alpha = 0.0

        hidden = false

        if (!keepAdSlot) {

            animateWithDuration(
                duration = 0.25,
                animations = { this.alpha = 1.0 }
            )
        }
    }

    fun destroy() {
        bannerView?.removeFromSuperview()
        bannerView = null
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

        return null
    }
}
