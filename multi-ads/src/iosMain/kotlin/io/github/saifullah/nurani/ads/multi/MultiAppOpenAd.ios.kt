@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.multi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.admob.AdmobAppOpenAd
import io.github.saifullah.nurani.ads.applovin.AppLovinAppOpenAd
import io.github.saifullah.nurani.ads.applovin.AppLovinAds
import io.github.saifullah.nurani.ads.core.AdState
import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.AdLifecycleObserver
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.DefaultAdLogger
import io.github.saifullah.nurani.ads.multi.models.AdNetwork
import io.github.saifullah.nurani.ads.multi.models.AdNetworkConfig
import io.github.saifullah.nurani.ads.multi.models.MultiAdContentCallback
import io.github.saifullah.nurani.ads.multi.models.MultiAdLoadCallback
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig
import io.github.saifullah.nurani.ads.pangle.PangleAds
import io.github.saifullah.nurani.ads.pangle.PangleAppOpenAd
import platform.UIKit.UIViewController
import kotlin.concurrent.AtomicReference

actual class MultiAppOpenAd actual constructor(
    private val context: PlatformContext
) : AppOpenAd {

    actual var waterfallConfig: WaterfallConfig? = null
    actual var testModeEnabled: Boolean = false
    actual var isImmersiveModeEnabled: Boolean = false
    actual var tag: String? = null
    actual var requestConfig: AdConfig = AdConfig.default

    private val pendingNetworks = mutableListOf<AdNetworkConfig>()
    private val loadingAds = mutableMapOf<AdNetworkConfig, AdState>()
    private val failedNetworks = mutableSetOf<AdNetworkConfig>()
    private var activeAd: AdState? = null
    private var activeNetwork: AdNetworkConfig? = null
    private var isShowingAd = false
    private var isDestroyed = AtomicReference(false)
    private var isAdAvailableState by mutableStateOf(false)
    private var isAdLoadingState by mutableStateOf(false)
    private var isRetryingAdFailedLoadState by mutableStateOf(false)
    private var isAdRefreshingState by mutableStateOf(false)
    private var isAdReloadingState by mutableStateOf(false)
    private var attemptCountState by mutableIntStateOf(0)

    private var adLoadListener: AdLoadCallback? = null
    private var adContentListener: AdContentCallback? = null
    private var multiAdLoadListener: MultiAdLoadCallback? = null
    private var multiAdContentListener: MultiAdContentCallback? = null

    actual override val isAdAvailable: Boolean
        get() = isAdAvailableState

    actual override val isAdLoading: Boolean
        get() = isAdLoadingState

    actual override val isRetryingAdFailedLoad: Boolean
        get() = isRetryingAdFailedLoadState

    actual override val isAdRefreshing: Boolean
        get() = isAdRefreshingState

    actual override val isAdReloading: Boolean
        get() = isAdReloadingState

    actual override val attemptCount: Int
        get() = attemptCountState

    actual override fun loadAd() {
        if (isDestroyed.value) return
        val config = waterfallConfig ?: return

        // Clean up previous state
        destroyAllLoadingAds()
        activeAd?.let { destroyAd(it) }
        activeAd = null
        activeNetwork = null
        isShowingAd = false
        failedNetworks.clear()
        updateState()

        pendingNetworks.clear()
        pendingNetworks.addAll(
            config.networks
                .filter { it.network in SUPPORTED_NETWORKS }
                .sortedBy { it.priority }
        )

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

    actual fun setMultiAdLoadCallback(callback: MultiAdLoadCallback?) {
        this.multiAdLoadListener = callback
    }

    actual fun setMultiAdContentCallback(callback: MultiAdContentCallback?) {
        this.multiAdContentListener = callback
    }

    actual override fun showAd(activity: PlatformActivity) {
        val ad = activeAd ?: return

        ad.setAdContentCallback(object : AdContentCallback {
            override fun onAdFailedToShow(error: AdError?) {
                isShowingAd = false
                updateState()
                adContentListener?.onAdFailedToShow(error)
                activeNetwork?.let { multiAdContentListener?.onAdFailedToShow(it, error) }
            }
            override fun onAdShowed() {
                adContentListener?.onAdShowed()
                activeNetwork?.let { multiAdContentListener?.onAdShowed(it) }
            }
            override fun onAdDisplayed() {
                adContentListener?.onAdDisplayed()
                activeNetwork?.let { multiAdContentListener?.onAdDisplayed(it) }
            }
            override fun onAdDismissed() {
                isShowingAd = false
                updateState()
                adContentListener?.onAdDismissed()
                activeNetwork?.let { multiAdContentListener?.onAdDismissed(it) }
            }
            override fun onAdClicked() {
                adContentListener?.onAdClicked()
                activeNetwork?.let { multiAdContentListener?.onAdClicked(it) }
            }
        })

        isShowingAd = true
        updateState()
        showAdNetwork(ad, activity)
    }

    actual override fun tryShowAd(): Boolean {
        if (!isAdAvailable) return false
        val ad = activeAd ?: return false
        ad.setAdContentCallback(object : AdContentCallback {
            override fun onAdFailedToShow(error: AdError?) {
                isShowingAd = false
                updateState()
                adContentListener?.onAdFailedToShow(error)
                activeNetwork?.let { multiAdContentListener?.onAdFailedToShow(it, error) }
            }

            override fun onAdShowed() {
                adContentListener?.onAdShowed()
                activeNetwork?.let { multiAdContentListener?.onAdShowed(it) }
            }

            override fun onAdDisplayed() {
                adContentListener?.onAdDisplayed()
                activeNetwork?.let { multiAdContentListener?.onAdDisplayed(it) }
            }

            override fun onAdDismissed() {
                isShowingAd = false
                updateState()
                adContentListener?.onAdDismissed()
                activeNetwork?.let { multiAdContentListener?.onAdDismissed(it) }
            }

            override fun onAdClicked() {
                adContentListener?.onAdClicked()
                activeNetwork?.let { multiAdContentListener?.onAdClicked(it) }
            }
        })
        val shown = tryShowAdNetwork(ad)
        isShowingAd = shown
        updateState()
        return shown
    }

    actual fun destroy() {
        isDestroyed.value = true
        destroyAllLoadingAds()
        activeAd?.let { destroyAd(it) }
        activeAd = null
        activeNetwork = null
        isShowingAd = false
        pendingNetworks.clear()
        failedNetworks.clear()
        updateState()
    }

    actual override fun onStart() {
        (activeAd as? AdLifecycleObserver)?.onStart()
        loadingAds.values.forEach { (it as? AdLifecycleObserver)?.onStart() }
        updateState()
    }

    actual override fun onStop() {
        (activeAd as? AdLifecycleObserver)?.onStop()
        loadingAds.values.forEach { (it as? AdLifecycleObserver)?.onStop() }
        updateState()
    }

    actual override fun onDestroy() {
        destroy()
    }

    private fun loadNextBatch() {
        if (isDestroyed.value || activeAd != null) return
        val config = waterfallConfig ?: return

        while (loadingAds.size < config.maxConcurrentLoads && pendingNetworks.isNotEmpty()) {
            val nextConfig = pendingNetworks.removeAt(0)
            startLoadingNetwork(nextConfig)
        }
        updateState()

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
            tag = this@MultiAppOpenAd.tag ?: config.network.name
            adLogger = requestConfig.adLogger ?: DefaultAdLogger(config.network.name)
            adReloadPolicies = requestConfig.adReloadPolicies
            adFailedRetryRule = requestConfig.adFailedRetryRule
            adRefreshStrategy = requestConfig.adRefreshStrategy
        }

        val ad = createAd(config, adConfigObj) ?: run {
            loadNextBatch()
            return
        }

        ad.setAdLoadCallback(object : AdLoadCallback {
            override fun onAdLoaded() {
                if (isDestroyed.value) {
                    destroyAd(ad)
                    return
                }
                if (activeAd === ad) {
                    updateState()
                    adLoadListener?.onAdLoaded()
                    multiAdLoadListener?.onAdLoaded(config)
                    return
                }
                if (activeAd != null) {
                    destroyAd(ad)
                    return
                }

                val isHighestPriority = loadingAds.keys.all { it.priority >= config.priority }

                if (isHighestPriority) {
                    activeAd = ad
                    activeNetwork = config
                    loadingAds.remove(config)
                    destroyAllLoadingAds()
                    pendingNetworks.clear()
                    updateState()
                    adLoadListener?.onAdLoaded()
                    multiAdLoadListener?.onAdLoaded(config)
                }
            }

            override fun onAdFailedToLoad(error: AdError?) {
                if (isDestroyed.value) return
                if (activeAd === ad) {
                    multiAdLoadListener?.onAdFailedToLoad(config, error)
                    if (ad.isAdAvailable) {
                        updateState()
                        return
                    }
                    failedNetworks.add(config)
                    activeAd = null
                    activeNetwork = null
                    destroyAd(ad)
                    pendingNetworks.clear()
                    pendingNetworks.addAll(
                        waterfallConfig?.networks.orEmpty()
                            .filter { it !in failedNetworks }
                            .sortedBy { it.priority }
                    )
                    updateState()
                    loadNextBatch()
                    return
                }
                if (loadingAds[config] !== ad) return
                loadingAds.remove(config)
                failedNetworks.add(config)
                destroyAd(ad)
                multiAdLoadListener?.onAdFailedToLoad(config, error)

                if (activeAd == null) {
                    val loadedLowerPriority = loadingAds.entries
                        .filter { (k, v) -> k.priority > config.priority && v.isAdAvailable }
                        .minByOrNull { it.key.priority }

                    if (loadedLowerPriority != null) {
                        val (winningConfig, winningAd) = loadedLowerPriority
                        activeAd = winningAd
                        activeNetwork = winningConfig
                        loadingAds.remove(winningConfig)
                        destroyAllLoadingAds()
                        pendingNetworks.clear()
                        updateState()
                        adLoadListener?.onAdLoaded()
                        multiAdLoadListener?.onAdLoaded(winningConfig)
                    } else {
                        loadNextBatch()
                    }
                }
            }
        })

        loadingAds[config] = ad
        updateState()
        ad.loadAd()
    }

    private fun createAd(config: AdNetworkConfig, adConfigObj: AdConfig): AdState? {
        return try {
            when (config.network) {
                AdNetwork.ADMOB -> AdmobAppOpenAd(config.adUnitId, null, adConfigObj, null)
                AdNetwork.APPLOVIN -> AppLovinAppOpenAd(config.adUnitId, null, adConfigObj)
                AdNetwork.PANGLE -> PangleAppOpenAd(config.adUnitId, null, adConfigObj)
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
            AdNetwork.PANGLE -> PangleAds.isInitialized()
            else -> false
        }
    }

    private fun destroyAllLoadingAds() {
        loadingAds.values.forEach { destroyAd(it) }
        loadingAds.clear()
        updateState()
    }

    private fun updateState() {
        val managedAds = loadingAds.values + listOfNotNull(activeAd)
        isAdAvailableState = !isShowingAd && activeAd?.isAdAvailable == true
        isAdLoadingState = managedAds.any { it.isAdLoading }
        isRetryingAdFailedLoadState = managedAds.any { it.isRetryingAdFailedLoad }
        isAdRefreshingState = managedAds.any { it.isAdRefreshing }
        isAdReloadingState = managedAds.any { it.isAdReloading }
        attemptCountState = managedAds.maxOfOrNull { it.attemptCount } ?: 0
    }

    private fun destroyAd(ad: AdState) {
        try {
            if (ad is AdLifecycleObserver) {
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
            is PangleAppOpenAd -> ad.showAd(viewController)
        }
    }

    private fun tryShowAdNetwork(ad: AdState): Boolean {
        return when (ad) {
            is AdmobAppOpenAd -> ad.tryShowAd()
            is AppLovinAppOpenAd -> ad.tryShowAd()
            is PangleAppOpenAd -> ad.tryShowAd()
            else -> false
        }
    }

    private companion object {
        val SUPPORTED_NETWORKS = setOf(AdNetwork.ADMOB, AdNetwork.APPLOVIN, AdNetwork.PANGLE)
    }
}
