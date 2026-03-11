package io.github.saifullah.nurani.ads.admob.compose

import GoogleMobileAds.GADMobileAds
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import io.github.saifullah.nurani.ads.admob.AdmobBannerUIView
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
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
    adReloadPolicies: Set<AdReloadPolicy>,
    adLogger: AdLogger?,
    adListener: BannerAdListener?
) {
    val heightController = rememberBannerHeightController(
        expandWhenReady,
        animateExpansion
    )
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
                setAdUnitId(properties.iosAdUnitId)
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
            view.loadAd()
        },

        onRelease = { it.destroy() }
    )
}
