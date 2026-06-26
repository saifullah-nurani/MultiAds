package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.IosAppOpenAutoShowBuilder
import io.github.saifullah.nurani.ads.core.IosAppOpenAutoShowHandle
import io.github.saifullah.nurani.ads.core.bindToIosAppOpenAutoShow
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.multi.models.MultiAdsConfig
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

class MultiNativeAppOpenHandle internal constructor(
    val ad: MultiAppOpenAd,
    private val lifecycleHandle: IosAppOpenAutoShowHandle
) {
    fun notifyViewWillAppear(viewController: platform.UIKit.UIViewController) {
        lifecycleHandle.notifyViewWillAppear(viewController)
    }

    fun notifyViewDidAppear(viewController: platform.UIKit.UIViewController) {
        lifecycleHandle.notifyViewDidAppear(viewController)
    }

    fun dispose() {
        lifecycleHandle.dispose()
    }
}

object MultiNativeAppOpen {
    fun register(
        waterfallConfig: WaterfallConfig,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        tag: String? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: IosAppOpenAutoShowBuilder.() -> Unit = { appDidBecomeActive() }
    ): MultiNativeAppOpenHandle {
        val ad = MultiAppOpenAd(object : PlatformContext() {}).apply {
            this.waterfallConfig = waterfallConfig
            this.testModeEnabled = testModeEnabled
            this.isImmersiveModeEnabled = immersiveModeEnabled
            this.tag = tag
            setAdLoadCallback(adLoadCallback)
            setAdContentCallback(adContentCallback)
        }
        if (initialLoad) ad.loadAd()
        return MultiNativeAppOpenHandle(ad, ad.bindToIosAppOpenAutoShow(configure))
    }

    fun register(
        multiAdsConfig: MultiAdsConfig,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: IosAppOpenAutoShowBuilder.() -> Unit = { appDidBecomeActive() }
    ): MultiNativeAppOpenHandle {
        return register(
            waterfallConfig = multiAdsConfig.waterfallConfig ?: error("waterfallConfig is required"),
            initialLoad = initialLoad,
            immersiveModeEnabled = immersiveModeEnabled,
            testModeEnabled = multiAdsConfig.adConfig.isTestModeEnabled,
            tag = multiAdsConfig.adConfig.tag,
            adLoadCallback = adLoadCallback,
            adContentCallback = adContentCallback,
            configure = configure
        )
    }
}
