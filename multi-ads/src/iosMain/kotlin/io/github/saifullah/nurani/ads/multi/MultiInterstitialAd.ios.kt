@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.multi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.admob.AdmobInterstitialAd
import io.github.saifullah.nurani.ads.applovin.AppLovinInterstitialAd
import io.github.saifullah.nurani.ads.applovin.AppLovinAds
import io.github.saifullah.nurani.ads.core.AdState
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.AdLifecycleObserver
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.DefaultAdLogger
import io.github.saifullah.nurani.ads.inmobi.InMobiInterstitialAd
import io.github.saifullah.nurani.ads.inmobi.InMobiAds
import io.github.saifullah.nurani.ads.ironsource.IronSourceInterstitialAd
import io.github.saifullah.nurani.ads.ironsource.IronSourceAds
import io.github.saifullah.nurani.ads.man.MetaInterstitialAd
import io.github.saifullah.nurani.ads.man.MetaAds
import io.github.saifullah.nurani.ads.multi.models.AdNetwork
import io.github.saifullah.nurani.ads.multi.models.AdNetworkConfig
import io.github.saifullah.nurani.ads.multi.models.MultiAdContentCallback
import io.github.saifullah.nurani.ads.multi.models.MultiAdLoadCallback
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig
import io.github.saifullah.nurani.ads.pangle.PangleInterstitialAd
import io.github.saifullah.nurani.ads.pangle.PangleAds
import io.github.saifullah.nurani.ads.vungle.VungleInterstitialAd
import io.github.saifullah.nurani.ads.vungle.VungleAds
import platform.UIKit.UIViewController
import kotlin.concurrent.AtomicReference

actual class MultiInterstitialAd actual constructor(
    private val context: PlatformContext
) : AdState {

    actual var waterfallConfig: WaterfallConfig? = null
    actual var testModeEnabled: Boolean = false
    actual var isImmersiveModeEnabled: Boolean = false
    actual var tag: String? = null

    private val pendingNetworks = mutableListOf<AdNetworkConfig>()
    private val loadingAds = mutableMapOf<AdNetworkConfig, AdState>()
    private var activeAd: AdState? = null
    private var activeNetwork: AdNetworkConfig? = null
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
        updateState()

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

    actual fun setMultiAdLoadCallback(callback: MultiAdLoadCallback?) {
        this.multiAdLoadListener = callback
    }

    actual fun setMultiAdContentCallback(callback: MultiAdContentCallback?) {
        this.multiAdContentListener = callback
    }

    actual fun showAd(activity: PlatformActivity) {
        val ad = activeAd ?: return

        ad.setAdContentCallback(object : AdContentCallback {
            override fun onAdFailedToShow(error: AdError?) {
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
                adContentListener?.onAdDismissed()
                activeNetwork?.let { multiAdContentListener?.onAdDismissed(it) }
            }
            override fun onAdClicked() {
                adContentListener?.onAdClicked()
                activeNetwork?.let { multiAdContentListener?.onAdClicked(it) }
            }
        })

        showAdNetwork(ad, activity)
    }

    actual fun tryShowAd(): Boolean {
        if (!isAdAvailable) return false
        val ad = activeAd ?: return false
        return tryShowAdNetwork(ad)
    }

    actual fun destroy() {
        isDestroyed.value = true
        destroyAllLoadingAds()
        activeAd?.let { destroyAd(it) }
        activeAd = null
        activeNetwork = null
        pendingNetworks.clear()
        updateState()
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
            tag = this@MultiInterstitialAd.tag ?: config.network.name
            adLogger = DefaultAdLogger(config.network.name)
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

                // Verify if this is the highest priority loaded ad
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
                loadingAds.remove(config)
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
                AdNetwork.ADMOB -> AdmobInterstitialAd(config.adUnitId, null, adConfigObj, null)
                AdNetwork.APPLOVIN -> AppLovinInterstitialAd(config.adUnitId, null, adConfigObj)
                AdNetwork.META -> MetaInterstitialAd(config.adUnitId, null, adConfigObj)
                AdNetwork.VUNGLE -> VungleInterstitialAd(config.adUnitId, null, adConfigObj)
                AdNetwork.INMOBI -> InMobiInterstitialAd(config.adUnitId.toLong(), null, adConfigObj)
                AdNetwork.PANGLE -> PangleInterstitialAd(config.adUnitId, null, adConfigObj)
                AdNetwork.IRONSOURCE -> IronSourceInterstitialAd(config.adUnitId, null, adConfigObj)
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
        updateState()
    }

    private fun updateState() {
        isAdAvailableState = activeAd?.isAdAvailable == true
        isAdLoadingState = loadingAds.isNotEmpty()
        isRetryingAdFailedLoadState = loadingAds.values.any { it.isRetryingAdFailedLoad }
        isAdRefreshingState = loadingAds.values.any { it.isAdRefreshing }
        isAdReloadingState = loadingAds.values.any { it.isAdReloading }
        attemptCountState = loadingAds.values.maxOfOrNull { it.attemptCount } ?: 0
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
            is AdmobInterstitialAd -> ad.showAd(viewController)
            is AppLovinInterstitialAd -> ad.showAd(viewController)
            is MetaInterstitialAd -> ad.showAd(viewController)
            is VungleInterstitialAd -> ad.showAd(viewController)
            is InMobiInterstitialAd -> ad.showAd(viewController)
            is PangleInterstitialAd -> ad.showAd(viewController)
            is IronSourceInterstitialAd -> ad.showAd(viewController)
        }
    }

    private fun tryShowAdNetwork(ad: AdState): Boolean {
        return when (ad) {
            is AdmobInterstitialAd -> ad.tryShowAd()
            is AppLovinInterstitialAd -> ad.tryShowAd()
            is MetaInterstitialAd -> ad.tryShowAd()
            is VungleInterstitialAd -> ad.tryShowAd()
            is InMobiInterstitialAd -> ad.tryShowAd()
            is PangleInterstitialAd -> ad.tryShowAd()
            is IronSourceInterstitialAd -> ad.tryShowAd()
            else -> false
        }
    }
}
