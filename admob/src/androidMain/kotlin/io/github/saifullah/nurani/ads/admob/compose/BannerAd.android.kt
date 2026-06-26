package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.saifullah.nurani.ads.admob.AdmobBannerView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

@Composable
actual fun AdmobBannerAd(
    properties: AdmobAdProperties,
    testModeEnabled: Boolean,
    expandWhenReady: Boolean,
    animateExpansion: Boolean,
    adSize: BannerAd<AdSize>,
    adFailedAdRetryRule: AdFailedRetryRule,
    adLogger: AdLogger?,
    adListener: BannerAdListener?
) {
    AdmobBannerAd(
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
 * Remembers and manages the lifecycle of a BannerAd.
 *
 * This function integrates with Compose lifecycle to automatically:
 * - start and stop the ad state
 * - load the ad initially if required
 * - attach load/content callbacks
 */
@Composable
fun AdmobBannerAd(
    adUnitId: String,
    tag: String? = null,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = true,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = AdmobDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = AdmobDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
) {
    val heightController = io.github.saifullah.nurani.ads.core.rememberBannerHeightController(
        initialHeight = adSize.getSize().height,
        expandWhenReady = expandWhenReady,
        animateExpansion = animateExpansion
    )
    val adUnitId by rememberSaveable(adUnitId) {
        mutableStateOf(adUnitId)
    }
    val initialLoadRequested = androidx.compose.runtime.remember { booleanArrayOf(false) }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),

        factory = { ctx ->
            AdmobBannerView(ctx).apply {
                setAdLogger(adLogger)
                setAdUnitId(adUnitId)
                setRequestTag(tag)
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
 * Creates AdMob ad properties for Android ad unit IDs.
 */
fun admobAdProperties(
    adUnitId: String,
    tag: String? = null
): AdmobAdProperties {
    return AdmobAdProperties(adUnitId, "", tag)
}
