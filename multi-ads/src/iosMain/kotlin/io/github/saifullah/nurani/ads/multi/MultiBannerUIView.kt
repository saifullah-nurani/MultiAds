package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.admob.AdmobBannerUIView
import io.github.saifullah.nurani.ads.applovin.AppLovinAds
import io.github.saifullah.nurani.ads.applovin.AppLovinBannerUIView
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.exponentialRetry
import io.github.saifullah.nurani.ads.inmobi.InMobiAds
import io.github.saifullah.nurani.ads.inmobi.InMobiBannerUIView
import io.github.saifullah.nurani.ads.ironsource.IronSourceBannerUIView
import io.github.saifullah.nurani.ads.ironsource.IronSourceAds
import io.github.saifullah.nurani.ads.man.MetaBannerUIView
import io.github.saifullah.nurani.ads.man.MetaAudienceNetworkAds
import io.github.saifullah.nurani.ads.multi.models.AdNetwork
import io.github.saifullah.nurani.ads.multi.models.AdNetworkConfig
import io.github.saifullah.nurani.ads.multi.models.MultiAdListener
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig
import io.github.saifullah.nurani.ads.pangle.PangleAds
import io.github.saifullah.nurani.ads.pangle.PangleBannerUIView
import io.github.saifullah.nurani.ads.vungle.VungleAds
import io.github.saifullah.nurani.ads.vungle.VungleBannerUIView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIColor
import platform.UIKit.UIView
import kotlin.concurrent.AtomicReference

@OptIn(ExperimentalForeignApi::class)
class MultiBannerUIView : UIView(frame = CGRectZero.readValue()) {

    init {
        backgroundColor = UIColor.clearColor
        opaque = false
    }

    private var waterfallConfig: WaterfallConfig? = null
    private var bannerAdSize: BannerAd<AdSize>? = null
    private var adListener: BannerAdListener? = null
    private var testMode = false

    private val pendingNetworks = mutableListOf<AdNetworkConfig>()
    private val loadingViews = mutableMapOf<AdNetworkConfig, UIView>()
    private var activeAdView: UIView? = null
    private var isDestroyed = AtomicReference(false)
    private var requestTag: String? = null
    private var multiAdListener: MultiAdListener? = null
    private var adFailedAdRetryRule: AdFailedRetryRule = exponentialRetry()
    private var adLogger: AdLogger? = null

    fun setWaterfallConfig(config: WaterfallConfig) {
        this.waterfallConfig = config
    }

    fun setBannerAd(size: BannerAd<AdSize>) {
        this.bannerAdSize = size
    }

    fun setAdListener(listener: BannerAdListener?) {
        this.adListener = listener
    }

    fun setMultiAdListener(listener: MultiAdListener?) {
        this.multiAdListener = listener
    }

    fun setTestModeEnabled(enabled: Boolean) {
        this.testMode = enabled
    }

    fun setAdFailedAdRetryRule(rule: AdFailedRetryRule) {
        this.adFailedAdRetryRule = rule
    }

    fun setAdLogger(logger: AdLogger?) {
        this.adLogger = logger
    }

    fun setRequestTag(tag: String?) {
        this.requestTag = tag
    }

    fun loadAd() {
        if (isDestroyed.value) return
        val config = waterfallConfig ?: return
        
        destroyAllLoadingViews()
        activeAdView?.removeFromSuperview()
        activeAdView = null
        
        pendingNetworks.clear()
        pendingNetworks.addAll(config.networks.sortedBy { it.priority })
        
        loadNextBatch()
    }

    private fun loadNextBatch() {
        if (isDestroyed.value || activeAdView != null) return
        val config = waterfallConfig ?: return
        
        while (loadingViews.size < config.maxConcurrentLoads && pendingNetworks.isNotEmpty()) {
            val nextConfig = pendingNetworks.removeAt(0)
            startLoadingNetwork(nextConfig)
        }
        
        if (loadingViews.isEmpty() && activeAdView == null && pendingNetworks.isEmpty()) {
            adListener?.onAdFailedToLoad(AdError(0, "All networks in waterfall failed to load", null))
        }
    }

    private fun startLoadingNetwork(config: AdNetworkConfig) {
        if (!isNetworkInitialized(config.network)) {
            return
        }

        val view = createAdView(config.network) ?: return
        
        when (view) {
            is AdmobBannerUIView -> {
                view.setAdUnitId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
                view.retryRule = adFailedAdRetryRule
                view.logger = adLogger
            }
            is AppLovinBannerUIView -> {
                view.setAdUnitId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
                view.retryRule = adFailedAdRetryRule
                view.logger = adLogger
            }
            is MetaBannerUIView -> {
                view.setPlacementId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
                view.retryRule = adFailedAdRetryRule
                view.logger = adLogger
            }
            is VungleBannerUIView -> {
                view.setPlacementId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
                view.retryRule = adFailedAdRetryRule
                view.logger = adLogger
            }
            is InMobiBannerUIView -> {
                view.setPlacementId(config.adUnitId.toLongOrNull() ?: 0L)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
                view.retryRule = adFailedAdRetryRule
                view.logger = adLogger
            }
            is PangleBannerUIView -> {
                view.setAdUnitId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
                view.retryRule = adFailedAdRetryRule
                view.logger = adLogger
            }
            is IronSourceBannerUIView -> {
                view.setPlacementId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
                view.retryRule = adFailedAdRetryRule
                view.logger = adLogger
            }
        }
        
        loadingViews[config] = view
        view.hidden = true
        addSubview(view)
        
        when (view) {
            is AdmobBannerUIView -> view.loadAd()
            is AppLovinBannerUIView -> view.loadAd()
            is MetaBannerUIView -> view.loadAd()
            is VungleBannerUIView -> view.loadAd()
            is InMobiBannerUIView -> view.loadAd()
            is PangleBannerUIView -> view.loadAd()
            is IronSourceBannerUIView -> view.loadAd()
        }
    }

    private fun createChildListener(config: AdNetworkConfig, view: UIView): BannerAdListener {
        return object : BannerAdListener {
            override fun onAdLoaded() {
                multiAdListener?.onAdLoaded(config)
                if (isDestroyed.value || activeAdView != null) {
                    view.removeFromSuperview()
                    return
                }
                
                val isHighestPriority = loadingViews.keys.all { it.priority >= config.priority }
                
                if (isHighestPriority) {
                    activeAdView = view
                    view.hidden = false
                    loadingViews.remove(config)
                    
                    destroyAllLoadingViews()
                    pendingNetworks.clear()
                    
                    adListener?.onAdLoaded()
                }
            }

            override fun onAdFailedToLoad(error: AdError?) {
                multiAdListener?.onAdFailedToLoad(config, error)
                if (isDestroyed.value) return
                
                loadingViews.remove(config)
                view.removeFromSuperview()
                
                if (activeAdView == null) {
                    val loadedLowerPriority = loadingViews.entries
                        .filter { (k, v) -> k.priority > config.priority && v.hidden }
                        .minByOrNull { it.key.priority }
                    
                    if (loadedLowerPriority != null) {
                        val (winningConfig, winningView) = loadedLowerPriority
                        activeAdView = winningView
                        winningView.hidden = false
                        loadingViews.remove(winningConfig)
                        
                        destroyAllLoadingViews()
                        pendingNetworks.clear()
                        
                        adListener?.onAdLoaded()
                    } else {
                        loadNextBatch()
                    }
                }
            }

            override fun onAdFailedToShow(error: AdError?) {
                multiAdListener?.onAdFailedToShow(config, error)
                if (activeAdView == view) adListener?.onAdFailedToShow(error)
            }
            override fun onAdShowed() {
                multiAdListener?.onAdShowed(config)
                if (activeAdView == view) adListener?.onAdShowed()
            }
            override fun onAdDisplayed() {
                multiAdListener?.onAdDisplayed(config)
                if (activeAdView == view) adListener?.onAdDisplayed()
            }
            override fun onAdDismissed() {
                multiAdListener?.onAdDismissed(config)
                if (activeAdView == view) adListener?.onAdDismissed()
            }
            override fun onAdClicked() {
                multiAdListener?.onAdClicked(config)
                if (activeAdView == view) adListener?.onAdClicked()
            }
        }
    }

    private fun createAdView(network: AdNetwork): UIView? {
        return try {
            when (network) {
                AdNetwork.ADMOB -> AdmobBannerUIView()
                AdNetwork.APPLOVIN -> AppLovinBannerUIView()
                AdNetwork.META -> MetaBannerUIView()
                AdNetwork.VUNGLE -> VungleBannerUIView()
                AdNetwork.INMOBI -> InMobiBannerUIView()
                AdNetwork.PANGLE -> PangleBannerUIView()
                AdNetwork.IRONSOURCE -> IronSourceBannerUIView()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isNetworkInitialized(network: AdNetwork): Boolean {
        return when (network) {
            AdNetwork.ADMOB -> true
            AdNetwork.APPLOVIN -> AppLovinAds.isInitialized()
            AdNetwork.META -> MetaAudienceNetworkAds.isInitialized()
            AdNetwork.VUNGLE -> VungleAds.isInitialized()
            AdNetwork.INMOBI -> InMobiAds.isInitialized()
            AdNetwork.PANGLE -> PangleAds.isInitialized()
            AdNetwork.IRONSOURCE -> IronSourceAds.isInitialized()
        }
    }

    private fun destroyAllLoadingViews() {
        loadingViews.values.forEach { it.removeFromSuperview() }
        loadingViews.clear()
    }

    fun destroy() {
        isDestroyed.value = true
        destroyAllLoadingViews()
        activeAdView?.removeFromSuperview()
        activeAdView = null
        pendingNetworks.clear()
    }
}
