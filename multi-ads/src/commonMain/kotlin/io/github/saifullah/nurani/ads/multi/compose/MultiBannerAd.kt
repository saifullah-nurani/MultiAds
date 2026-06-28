package io.github.saifullah.nurani.ads.multi.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.exponentialRetry
import io.github.saifullah.nurani.ads.multi.models.MultiAdListener
import io.github.saifullah.nurani.ads.multi.models.MultiBannerAdConfig
import io.github.saifullah.nurani.ads.multi.models.MultiAdsConfig
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

@Composable
expect fun MultiBannerAd(
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = false,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = BannerAd.Fixed(AdSize.BANNER),
    adFailedAdRetryRule: AdFailedRetryRule = exponentialRetry(),
    adLogger: AdLogger? = null,
    adListener: MultiAdListener? = null,
    tag: String? = null
)

@Composable
expect fun MultiBannerAd(
    waterfallConfig: WaterfallConfig,
    config: MultiBannerAdConfig,
    adListener: MultiAdListener? = null
)
