package io.github.saifullah.nurani.ads.multi.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.rememberBannerHeightController
import io.github.saifullah.nurani.ads.multi.MultiBannerView
import io.github.saifullah.nurani.ads.multi.models.MultiAdListener
import io.github.saifullah.nurani.ads.multi.models.MultiAdsConfig
import io.github.saifullah.nurani.ads.multi.models.MultiBannerAdConfig
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

@Composable
actual fun MultiBannerAd(
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean,
    expandWhenReady: Boolean,
    animateExpansion: Boolean,
    adSize: BannerAd<AdSize>,
    adFailedAdRetryRule: AdFailedRetryRule,
    adLogger: AdLogger?,
    adListener: MultiAdListener?,
    tag: String?
) {
    MultiBannerAd(
        waterfallConfig = waterfallConfig,
        config = MultiBannerAdConfig(
            testModeEnabled = testModeEnabled,
            expandWhenReady = expandWhenReady,
            animateExpansion = animateExpansion,
            adSize = adSize,
            adFailedAdRetryRule = adFailedAdRetryRule,
            adLogger = adLogger,
            adListener = null,
            tag = tag
        ),
        adListener = adListener
    )
}

@Composable
actual fun MultiBannerAd(
    waterfallConfig: WaterfallConfig,
    config: MultiBannerAdConfig,
    adListener: MultiAdListener?
) {
    val heightController = rememberBannerHeightController(
        initialHeight = config.adSize.getSize().height,
        expandWhenReady = config.expandWhenReady,
        animateExpansion = config.animateExpansion
    )
    val bannerAdListener = remember(config, heightController, adListener) {
        BannerAdListener(
            onAdShowed = { config.adListener?.onAdShowed() },
            onAdDismissed = { config.adListener?.onAdDismissed() },
            onAdLoaded = {
                config.adListener?.onAdLoaded()
                heightController.onAdLoaded(config.adSize.getSize().height)
            },
            onAdClicked = { config.adListener?.onAdClicked() },
            onAdFailedToLoad = { config.adListener?.onAdFailedToLoad(it) },
            onAdFailedToShow = { config.adListener?.onAdFailedToShow(it) },
            onAdDisplayed = {
                config.adListener?.onAdDisplayed()
                heightController.onAdDisplayed()
            }
        )
    }
    val heightDp = config.adSize.getSize().height
    val modifier = if (heightDp > 0) {
        Modifier.fillMaxWidth().height(heightController.animatedHeight())
    } else {
        Modifier.fillMaxWidth()
    }
    val initialLoadRequested = remember { booleanArrayOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MultiBannerView(context).apply {
                setWaterfallConfig(waterfallConfig)
                setTestModeEnabled(config.testModeEnabled)
                setAdFailedAdRetryRule(config.adFailedAdRetryRule)
                setAdLogger(config.adLogger)
                setRequestTag(config.tag)
                setBannerAd(config.adSize)
                setAdListener(bannerAdListener)
                setMultiAdListener(adListener)
                loadAd()
            }
        },
        update = { view ->
            view.setWaterfallConfig(waterfallConfig)
            view.setTestModeEnabled(config.testModeEnabled)
            view.setAdFailedAdRetryRule(config.adFailedAdRetryRule)
            view.setAdLogger(config.adLogger)
            view.setRequestTag(config.tag)
            view.setBannerAd(config.adSize)
            view.setAdListener(bannerAdListener)
            view.setMultiAdListener(adListener)
            if (!initialLoadRequested[0]) {
                initialLoadRequested[0] = true
                view.loadAd()
            }
        },
        onRelease = { view ->
            view.destroy()
        }
    )
}

@Composable
actual fun MultiBannerAd(
    multiAdsConfig: MultiAdsConfig,
    config: MultiBannerAdConfig,
    adListener: MultiAdListener?
) {
    val mergedConfig = config.copy(
        testModeEnabled = multiAdsConfig.adConfig.isTestModeEnabled || config.testModeEnabled,
        adLogger = config.adLogger ?: multiAdsConfig.adConfig.adLogger,
        tag = config.tag ?: multiAdsConfig.adConfig.tag
    )
    MultiBannerAd(
        waterfallConfig = multiAdsConfig.waterfallConfig ?: error("waterfallConfig is required"),
        config = mergedConfig,
        adListener = adListener
    )
}
