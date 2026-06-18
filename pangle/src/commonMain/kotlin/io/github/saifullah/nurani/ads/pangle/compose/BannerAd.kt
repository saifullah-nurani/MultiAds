package io.github.saifullah.nurani.ads.pangle.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

/**
 * Remembers and manages the lifecycle of a Pangle BannerAd.
 *
 * This function integrates with Compose lifecycle to automatically:
 * - start and stop the ad state
 * - load the ad initially if required
 * - attach load/content callbacks
 */
@Composable
expect fun PangleBannerAd(
    properties: PangleAdProperties,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = false,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = PangleDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = PangleDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
)
