package io.github.saifullah.nurani.ads.`is`.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.saifullah.nurani.ads.`is`.IronSourceBannerView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

@Composable
actual fun IronSourceBannerAd(
    properties: IronSourceAdProperties,
    testModeEnabled: Boolean,
    expandWhenReady: Boolean,
    animateExpansion: Boolean,
    adSize: BannerAd<AdSize>,
    adFailedAdRetryRule: AdFailedRetryRule,
    adLogger: AdLogger?,
    adListener: BannerAdListener?
) {
    IronSourceBannerAd(
        placementName = properties.androidPlacementName,
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
 * Remembers and manages the lifecycle of an IronSource BannerAd.
 */
@Composable
fun IronSourceBannerAd(
    placementName: String? = null,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = true,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = IronSourceDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = IronSourceDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
) {
    val heightController = io.github.saifullah.nurani.ads.core.rememberBannerHeightController(
        expandWhenReady,
        animateExpansion
    )
    val placementNameState by rememberSaveable(placementName) {
        mutableStateOf(placementName)
    }
    val initialLoadRequested = androidx.compose.runtime.remember { booleanArrayOf(false) }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),

        factory = { ctx ->
            IronSourceBannerView(ctx).apply {
                setAdLogger(adLogger)
                placementNameState?.let { setPlacementId(it) }
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
 * Creates IronSource ad properties for Android placement names.
 */
fun ironSourceAdProperties(placementName: String? = null): IronSourceAdProperties {
    return IronSourceAdProperties(placementName, null)
}
