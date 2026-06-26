package io.github.saifullah.nurani.ads.pangle

import android.app.Application
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AndroidAppOpenAutoShowBuilder
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.bindToAndroidAppOpenAutoShow
import io.github.saifullah.nurani.ads.pangle.compose.PangleAdProperties
import io.github.saifullah.nurani.ads.pangle.compose.PangleDefault

object PangleNativeAppOpen {
    @JvmStatic
    fun register(
        application: Application,
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
        configure: AndroidAppOpenAutoShowBuilder.() -> Unit = { anyActivity() }
    ): PangleAppOpenAd {
        val ad = PangleAppOpenAd(
            context = application,
            adUnitId = adUnitId,
            handler = null,
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
        return ad.bindToAndroidAppOpenAutoShow(application, configure)
    }

    @JvmStatic
    fun register(
        application: Application,
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
        configure: AndroidAppOpenAutoShowBuilder.() -> Unit = { anyActivity() }
    ): PangleAppOpenAd {
        return register(
            application = application,
            adUnitId = properties.androidAdUnitId,
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
