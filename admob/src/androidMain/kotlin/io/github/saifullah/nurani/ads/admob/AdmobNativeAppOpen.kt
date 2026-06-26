package io.github.saifullah.nurani.ads.admob

import android.app.Application
import com.google.android.gms.ads.AdRequest
import io.github.saifullah.nurani.ads.admob.compose.AdmobAdProperties
import io.github.saifullah.nurani.ads.admob.compose.AdmobDefault
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AndroidAppOpenAutoShowBuilder
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.bindToAndroidAppOpenAutoShow

object AdmobNativeAppOpen {
    @JvmStatic
    fun register(
        application: Application,
        adUnitId: String,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        tag: String? = null,
        adRequest: AdRequest? = null,
        adFailedAdRetryRule: AdFailedRetryRule = AdmobDefault.DefaultAdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy = AdmobDefault.DefaultAdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy> = AdmobDefault.DefaultAdReloadPolicy,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: AndroidAppOpenAutoShowBuilder.() -> Unit = { anyActivity() }
    ): AdmobAppOpenAd {
        val ad = AdmobAppOpenAd(
            context = application,
            adUnitId = adUnitId,
            adConfig = adConfig {
                this.adLogger = adLogger
                this.adFailedRetryRule = adFailedAdRetryRule
                this.adRefreshStrategy = adRefreshStrategy
                this.adReloadPolicies = adReloadPolicies
                this.isTestModeEnabled = testModeEnabled
                this.tag = tag
            },
            adRequest = adRequest,
            handler = null
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
        properties: AdmobAdProperties,
        initialLoad: Boolean = true,
        immersiveModeEnabled: Boolean = true,
        testModeEnabled: Boolean = false,
        adRequest: AdRequest? = null,
        adFailedAdRetryRule: AdFailedRetryRule = AdmobDefault.DefaultAdFailedRetryRule,
        adRefreshStrategy: AdRefreshStrategy = AdmobDefault.DefaultAdRefreshStrategy,
        adReloadPolicies: Set<AdReloadPolicy> = AdmobDefault.DefaultAdReloadPolicy,
        adLogger: AdLogger? = null,
        adLoadCallback: AdLoadCallback? = null,
        adContentCallback: AdContentCallback? = null,
        configure: AndroidAppOpenAutoShowBuilder.() -> Unit = { anyActivity() }
    ): AdmobAppOpenAd {
        return register(
            application = application,
            adUnitId = properties.androidAdUnitId,
            initialLoad = initialLoad,
            immersiveModeEnabled = immersiveModeEnabled,
            testModeEnabled = testModeEnabled,
            tag = properties.tag,
            adRequest = adRequest,
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
