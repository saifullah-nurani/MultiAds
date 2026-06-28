package io.github.saifullah.nurani.ads.applovin

import AppLovinSDK.MAAdView
import AppLovinSDK.MAAdFormat
import AppLovinSDK.MAAd
import AppLovinSDK.MAError
import AppLovinSDK.MAAdViewAdDelegateProtocol
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
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class AppLovinBannerUIView : UIView(frame = CGRectZero.readValue()) {

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

    private var adUnitId: String? = null
    private var bannerView: MAAdView? = null
    private var bannerAd: BannerAd<MAAdFormat>? = null
    private var adStateManager: AdStateManager? = null
    private var adDelegate: NSObject? = null
    private val bannerTag = "AppLovinBannerUIView"
    private var requestTag: String? = null

    fun setPlacementId(id: String) {
        adUnitId = id
    }

    fun setAdUnitId(id: String) {
        adUnitId = id
    }

    fun setRequestTag(tag: String?) {
        requestTag = tag
    }

    fun setBannerAd(ad: BannerAd<AdSize>) {
        val size = ad.getSize()
        currentAdSize = size
        bannerAd = ad.mapToBannerAd {
            when {
                it.height >= 250 -> MAAdFormat.mrec
                it.height >= 90 -> MAAdFormat.leader
                else -> MAAdFormat.banner
            }
        }
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

    private val testAdUnitId = "YOUR_MAX_TEST_AD_UNIT_ID"

    private fun loadAdInternally() {
        if (!isTestModeEnabled) {
            checkNotNull(adUnitId) { "adUnitId must be set." }
            require(adUnitId!!.isNotEmpty()) { "adUnitId must not be empty." }
        }
        destroy()
        if (bannerView == null) {
            val format = bannerAd?.getSize() ?: MAAdFormat.banner
            val finalAdUnitId = if (isTestModeEnabled) testAdUnitId else adUnitId!!
            bannerView = MAAdView(finalAdUnitId, format)
            bannerView!!.translatesAutoresizingMaskIntoConstraints = false
            val delegate = object : NSObject(), MAAdViewAdDelegateProtocol {
                override fun didLoadAd(ad: MAAd) {
                    log("Banner loaded")
                    adStateManager?.onAdLoaded()
                    adListener?.onAdLoaded()
                    fadeIn()
                }

                override fun didFailToLoadAdForAdUnitIdentifier(adUnitIdentifier: String, withError: MAError) {
                    log("Load failed ${withError.toString()}")
                    adStateManager?.onAdFailedToLoad(AdError(0, withError.toString()))
                    adListener?.onAdFailedToLoad(AdError(0, withError.toString()))
                    if (!keepAdSlot) hidden = true
                }

                override fun didDisplayAd(ad: MAAd) {
                    adStateManager?.onAdDisplayed()
                }

                override fun didHideAd(ad: MAAd) {
                    adStateManager?.onAdDismissed()
                }

                override fun didClickAd(ad: MAAd) {
                    adStateManager?.onAdClicked()
                }

                override fun didFailToDisplayAd(ad: MAAd, withError: MAError) {
                    adStateManager?.onAdFailedToShow(AdError(0, withError.toString()))
                }
                
                override fun didExpandAd(ad: MAAd) {}
                override fun didCollapseAd(ad: MAAd) {}
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
}
