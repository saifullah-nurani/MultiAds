package io.github.saifullah.nurani.ads.vungle.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import io.github.saifullah.nurani.ads.vungle.VungleBannerUIView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.rememberBannerHeightController
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VungleBannerAd(
    properties: VungleAdProperties,
    testModeEnabled: Boolean,
    expandWhenReady: Boolean,
    animateExpansion: Boolean,
    adSize: BannerAd<AdSize>,
    adFailedAdRetryRule: AdFailedRetryRule,
    adLogger: AdLogger?,
    adListener: BannerAdListener?
) {
    val heightController = rememberBannerHeightController(
        initialHeight = adSize.getSize().height,
        expandWhenReady = expandWhenReady,
        animateExpansion = animateExpansion
    )
    val placementId by rememberSaveable(properties.iosPlacementId) {
        mutableStateOf(properties.iosPlacementId)
    }
    val initialLoadRequested = remember { booleanArrayOf(false) }
    UIKitView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),
        background = androidx.compose.ui.graphics.Color.Transparent,
        factory = {
            VungleBannerUIView().apply {
                logger = adLogger
                this.isTestModeEnabled = testModeEnabled
                this.retryRule = adFailedAdRetryRule
                this.keepAdSlot = expandWhenReady
                setRequestTag(properties.tag)
                setPlacementId(placementId)
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
