@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.admob

import io.github.saifullah.nurani.ads.admob.compose.AdmobAdProperties
import io.github.saifullah.nurani.ads.admob.compose.AdmobDefault
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

class AdmobNativeAppOpenHandle internal constructor(
    val ad: AdmobAppOpenAd,
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

object AdmobNativeAppOpen {
    fun register(
        adUnitId: String,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        tag: String? = null,
        adFailedAdRetryRule: AdFailedRetryRule = AdmobDefault.DefaultAdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy = AdmobDefault.DefaultAdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy> = AdmobDefault.DefaultAdReloadPolicy,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: IosAppOpenAutoShowBuilder.() -> Unit = { appDidBecomeActive() }
    ): AdmobNativeAppOpenHandle {
        val ad = AdmobAppOpenAd(
            adUnitId = adUnitId,
            uIViewController = null,
            adConfig = adConfig {
                this.adLogger = adLogger
                this.adFailedRetryRule = adFailedAdRetryRule
                this.adRefreshStrategy = adRefreshStrategy
                this.adReloadPolicies = adReloadPolicies
                this.isTestModeEnabled = testModeEnabled
                this.tag = tag
            },
            adRequest = null
        )
        ad.isImmersiveModeEnabled = immersiveModeEnabled
        ad.setAdLoadCallback(adLoadCallback)
        ad.setAdContentCallback(adContentCallback)
        if (initialLoad) ad.loadAd()
        return AdmobNativeAppOpenHandle(ad, ad.bindToIosAppOpenAutoShow(configure))
    }

    fun register(
        properties: AdmobAdProperties,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        adFailedAdRetryRule: AdFailedRetryRule = AdmobDefault.DefaultAdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy = AdmobDefault.DefaultAdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy> = AdmobDefault.DefaultAdReloadPolicy,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: IosAppOpenAutoShowBuilder.() -> Unit = { appDidBecomeActive() }
    ): AdmobNativeAppOpenHandle {
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
