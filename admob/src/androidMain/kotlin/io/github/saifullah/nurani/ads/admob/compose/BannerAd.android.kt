package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.saifullah.nurani.ads.admob.AdmobBannerView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.BannerAd

@Composable
actual fun AdmobBannerAd(
    properties: AdmobAdProperties,
    testModeEnabled: Boolean,
    expandWhenReady: Boolean,
    animateExpansion: Boolean,
    adSize: BannerAd<AdSize>,
    adFailedAdRetryRule: AdFailedRetryRule,
    adReloadPolicies: Set<AdReloadPolicy>,
    adLogger: AdLogger?,
    adListener: BannerAdListener?
) {
    val heightController = rememberBannerHeightController(
        expandWhenReady,
        animateExpansion
    )
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),

        factory = {ctx->
            AdmobBannerView(ctx).apply {
                setAdLogger(adLogger)
                setAdUnitId(properties.androidAdUnitId)
                this.retryRule = adFailedAdRetryRule
                setKeepAdSlot(expandWhenReady)
                setTestModeEnabled(testModeEnabled)
            }
        },

        update = { view ->
            view.setBannerAd(adSize)
            view.adListener = BannerAdListener(
                onAdShowed = { adListener?.onAdShowed() },
                onAdDismissed = { adListener?.onAdDismissed() },
                onAdLoaded = { adListener?.onAdLoaded(); heightController.onAdLoaded(view.adSize.height) },
                onAdClicked = { adListener?.onAdClicked() },
                onAdFailedToLoad = { adListener?.onAdFailedToLoad(it) },
                onAdFailedToShow = { adListener?.onAdFailedToLoad(it) },
                onAdDisplayed = {
                    adListener?.onAdDisplayed()
                    heightController.onAdDisplayed()
                }
            )
            view.loadAd()
        },

        onRelease = { it.destroy() }
    )
}
