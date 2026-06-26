@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.pangle

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
import io.github.saifullah.nurani.ads.pangle.compose.PangleAdProperties
import io.github.saifullah.nurani.ads.pangle.compose.PangleDefault

class PangleNativeAppOpenHandle internal constructor(
    val ad: PangleAppOpenAd,
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

object PangleNativeAppOpen {
    fun register(
        adUnitId: String,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        tag: String? = null,
        adFailedAdRetryRule: AdFailedRetryRule = PangleDefault.DefaultAdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy = PangleDefault.DefaultAdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy> = PangleDefault.DefaultAdReloadPolicy,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: IosAppOpenAutoShowBuilder.() -> Unit = { appDidBecomeActive() }
    ): PangleNativeAppOpenHandle {
        val ad = PangleAppOpenAd(
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
        return PangleNativeAppOpenHandle(ad, ad.bindToIosAppOpenAutoShow(configure))
    }

    fun register(
        properties: PangleAdProperties,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        adFailedAdRetryRule: AdFailedRetryRule = PangleDefault.DefaultAdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy = PangleDefault.DefaultAdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy> = PangleDefault.DefaultAdReloadPolicy,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: IosAppOpenAutoShowBuilder.() -> Unit = { appDidBecomeActive() }
    ): PangleNativeAppOpenHandle {
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
