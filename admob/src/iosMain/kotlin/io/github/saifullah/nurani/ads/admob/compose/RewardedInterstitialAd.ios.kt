package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.saifullah.nurani.ads.admob.AdmobRewardedInterstitialAd
import io.github.saifullah.nurani.ads.admob.RewardedAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Remembers and manages the lifecycle of a rewarded interstitial ad.
 *
 * This function integrates with Compose lifecycle to automatically:
 * - start and stop the ad state
 * - load the ad initially if required
 * - attach load/content callbacks
 */
@Composable
actual fun rememberAdmobRewardedInterstitialAd(
    properties: AdmobAdProperties,
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
        AdmobRewardedInterstitialAd(
            adUnitId = properties.iosAdUnitId,
            uIViewController = null,
            adRequest = null,
            adConfig = adConfig {
                this.adLogger = adLogger
                this.adFailedRetryRule = adFailedAdRetryRule
                this.adRefreshStrategy = adRefreshStrategy
                this.adReloadPolicies = adReloadPolicies
                this.isTestModeEnabled = testModeEnabled
            },
        )
    }

    /**
     * Connect ad lifecycle with Compose lifecycle.
     */
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