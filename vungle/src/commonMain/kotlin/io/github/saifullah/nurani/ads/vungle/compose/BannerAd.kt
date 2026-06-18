package io.github.saifullah.nurani.ads.vungle.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

/**
 * Remembers and manages the lifecycle of a Vungle BannerAd.
 */
@Composable
expect fun VungleBannerAd(
    properties: VungleAdProperties,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = false,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = VungleDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = VungleDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
)
