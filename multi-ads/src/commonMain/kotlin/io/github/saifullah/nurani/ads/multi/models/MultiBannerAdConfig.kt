package io.github.saifullah.nurani.ads.multi.models

import androidx.compose.runtime.Immutable
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.exponentialRetry

@Immutable
data class MultiBannerAdConfig internal constructor(
    val testModeEnabled: Boolean,
    val expandWhenReady: Boolean,
    val animateExpansion: Boolean,
    val adSize: BannerAd<AdSize>,
    val adFailedAdRetryRule: AdFailedRetryRule,
    val adLogger: AdLogger?,
    val adListener: BannerAdListener?,
    val tag: String?
)

@DslMarker
annotation class MultiBannerAdDsl

@MultiBannerAdDsl
class MultiBannerAdConfigBuilder internal constructor() {
    var testModeEnabled: Boolean = false
    var expandWhenReady: Boolean = false
    var animateExpansion: Boolean = true
    var adSize: BannerAd<AdSize> = BannerAd.Fixed(AdSize.BANNER)
    var adFailedAdRetryRule: AdFailedRetryRule = exponentialRetry()
    var adLogger: AdLogger? = null
    var adListener: BannerAdListener? = null
    var tag: String? = null

    internal fun build(): MultiBannerAdConfig {
        return MultiBannerAdConfig(
            testModeEnabled = testModeEnabled,
            expandWhenReady = expandWhenReady,
            animateExpansion = animateExpansion,
            adSize = adSize,
            adFailedAdRetryRule = adFailedAdRetryRule,
            adLogger = adLogger,
            adListener = adListener,
            tag = tag
        )
    }
}

fun multiBannerAdConfig(block: MultiBannerAdConfigBuilder.() -> Unit = {}): MultiBannerAdConfig {
    return MultiBannerAdConfigBuilder().apply(block).build()
}
