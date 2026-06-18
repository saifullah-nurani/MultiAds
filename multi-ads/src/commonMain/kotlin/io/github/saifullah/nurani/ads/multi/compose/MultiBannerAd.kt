package io.github.saifullah.nurani.ads.multi.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

@Composable
expect fun MultiBannerAd(
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean = false,
    adSize: BannerAd<AdSize> = BannerAd.Fixed(AdSize.BANNER),
    adListener: BannerAdListener? = null
)
