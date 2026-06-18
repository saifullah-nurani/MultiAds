package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.admob.RewardedAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

/**
 * Remembers and manages the lifecycle of a rewarded ad.
 *
 * This function integrates with Compose lifecycle to automatically:
 * - start and stop the ad state
 * - load the ad initially if required
 * - attach load/content callbacks
 */
@Composable
expect  fun rememberAdmobRewardedAd(
    properties: AdmobAdProperties,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    testModeEnabled: Boolean = false,
    context: PlatformContext = LocalPlatformContext.current,
    adFailedAdRetryRule: AdFailedRetryRule = AdmobDefault.DefaultAdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy = AdmobDefault.DefaultAdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy> = AdmobDefault.DefaultAdReloadPolicy,
    adLogger: AdLogger? = null,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
): RewardedAdState
