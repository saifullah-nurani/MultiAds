package io.github.saifullah.nurani.ads.applovin.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.applovin.FullScreenAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

/**
 * Remembers and manages the lifecycle of an AppLovin interstitial ad.
 */
@Composable
expect fun rememberAppLovinInterstitialAd(
    properties: AppLovinAdProperties,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    testModeEnabled: Boolean = false,
    context: PlatformContext = LocalPlatformContext.current,
    adFailedAdRetryRule: AdFailedRetryRule = AppLovinDefault.DefaultAdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy = AppLovinDefault.DefaultAdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy> = AppLovinDefault.DefaultAdReloadPolicy,
    adLogger: AdLogger? = null,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
): FullScreenAdState
