package io.github.saifullah.nurani.ads.multi

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import io.github.saifullah.nurani.ads.admob.AdmobBannerView
import io.github.saifullah.nurani.ads.applovin.AppLovinAds
import io.github.saifullah.nurani.ads.applovin.AppLovinBannerView
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.exponentialRetry
import io.github.saifullah.nurani.ads.inmobi.InMobiAds
import io.github.saifullah.nurani.ads.inmobi.InMobiBannerView
import io.github.saifullah.nurani.ads.ironsource.IronSourceBannerView
import io.github.saifullah.nurani.ads.ironsource.IronSourceAds
import io.github.saifullah.nurani.ads.man.MetaBannerView
import io.github.saifullah.nurani.ads.man.MetaAds
import io.github.saifullah.nurani.ads.multi.models.AdNetwork
import io.github.saifullah.nurani.ads.multi.models.AdNetworkConfig
import io.github.saifullah.nurani.ads.multi.models.MultiAdListener
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig
import io.github.saifullah.nurani.ads.pangle.PangleAds
import io.github.saifullah.nurani.ads.pangle.PangleBannerView
import io.github.saifullah.nurani.ads.vungle.VungleAds
import io.github.saifullah.nurani.ads.vungle.VungleBannerView
import java.util.concurrent.atomic.AtomicBoolean

class MultiBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var waterfallConfig: WaterfallConfig? = null
    private var bannerAdSize: BannerAd<AdSize>? = null
    private var adListener: BannerAdListener? = null
    private var testMode = false

    private val pendingNetworks = mutableListOf<AdNetworkConfig>()
    private val loadingViews = mutableMapOf<AdNetworkConfig, View>()
    private var activeAdView: View? = null
    private var isDestroyed = AtomicBoolean(false)
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
        if (isDestroyed.get()) return
        val config = waterfallConfig ?: return
        
        // Reset state
        destroyAllLoadingViews()
        activeAdView?.let { removeView(it); destroyAdView(it) }
        activeAdView = null
        
        // Sort and populate pending queue
        pendingNetworks.clear()
        pendingNetworks.addAll(config.networks.sortedBy { it.priority })
        
        // Start initial loads
        loadNextBatch()
    }

    private fun loadNextBatch() {
        if (isDestroyed.get() || activeAdView != null) return
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
            android.util.Log.d("MultiBannerView", "Skipping ${config.network} because SDK is not initialized")
            return
        }

        val view = createAdView(config.network) ?: return
        
        // Apply configuration
        when (view) {
            is AdmobBannerView -> {
                view.setAdUnitId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setTestModeEnabled(testMode)
                view.retryRule = adFailedAdRetryRule
                view.setAdLogger(adLogger)
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
            }
            is AppLovinBannerView -> {
                view.setAdUnitId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setTestModeEnabled(testMode)
                view.retryRule = adFailedAdRetryRule
                view.setAdLogger(adLogger)
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
            }
            is MetaBannerView -> {
                view.setPlacementId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setTestModeEnabled(testMode)
                view.retryRule = adFailedAdRetryRule
                view.setAdLogger(adLogger)
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
            }
            is VungleBannerView -> {
                view.setPlacementId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setTestModeEnabled(testMode)
                view.retryRule = adFailedAdRetryRule
                view.setAdLogger(adLogger)
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
            }
            is InMobiBannerView -> {
                view.setPlacementId(config.adUnitId.toLong())
                bannerAdSize?.let { view.setBannerAd(it) }
                view.retryRule = adFailedAdRetryRule
                view.setAdLogger(adLogger)
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
            }
            is PangleBannerView -> {
                view.setAdUnitId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.retryRule = adFailedAdRetryRule
                view.setAdLogger(adLogger)
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
            }
            is IronSourceBannerView -> {
                view.setPlacementId(config.adUnitId)
                bannerAdSize?.let { view.setBannerAd(it) }
                view.setTestModeEnabled(testMode)
                view.retryRule = adFailedAdRetryRule
                view.setAdLogger(adLogger)
                view.setRequestTag(requestTag)
                view.adListener = createChildListener(config, view)
            }
        }
        
        // Add to map and invisible until loaded
        loadingViews[config] = view
        view.visibility = View.INVISIBLE
        addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        
        // Trigger load
        when (view) {
            is AdmobBannerView -> view.loadAd()
            is AppLovinBannerView -> view.loadAd()
            is MetaBannerView -> view.loadAd()
            is VungleBannerView -> view.loadAd()
            is InMobiBannerView -> view.loadAd()
            is PangleBannerView -> view.loadAd()
            is IronSourceBannerView -> view.loadAd()
        }
    }

    private fun createChildListener(config: AdNetworkConfig, view: View): BannerAdListener {
        return object : BannerAdListener {
            override fun onAdLoaded() {
                multiAdListener?.onAdLoaded(config)
                if (isDestroyed.get() || activeAdView != null) {
                    // Already have an active ad, or destroyed
                    destroyAdView(view)
                    return
                }
                
                // This ad loaded successfully!
                // Is it the highest priority currently loading?
                val isHighestPriority = loadingViews.keys.all { it.priority >= config.priority }
                
                if (isHighestPriority) {
                    // We win!
                    activeAdView = view
                    view.visibility = View.VISIBLE
                    loadingViews.remove(config)
                    
                    // Destroy all other loading views to save memory
                    destroyAllLoadingViews()
                    pendingNetworks.clear()

                    adListener?.onAdLoaded()
                } else {
                    // Keep it hidden, waiting for higher priority to fail or succeed
                }
            }

            override fun onAdFailedToLoad(error: AdError?) {
                multiAdListener?.onAdFailedToLoad(config, error)
                if (isDestroyed.get()) return
                
                loadingViews.remove(config)
                removeView(view)
                destroyAdView(view)
                
                if (activeAdView == null) {
                    // Check if a lower priority ad has already loaded
                    val loadedLowerPriority = loadingViews.entries
                        .filter { (k, v) -> k.priority > config.priority && v.visibility == View.INVISIBLE }
                        .minByOrNull { it.key.priority }
                    
                    if (loadedLowerPriority != null) {
                        // The higher priority failed, so let's use the waiting lower priority one
                        val (winningConfig, winningView) = loadedLowerPriority
                        activeAdView = winningView
                        winningView.visibility = View.VISIBLE
                        loadingViews.remove(winningConfig)
                        
                        destroyAllLoadingViews()
                        pendingNetworks.clear()
                        
                        adListener?.onAdLoaded()
                    } else {
                        // Keep loading next
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

    private fun createAdView(network: AdNetwork): View? {
        return try {
            when (network) {
                AdNetwork.ADMOB -> AdmobBannerView(context)
                AdNetwork.APPLOVIN -> AppLovinBannerView(context)
                AdNetwork.META -> MetaBannerView(context)
                AdNetwork.VUNGLE -> VungleBannerView(context)
                AdNetwork.INMOBI -> InMobiBannerView(context)
                AdNetwork.PANGLE -> PangleBannerView(context)
                AdNetwork.IRONSOURCE -> IronSourceBannerView(context)
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
            AdNetwork.META -> MetaAds.isInitialized()
            AdNetwork.VUNGLE -> VungleAds.isInitialized()
            AdNetwork.INMOBI -> InMobiAds.isInitialized()
            AdNetwork.PANGLE -> PangleAds.isInitialized()
            AdNetwork.IRONSOURCE -> IronSourceAds.isInitialized()
        }
    }

    private fun destroyAllLoadingViews() {
        loadingViews.values.forEach { view ->
            removeView(view)
            destroyAdView(view)
        }
        loadingViews.clear()
    }

    private fun destroyAdView(view: View) {
        when (view) {
            is AdmobBannerView -> view.destroy()
            is AppLovinBannerView -> view.destroy()
            is MetaBannerView -> view.destroy()
            is VungleBannerView -> view.destroy()
            is InMobiBannerView -> view.destroy()
            is PangleBannerView -> view.destroy()
            is IronSourceBannerView -> view.destroy()
        }
    }

    fun destroy() {
        isDestroyed.set(true)
        destroyAllLoadingViews()
        activeAdView?.let { destroyAdView(it) }
        activeAdView = null
        pendingNetworks.clear()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Some ads require destroy on detach
        // But we will let the consumer call destroy() explicitly if needed.
    }
}
