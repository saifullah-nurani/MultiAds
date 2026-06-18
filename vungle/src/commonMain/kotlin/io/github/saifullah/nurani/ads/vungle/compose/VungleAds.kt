package io.github.saifullah.nurani.ads.vungle.compose

import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.exponentialRetry
import io.github.saifullah.nurani.ads.core.periodicRefresh

object VungleDefault {
    val DefaultBannerAd = BannerAd.Fixed(AdSize.BANNER)
    val DefaultAdRefreshStrategy = periodicRefresh()
    val DefaultAdFailedRetryRule = exponentialRetry()
    val DefaultAdReloadPolicy = setOf(
        AdReloadPolicy.OnDismissed,
        AdReloadPolicy.OnClicked
    )
}
