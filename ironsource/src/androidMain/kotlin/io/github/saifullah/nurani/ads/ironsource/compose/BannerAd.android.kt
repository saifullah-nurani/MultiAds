package io.github.saifullah.nurani.ads.ironsource.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.saifullah.nurani.ads.ironsource.IronSourceBannerView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.rememberBannerHeightController
import io.github.saifullah.nurani.ads.core.rememberBannerLifecycleBinding

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
        adUnitId = properties.androidAdUnitId,
        tag = properties.tag,
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
    adUnitId: String? = null,
    tag: String? = null,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = true,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = IronSourceDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = IronSourceDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
) {
    val heightController = rememberBannerHeightController(
        initialHeight = adSize.getSize().height,
        expandWhenReady = expandWhenReady,
        animateExpansion = animateExpansion
    )
    val adUnitIdState by rememberSaveable(adUnitId) {
        mutableStateOf(adUnitId)
    }
    val initialLoadRequested = remember { booleanArrayOf(false) }
    val lifecycleBinding = rememberBannerLifecycleBinding<IronSourceBannerView>(
        onStart = { it.resume() },
        onStop = { it.pause() }
    )
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),

        factory = { ctx ->
            IronSourceBannerView(ctx).apply {
                setAdLogger(adLogger)
                adUnitIdState?.let { setAdUnitId(it) }
                setRequestTag(tag)
                this.retryRule = adFailedAdRetryRule
                setKeepAdSlot(expandWhenReady)
                setTestModeEnabled(testModeEnabled)
            }.also(lifecycleBinding::attach)
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

        onRelease = {
            lifecycleBinding.detach(it)
            it.destroy()
        }
    )
}

/**
 * Creates IronSource ad properties for Android LevelPlay ad unit IDs.
 */
fun ironSourceAdProperties(
    adUnitId: String? = null,
    tag: String? = null
): IronSourceAdProperties {
    return IronSourceAdProperties(adUnitId, null, tag)
}
