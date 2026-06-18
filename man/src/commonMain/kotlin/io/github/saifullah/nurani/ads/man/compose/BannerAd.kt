package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

/**
 * Remembers and manages the lifecycle of a Meta BannerAd.
 */
@Composable
expect fun MetaBannerAd(
    properties: MetaAdProperties,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = false,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = MetaDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = MetaDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
)
