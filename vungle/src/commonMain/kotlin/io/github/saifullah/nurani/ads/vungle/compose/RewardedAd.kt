package io.github.saifullah.nurani.ads.vungle.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.vungle.RewardedAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

/**
 * Remembers and manages the lifecycle of a Vungle rewarded ad.
 */
@Composable
expect fun rememberVungleRewardedAd(
    properties: VungleAdProperties,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    testModeEnabled: Boolean = false,
    context: PlatformContext = LocalPlatformContext.current,
    adFailedAdRetryRule: AdFailedRetryRule = VungleDefault.DefaultAdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy = VungleDefault.DefaultAdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy> = VungleDefault.DefaultAdReloadPolicy,
    adLogger: AdLogger? = null,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
): RewardedAdState
