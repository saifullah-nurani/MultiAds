package io.github.saifullah.nurani.ads.`is`.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

/**
 * Remembers and manages the lifecycle of an IronSource BannerAd.
 */
@Composable
expect fun IronSourceBannerAd(
    properties: IronSourceAdProperties = ironSourceAdProperties(),
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = false,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = IronSourceDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = IronSourceDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
)
