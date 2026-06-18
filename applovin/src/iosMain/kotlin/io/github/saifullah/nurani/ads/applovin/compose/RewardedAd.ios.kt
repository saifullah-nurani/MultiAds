package io.github.saifullah.nurani.ads.applovin.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.applovin.RewardedAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

import androidx.compose.runtime.remember
import io.github.saifullah.nurani.ads.applovin.AppLovinRewardedAd
import io.github.saifullah.nurani.ads.core.adConfig
import kotlinx.cinterop.ExperimentalForeignApi

@Composable
actual fun rememberAppLovinRewardedAd(
    properties: AppLovinAdProperties,
    initialLoad: Boolean,
    immersiveModeEnabled: Boolean,
    testModeEnabled: Boolean,
    context: PlatformContext,
    adFailedAdRetryRule: AdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy>,
    adLogger: AdLogger?,
    adLoadCallback: AdLoadCallback?,
    adContentCallback: AdContentCallback?
): RewardedAdState {
    val adState = remember(properties.iosAdUnitId) {
        @OptIn(ExperimentalForeignApi::class)
        AppLovinRewardedAd(
            adUnitId = properties.iosAdUnitId,
            uIViewController = null,
            adConfig = adConfig {
                this.adLogger = adLogger
                this.adFailedRetryRule = adFailedAdRetryRule
                this.adRefreshStrategy = adRefreshStrategy
                this.adReloadPolicies = adReloadPolicies
                this.isTestModeEnabled = testModeEnabled
            }
        )
    }

    AdStateLifecycleManage(
        adUnitId = properties.iosAdUnitId,
        initialLoad = initialLoad,
        immersiveModeEnabled = immersiveModeEnabled,
        adState = adState,
        adLoadCallback = adLoadCallback,
        adContentCallback = adContentCallback
    )
    return adState
}
