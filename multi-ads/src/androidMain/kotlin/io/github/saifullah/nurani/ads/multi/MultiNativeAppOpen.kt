package io.github.saifullah.nurani.ads.multi

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
import io.github.saifullah.nurani.ads.multi.models.MultiAdsConfig
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

object MultiNativeAppOpen {
    @JvmStatic
    fun register(
        application: Application,
        waterfallConfig: WaterfallConfig,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        tag: String? = null,
        adFailedAdRetryRule: AdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy>,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: AndroidAppOpenAutoShowBuilder.() -> Unit = { anyActivity() }
    ): MultiAppOpenAd {
        val ad = MultiAppOpenAd(application).apply {
            this.waterfallConfig = waterfallConfig
            this.testModeEnabled = testModeEnabled
            this.isImmersiveModeEnabled = immersiveModeEnabled
            this.tag = tag
            setAdLoadCallback(adLoadCallback)
            setAdContentCallback(adContentCallback)
        }
        if (initialLoad) ad.loadAd()
        return ad.bindToAndroidAppOpenAutoShow(application, configure)
    }

    @JvmStatic
    fun register(
        application: Application,
        multiAdsConfig: MultiAdsConfig,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: AndroidAppOpenAutoShowBuilder.() -> Unit = { anyActivity() }
    ): MultiAppOpenAd {
        return register(
            application = application,
            waterfallConfig = multiAdsConfig.waterfallConfig ?: error("waterfallConfig is required"),
            initialLoad = initialLoad,
            immersiveModeEnabled = immersiveModeEnabled,
            testModeEnabled = multiAdsConfig.adConfig.isTestModeEnabled,
            tag = multiAdsConfig.adConfig.tag,
            adFailedAdRetryRule = multiAdsConfig.adConfig.adFailedRetryRule,
            adRefreshStrategy = multiAdsConfig.adConfig.adRefreshStrategy,
            adReloadPolicies = multiAdsConfig.adConfig.adReloadPolicies,
            adLogger = adLogger ?: multiAdsConfig.adConfig.adLogger,
            adLoadCallback = adLoadCallback,
            adContentCallback = adContentCallback,
            configure = configure
        )
    }
}
