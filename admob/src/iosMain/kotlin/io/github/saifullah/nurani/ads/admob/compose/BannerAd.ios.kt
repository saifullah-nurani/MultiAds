package io.github.saifullah.nurani.ads.admob.compose

import GoogleMobileAds.GADMobileAds
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.github.saifullah.nurani.ads.admob.AdmobBannerUIView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.rememberBannerHeightController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents


@OptIn(ExperimentalForeignApi::class)
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
    val heightController = rememberBannerHeightController(
        initialHeight = adSize.getSize().height,
        expandWhenReady = expandWhenReady,
        animateExpansion = animateExpansion
    )
    val adUnit by rememberSaveable(properties.iosAdUnitId) {
        mutableStateOf(properties.iosAdUnitId)
    }
    val initialLoadRequested = remember { booleanArrayOf(false) }
    UIKitView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightController.animatedHeight()),

        factory = {
            GADMobileAds.sharedInstance().startWithCompletionHandler(null)
            AdmobBannerUIView().apply {
                logger = adLogger
                this.isTestModeEnabled = testModeEnabled
                this.retryRule = adFailedAdRetryRule
                this.keepAdSlot = expandWhenReady
                setRequestTag(properties.tag)
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
                    view.adSize.useContents {
                        heightController.onAdLoaded(this.size.height.toInt())
                    }
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
