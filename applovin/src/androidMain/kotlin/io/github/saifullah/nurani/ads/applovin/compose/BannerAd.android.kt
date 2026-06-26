package io.github.saifullah.nurani.ads.applovin.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.saifullah.nurani.ads.applovin.AppLovinBannerView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener

@Composable
actual fun AppLovinBannerAd(
    properties: AppLovinAdProperties,
    testModeEnabled: Boolean,
    expandWhenReady: Boolean,
    animateExpansion: Boolean,
    adSize: BannerAd<AdSize>,
    adFailedAdRetryRule: AdFailedRetryRule,
    adLogger: AdLogger?,
    adListener: BannerAdListener?
) {
    AppLovinBannerAd(
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
 * Remembers and manages the lifecycle of an AppLovin BannerAd.
 */
@Composable
fun AppLovinBannerAd(
    adUnitId: String,
    tag: String? = null,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = true,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = AppLovinDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = AppLovinDefault.DefaultAdFailedRetryRule,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
) {
    val heightController = io.github.saifullah.nurani.ads.core.rememberBannerHeightController(
        initialHeight = adSize.getSize().height,
        expandWhenReady = expandWhenReady,
        animateExpansion = animateExpansion
    )
    val adUnitIdState by rememberSaveable(adUnitId) {
        mutableStateOf(adUnitId)
    }
    val initialLoadRequested = androidx.compose.runtime.remember { booleanArrayOf(false) }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),

        factory = { ctx ->
            AppLovinBannerView(ctx).apply {
                setAdLogger(adLogger)
                setAdUnitId(adUnitIdState)
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
                onAdLoaded = { adListener?.onAdLoaded(); heightController.onAdLoaded(view.adFormat.size.height) },
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
 * Creates AppLovin ad properties for Android ad unit IDs.
 */
fun appLovinAdProperties(
    adUnitId: String,
    tag: String? = null
): AppLovinAdProperties {
    return AppLovinAdProperties(adUnitId, "", tag)
}
