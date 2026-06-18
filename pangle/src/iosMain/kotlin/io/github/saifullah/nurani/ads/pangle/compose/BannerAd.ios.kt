package io.github.saifullah.nurani.ads.pangle.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.github.saifullah.nurani.ads.pangle.PangleBannerUIView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PangleBannerAd(
    properties: PangleAdProperties,
    testModeEnabled: Boolean,
    expandWhenReady: Boolean,
    animateExpansion: Boolean,
    adSize: BannerAd<AdSize>,
    adFailedAdRetryRule: AdFailedRetryRule,
    adLogger: AdLogger?,
    adListener: BannerAdListener?
) {
    val heightController = io.github.saifullah.nurani.ads.core.rememberBannerHeightController(
        expandWhenReady,
        animateExpansion
    )
    val adUnit by rememberSaveable(properties.iosAdUnitId) {
        mutableStateOf(properties.iosAdUnitId)
    }
    val initialLoadRequested = androidx.compose.runtime.remember { booleanArrayOf(false) }
    UIKitView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),

        factory = {
            PangleBannerUIView().apply {
                logger = adLogger
                this.isTestModeEnabled = testModeEnabled
                this.retryRule = adFailedAdRetryRule
                this.keepAdSlot = expandWhenReady
                setAdUnitId(adUnit)
            }
        },

        update = { view ->
            view.setBannerAd(adSize)
            view.adListener = BannerAdListener(
                onAdShowed = { adListener?.onAdShowed() },
                onAdDismissed = { adListener?.onAdDismissed() },
                onAdLoaded = {
                    adListener?.onAdLoaded()
                    heightController.onAdLoaded(view.adSize.height)
                },
                onAdClicked = { adListener?.onAdClicked() },
                onAdFailedToLoad = { adListener?.onAdFailedToLoad(it) },
                onAdFailedToShow = { adListener?.onAdFailedToLoad(it) },
                onAdDisplayed = {
                    adListener?.onAdDisplayed()
                    heightController.onAdDisplayed()
                }
            )
            if (!initialLoadRequested[0]) {
                initialLoadRequested[0] = true
                view.loadAd()
            }
        },

        onRelease = { it.destroy() }
    )
}
