@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.admob.AdmobAppOpenAd
import io.github.saifullah.nurani.ads.applovin.AppLovinAppOpenAd
import io.github.saifullah.nurani.ads.applovin.AppLovinAds
import io.github.saifullah.nurani.ads.core.AdState
import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.multi.models.AdNetwork
import io.github.saifullah.nurani.ads.multi.models.AdNetworkConfig
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig
import platform.UIKit.UIViewController
import kotlin.concurrent.AtomicReference

actual class MultiAppOpenAd actual constructor(
    private val context: PlatformContext
) : AppOpenAd {

    actual var waterfallConfig: WaterfallConfig? = null
    actual var testModeEnabled: Boolean = false
    actual var isImmersiveModeEnabled: Boolean = false

    private val pendingNetworks = mutableListOf<AdNetworkConfig>()
    private val loadingAds = mutableMapOf<AdNetworkConfig, AdState>()
    private var activeAd: AdState? = null
    private var isDestroyed = AtomicReference(false)

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
        if (isDestroyed.value) return
        val config = waterfallConfig ?: return

        // Clean up previous state
        destroyAllLoadingAds()
        activeAd?.let { destroyAd(it) }
        activeAd = null

        pendingNetworks.clear()
        pendingNetworks.addAll(config.networks.filter { it.network == AdNetwork.ADMOB || it.network == AdNetwork.APPLOVIN }.sortedBy { it.priority })

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

    actual override fun showAd(activity: PlatformActivity) {
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

    actual override fun tryShowAd(): Boolean {
        if (!isAdAvailable) return false
        val ad = activeAd ?: return false
        return tryShowAdNetwork(ad)
    }

    actual fun destroy() {
        isDestroyed.value = true
        destroyAllLoadingAds()
        activeAd?.let { destroyAd(it) }
        activeAd = null
        pendingNetworks.clear()
    }

    private fun loadNextBatch() {
        if (isDestroyed.value || activeAd != null) return
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
                if (isDestroyed.value || activeAd != null) {
                    destroyAd(ad)
                    return
                }

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
                if (isDestroyed.value) return
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
                AdNetwork.ADMOB -> AdmobAppOpenAd(config.adUnitId, null, adConfigObj, null)
                AdNetwork.APPLOVIN -> AppLovinAppOpenAd(config.adUnitId, null, adConfigObj)
                else -> null
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
            else -> false
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

    private fun showAdNetwork(ad: AdState, viewController: UIViewController) {
        when (ad) {
            is AdmobAppOpenAd -> ad.showAd(viewController)
            is AppLovinAppOpenAd -> ad.showAd(viewController)
        }
    }

    private fun tryShowAdNetwork(ad: AdState): Boolean {
        return when (ad) {
            is AdmobAppOpenAd -> ad.tryShowAd()
            is AppLovinAppOpenAd -> ad.tryShowAd()
            else -> false
        }
    }
}
