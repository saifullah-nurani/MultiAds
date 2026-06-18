package io.github.saifullah.nurani.ads.applovin.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

/**
 * Remembers and manages the lifecycle of an AppLovin BannerAd.
 */
@Composable
expect fun AppLovinBannerAd(
    properties: AppLovinAdProperties,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = false,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = AppLovinDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = AppLovinDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
)
