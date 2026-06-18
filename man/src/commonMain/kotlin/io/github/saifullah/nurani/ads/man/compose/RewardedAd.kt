package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.man.MetaRewardedAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

/**
 * Remembers and manages the lifecycle of a Meta rewarded ad.
 */
@Composable
expect fun rememberMetaRewardedAd(
    properties: MetaAdProperties,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    testModeEnabled: Boolean = false,
    context: PlatformContext = LocalPlatformContext.current,
    adFailedAdRetryRule: AdFailedRetryRule = MetaDefault.DefaultAdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy = MetaDefault.DefaultAdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy> = MetaDefault.DefaultAdReloadPolicy,
    adLogger: AdLogger? = null,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
): MetaRewardedAdState
