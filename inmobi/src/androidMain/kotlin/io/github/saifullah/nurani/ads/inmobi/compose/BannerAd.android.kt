package io.github.saifullah.nurani.ads.inmobi.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.saifullah.nurani.ads.inmobi.InMobiBannerView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

@Composable
actual fun InMobiBannerAd(
    properties: InMobiAdProperties,
    testModeEnabled: Boolean,
    expandWhenReady: Boolean,
    animateExpansion: Boolean,
    adSize: BannerAd<AdSize>,
    adFailedAdRetryRule: AdFailedRetryRule,
    adLogger: AdLogger?,
    adListener: BannerAdListener?
) {
    InMobiBannerAd(
        placementId = properties.androidPlacementId,
        testModeEnabled = testModeEnabled,
        expandWhenReady = expandWhenReady,
        animateExpansion = animateExpansion,
        adSize = adSize,
        adFailedAdRetryRule = adFailedAdRetryRule,
        adLogger = adLogger,
        adListener = adListener
    )
}

/**
 * Remembers and manages the lifecycle of an InMobi BannerAd.
 */
@Composable
fun InMobiBannerAd(
    placementId: Long,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = true,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = InMobiDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = InMobiDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
) {
    val heightController = io.github.saifullah.nurani.ads.core.rememberBannerHeightController(
        expandWhenReady,
        animateExpansion
    )
    val placementIdState by rememberSaveable(placementId) {
        mutableStateOf(placementId)
    }
    val initialLoadRequested = androidx.compose.runtime.remember { booleanArrayOf(false) }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),

        factory = { ctx ->
            InMobiBannerView(ctx).apply {
                setAdLogger(adLogger)
                setPlacementId(placementIdState)
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
            if (!initialLoadRequested[0]) {
                initialLoadRequested[0] = true
                view.loadAd()
            }
        },

        onRelease = { it.destroy() }
    )
}

/**
 * Creates InMobi ad properties for Android placement IDs.
 */
fun inMobiPlacementProperties(placementId: Long): InMobiAdProperties {
    return InMobiAdProperties(placementId, 0L)
}
