package io.github.saifullah.nurani.ads.`is`.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.`is`.RewardedAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

/**
 * Remembers and manages the lifecycle of an IronSource rewarded ad.
 */
@Composable
expect fun rememberIronSourceRewardedAd(
    properties: IronSourceAdProperties = ironSourceAdProperties(),
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    testModeEnabled: Boolean = false,
    context: PlatformContext = LocalPlatformContext.current,
    adFailedAdRetryRule: AdFailedRetryRule = IronSourceDefault.DefaultAdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy = IronSourceDefault.DefaultAdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy> = IronSourceDefault.DefaultAdReloadPolicy,
    adLogger: AdLogger? = null,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
): RewardedAdState
