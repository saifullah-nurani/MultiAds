package io.github.saifullah.nurani.ads.multi

import android.app.Activity
import io.github.saifullah.nurani.ads.admob.AdmobInterstitialAd
import io.github.saifullah.nurani.ads.applovin.AppLovinInterstitialAd
import io.github.saifullah.nurani.ads.applovin.AppLovinAds
import io.github.saifullah.nurani.ads.core.AdState
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.inmobi.InMobiInterstitialAd
import io.github.saifullah.nurani.ads.inmobi.InMobiAds
import io.github.saifullah.nurani.ads.`is`.IronSourceInterstitialAd
import io.github.saifullah.nurani.ads.`is`.IronSourceAds
import io.github.saifullah.nurani.ads.man.MetaInterstitialAd
import io.github.saifullah.nurani.ads.man.MetaAds
import io.github.saifullah.nurani.ads.multi.models.AdNetwork
import io.github.saifullah.nurani.ads.multi.models.AdNetworkConfig
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig
import io.github.saifullah.nurani.ads.pangle.PangleInterstitialAd
import io.github.saifullah.nurani.ads.pangle.PangleAds
import io.github.saifullah.nurani.ads.vungle.VungleInterstitialAd
import io.github.saifullah.nurani.ads.vungle.VungleAds
import java.util.concurrent.atomic.AtomicBoolean

actual class MultiInterstitialAd actual constructor(
    private val context: PlatformContext
) : AdState {

    actual var waterfallConfig: WaterfallConfig? = null
    actual var testModeEnabled: Boolean = false
    actual var isImmersiveModeEnabled: Boolean = false

    private val pendingNetworks = mutableListOf<AdNetworkConfig>()
    private val loadingAds = mutableMapOf<AdNetworkConfig, AdState>()
    private var activeAd: AdState? = null
    private var isDestroyed = AtomicBoolean(false)

    private var adLoadListener: AdLoadCallback? = null
    private var adContentListener: AdContentCallback? = null

    actual override val isAdAvailable: Boolean
        get() = activeAd != null && activeAd?.isAdAvailable == true

    actual override val isAdLoading: Boolean
        get() = loadingAds.isNotEmpty()

    actual override val isRetryingAdFailedLoad: Boolean
        get() = loadingAds.values.any { it.isRetryingAdFailedLoad }

    actual override val isAdRefreshing: Boolean
        get() = loadingAds.values.any { it.isAdRefreshing }

    actual override val isAdReloading: Boolean
        get() = loadingAds.values.any { it.isAdReloading }

    actual override val attemptCount: Int
        get() = loadingAds.values.map { it.attemptCount }.maxOrNull() ?: 0

    actual override fun loadAd() {
        if (isDestroyed.get()) return
        val config = waterfallConfig ?: return

        // Clean up previous state
        destroyAllLoadingAds()
        activeAd?.let { destroyAd(it) }
        activeAd = null

        pendingNetworks.clear()
        pendingNetworks.addAll(config.networks.sortedBy { it.priority })

        loadNextBatch()
    }

    actual override fun reloadAd() {
        loadAd()
    }

    actual override fun setAdLoadCallback(callback: AdLoadCallback?) {
        this.adLoadListener = callback
    }

    actual override fun setAdContentCallback(callback: AdContentCallback?) {
        this.adContentListener = callback
    }

    actual fun showAd(activity: PlatformActivity) {
        val ad = activeAd ?: return

        ad.setAdContentCallback(object : AdContentCallback {
            override fun onAdFailedToShow(error: AdError?) {
                adContentListener?.onAdFailedToShow(error)
            }
            override fun onAdShowed() {
                adContentListener?.onAdShowed()
            }
            override fun onAdDisplayed() {
                adContentListener?.onAdDisplayed()
            }
            override fun onAdDismissed() {
                adContentListener?.onAdDismissed()
            }
            override fun onAdClicked() {
                adContentListener?.onAdClicked()
            }
        })

        showAdNetwork(ad, activity)
    }

    actual fun tryShowAd(): Boolean {
        if (!isAdAvailable) return false
        val activity = io.github.saifullah.nurani.ads.core.utils.ContextUtils.findActivity(context) ?: return false
        showAd(activity)
        return true
    }

    actual fun destroy() {
        isDestroyed.set(true)
        destroyAllLoadingAds()
        activeAd?.let { destroyAd(it) }
        activeAd = null
        pendingNetworks.clear()
    }

    private fun loadNextBatch() {
        if (isDestroyed.get() || activeAd != null) return
        val config = waterfallConfig ?: return

        while (loadingAds.size < config.maxConcurrentLoads && pendingNetworks.isNotEmpty()) {
            val nextConfig = pendingNetworks.removeAt(0)
            startLoadingNetwork(nextConfig)
        }

        if (loadingAds.isEmpty() && activeAd == null && pendingNetworks.isEmpty()) {
            adLoadListener?.onAdFailedToLoad(AdError(0, "All networks in waterfall failed to load", null))
        }
    }

    private fun startLoadingNetwork(config: AdNetworkConfig) {
        if (!isNetworkInitialized(config.network)) {
            android.util.Log.d("MultiInterstitialAd", "Skipping ${config.network} because SDK is not initialized")
            loadNextBatch()
            return
        }

        val adConfigObj = adConfig {
            isTestModeEnabled = testModeEnabled
        }

        val ad = createAd(config, adConfigObj) ?: run {
            loadNextBatch()
            return
        }

        ad.setAdLoadCallback(object : AdLoadCallback {
            override fun onAdLoaded() {
                if (isDestroyed.get() || activeAd != null) {
                    destroyAd(ad)
                    return
                }

                // Verify if this is the highest priority loaded ad
                val isHighestPriority = loadingAds.keys.all { it.priority >= config.priority }

                if (isHighestPriority) {
                    activeAd = ad
                    loadingAds.remove(config)
                    destroyAllLoadingAds()
                    pendingNetworks.clear()
                    adLoadListener?.onAdLoaded()
                }
            }

            override fun onAdFailedToLoad(error: AdError?) {
                if (isDestroyed.get()) return
                loadingAds.remove(config)
                destroyAd(ad)

                if (activeAd == null) {
                    val loadedLowerPriority = loadingAds.entries
                        .filter { (k, v) -> k.priority > config.priority && v.isAdAvailable }
                        .minByOrNull { it.key.priority }

                    if (loadedLowerPriority != null) {
                        val (winningConfig, winningAd) = loadedLowerPriority
                        activeAd = winningAd
                        loadingAds.remove(winningConfig)
                        destroyAllLoadingAds()
                        pendingNetworks.clear()
                        adLoadListener?.onAdLoaded()
                    } else {
                        loadNextBatch()
                    }
                }
            }
        })

        loadingAds[config] = ad
        ad.loadAd()
    }

    private fun createAd(config: AdNetworkConfig, adConfigObj: io.github.saifullah.nurani.ads.core.AdConfig): AdState? {
        return try {
            when (config.network) {
                AdNetwork.ADMOB -> AdmobInterstitialAd(context, config.adUnitId, adConfigObj, null, null)
                AdNetwork.APPLOVIN -> AppLovinInterstitialAd(context, config.adUnitId, adConfigObj, null)
                AdNetwork.META -> MetaInterstitialAd(context, config.adUnitId, adConfigObj, null)
                AdNetwork.VUNGLE -> VungleInterstitialAd(context, config.adUnitId, adConfigObj, null)
                AdNetwork.INMOBI -> InMobiInterstitialAd(context, config.adUnitId.toLong(), adConfigObj, null)
                AdNetwork.PANGLE -> PangleInterstitialAd(context, config.adUnitId, adConfigObj, null)
                AdNetwork.IRONSOURCE -> IronSourceInterstitialAd(context, config.adUnitId, adConfigObj, null)
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

    private fun destroyAllLoadingAds() {
        loadingAds.values.forEach { destroyAd(it) }
        loadingAds.clear()
    }

    private fun destroyAd(ad: AdState) {
        try {
            if (ad is io.github.saifullah.nurani.ads.core.AdLifecycleObserver) {
                ad.onDestroy()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAdNetwork(ad: AdState, activity: Activity) {
        when (ad) {
            is AdmobInterstitialAd -> ad.showAd(activity)
            is AppLovinInterstitialAd -> ad.showAd(activity)
            is MetaInterstitialAd -> ad.showAd(activity)
            is VungleInterstitialAd -> ad.showAd(activity)
            is InMobiInterstitialAd -> ad.showAd(activity)
            is PangleInterstitialAd -> ad.showAd(activity)
            is IronSourceInterstitialAd -> ad.showAd(activity)
        }
    }
}
