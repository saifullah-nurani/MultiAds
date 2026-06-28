package io.github.saifullah.nurani.ads.vungle

import VungleAdsSDK.VungleBanner
import VungleAdsSDK.VungleBannerDelegateProtocol
import VungleAdsSDK.VungleAdSize
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
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class VungleBannerUIView : UIView(frame = CGRectZero.readValue()) {

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
    private var bannerView: VungleBanner? = null
    private var bannerAd: BannerAd<AdSize>? = null
    private var adStateManager: AdStateManager? = null
    private var adDelegate: NSObject? = null
    private val bannerTag = "VungleBannerUIView"
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

    private val testAdUnitId = "B1-5106071"

    private fun loadAdInternally() {
        if (!VungleAds.isInitialized()) {
            val adError = AdError(
                code = 0,
                message = "Vungle SDK is not initialized yet."
            )
            adStateManager?.onAdFailedToLoad(adError)
            adListener?.onAdFailedToLoad(adError)
            return
        }
        if (!isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId!!.isNotEmpty()) { "placementId must not be empty." }
        }
        destroy()
        if (bannerView == null) {
            val vSize = when {
                currentAdSize.height >= 250 -> VungleAdSize.VungleAdSizeMREC()
                currentAdSize.height >= 90 -> VungleAdSize.VungleAdSizeLeaderboard()
                else -> VungleAdSize.VungleAdSizeBannerRegular()
            }
            val finalPlacementId = if (isTestModeEnabled) testAdUnitId else placementId ?: ""
            bannerView = VungleBanner(placementId = finalPlacementId, vungleAdSize = vSize)
            val delegate = object : NSObject(), VungleBannerDelegateProtocol {
                override fun bannerAdDidLoad(banner: VungleBanner) {
                    log("Banner loaded")
                    adStateManager?.onAdLoaded()
                    adListener?.onAdLoaded()
                    fadeIn()
                }

                override fun bannerAdDidFailToLoad(banner: VungleBanner, withError: NSError) {
                    log("Load failed ${withError.localizedDescription}")
                    adStateManager?.onAdFailedToLoad(AdError(0, withError.localizedDescription ?: "Unknown error"))
                    adListener?.onAdFailedToLoad(AdError(0, withError.localizedDescription ?: "Unknown error"))
                    if (!keepAdSlot) hidden = true
                }

                override fun bannerAdWillPresent(banner: VungleBanner) {}

                override fun bannerAdDidPresent(banner: VungleBanner) {
                    adStateManager?.onAdDisplayed()
                    subviews.forEach { 
                        val view = it as? UIView
                        if (view != null && view.translatesAutoresizingMaskIntoConstraints) {
                            view.translatesAutoresizingMaskIntoConstraints = false
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
                }

                override fun bannerAdDidFailToPresent(banner: VungleBanner, withError: NSError) {
                    adStateManager?.onAdFailedToShow(AdError(0, withError.localizedDescription ?: "Unknown error"))
                }

                override fun bannerAdDidClick(banner: VungleBanner) {
                    adStateManager?.onAdClicked()
                }

                override fun bannerAdDidClose(banner: VungleBanner) {
                    adStateManager?.onAdDismissed()
                }
                
                override fun bannerAdDidTrackImpression(banner: VungleBanner) {}
                override fun bannerAdWillClose(banner: VungleBanner) {}
                override fun bannerAdWillLeaveApplication(banner: VungleBanner) {}
            }
            adDelegate = delegate
            bannerView!!.setDelegate(delegate)
            
            bannerView!!.presentOn(this)
        }
        bannerView!!.load(null)
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
        bannerView = null
        adDelegate = null
        adStateManager?.onDestroy()
        adStateManager = null
    }

    private fun log(msg: String) {
        logger?.d("$bannerTag : $msg")
    }
}
