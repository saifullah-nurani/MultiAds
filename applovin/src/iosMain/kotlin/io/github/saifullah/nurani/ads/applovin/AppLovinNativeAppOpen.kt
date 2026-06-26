@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.applovin

import io.github.saifullah.nurani.ads.applovin.compose.AppLovinAdProperties
import io.github.saifullah.nurani.ads.applovin.compose.AppLovinDefault
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.IosAppOpenAutoShowBuilder
import io.github.saifullah.nurani.ads.core.IosAppOpenAutoShowHandle
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.bindToIosAppOpenAutoShow

class AppLovinNativeAppOpenHandle internal constructor(
    val ad: AppLovinAppOpenAd,
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

object AppLovinNativeAppOpen {
    fun register(
        adUnitId: String,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        tag: String? = null,
        adFailedAdRetryRule: AdFailedRetryRule = AppLovinDefault.DefaultAdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy = AppLovinDefault.DefaultAdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy> = AppLovinDefault.DefaultAdReloadPolicy,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: IosAppOpenAutoShowBuilder.() -> Unit = { appDidBecomeActive() }
    ): AppLovinNativeAppOpenHandle {
        val ad = AppLovinAppOpenAd(
            adUnitId = adUnitId,
            uIViewController = null,
            adConfig = adConfig {
                this.adLogger = adLogger
                this.adFailedRetryRule = adFailedAdRetryRule
                this.adRefreshStrategy = adRefreshStrategy
                this.adReloadPolicies = adReloadPolicies
                this.isTestModeEnabled = testModeEnabled
                this.tag = tag
            }
        )
        ad.isImmersiveModeEnabled = immersiveModeEnabled
        ad.setAdLoadCallback(adLoadCallback)
        ad.setAdContentCallback(adContentCallback)
        if (initialLoad) ad.loadAd()
        return AppLovinNativeAppOpenHandle(ad, ad.bindToIosAppOpenAutoShow(configure))
    }

    fun register(
        properties: AppLovinAdProperties,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        adFailedAdRetryRule: AdFailedRetryRule = AppLovinDefault.DefaultAdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy = AppLovinDefault.DefaultAdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy> = AppLovinDefault.DefaultAdReloadPolicy,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: IosAppOpenAutoShowBuilder.() -> Unit = { appDidBecomeActive() }
    ): AppLovinNativeAppOpenHandle {
        return register(
            adUnitId = properties.iosAdUnitId,
            initialLoad = initialLoad,
            immersiveModeEnabled = immersiveModeEnabled,
            testModeEnabled = testModeEnabled,
            tag = properties.tag,
            adFailedAdRetryRule = adFailedAdRetryRule,
            adRefreshStrategy = adRefreshStrategy,
            adReloadPolicies = adReloadPolicies,
            adLogger = adLogger,
            adLoadCallback = adLoadCallback,
            adContentCallback = adContentCallback,
            configure = configure
        )
    }
}
