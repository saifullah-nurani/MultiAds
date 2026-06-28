package io.github.saifullah.nurani.ads.multi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.multi.MultiInterstitialAd
import io.github.saifullah.nurani.ads.multi.MultiRewardedAd
import io.github.saifullah.nurani.ads.multi.models.MultiAdContentCallback
import io.github.saifullah.nurani.ads.multi.models.MultiAdLoadCallback
import io.github.saifullah.nurani.ads.multi.models.MultiAdsConfig
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

@Composable
fun rememberMultiInterstitialAd(
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean = false,
    tag: String? = null,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
    multiAdLoadCallback: MultiAdLoadCallback? = null,
    multiAdContentCallback: MultiAdContentCallback? = null
): MultiInterstitialAd {
    val context = LocalPlatformContext.current
    val adState = remember(waterfallConfig) {
        MultiInterstitialAd(context).apply {
            this.waterfallConfig = waterfallConfig
            this.testModeEnabled = testModeEnabled
            this.tag = tag
            this.isImmersiveModeEnabled = immersiveModeEnabled
        }
    }

    DisposableEffect(adState) {
        adState.setAdLoadCallback(adLoadCallback)
        adState.setAdContentCallback(adContentCallback)
        adState.setMultiAdLoadCallback(multiAdLoadCallback)
        adState.setMultiAdContentCallback(multiAdContentCallback)
        if (initialLoad && !adState.isAdAvailable && !adState.isAdLoading) {
            adState.loadAd()
        }
        onDispose {
            adState.destroy()
        }
    }

    return adState
}

@Composable
fun rememberMultiInterstitialAd(
    multiAdsConfig: MultiAdsConfig,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
    multiAdLoadCallback: MultiAdLoadCallback? = null,
    multiAdContentCallback: MultiAdContentCallback? = null
): MultiInterstitialAd {
    return rememberMultiInterstitialAd(
        waterfallConfig = multiAdsConfig.waterfallConfig ?: error("waterfallConfig is required"),
        testModeEnabled = multiAdsConfig.adConfig.isTestModeEnabled,
        tag = multiAdsConfig.adConfig.tag,
        initialLoad = initialLoad,
        immersiveModeEnabled = immersiveModeEnabled,
        adLoadCallback = adLoadCallback,
        adContentCallback = adContentCallback,
        multiAdLoadCallback = multiAdLoadCallback,
        multiAdContentCallback = multiAdContentCallback
    )
}

@Composable
fun rememberMultiRewardedAd(
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean = false,
    tag: String? = null,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
    multiAdLoadCallback: MultiAdLoadCallback? = null,
    multiAdContentCallback: MultiAdContentCallback? = null,
    onUserRewarded: (() -> Unit)? = null
): MultiRewardedAd {
    val context = LocalPlatformContext.current
    val adState = remember(waterfallConfig) {
        MultiRewardedAd(context).apply {
            this.waterfallConfig = waterfallConfig
            this.testModeEnabled = testModeEnabled
            this.tag = tag
            this.isImmersiveModeEnabled = immersiveModeEnabled
        }
    }

    DisposableEffect(adState) {
        adState.setAdLoadCallback(adLoadCallback)
        adState.setAdContentCallback(adContentCallback)
        adState.setMultiAdLoadCallback(multiAdLoadCallback)
        adState.setMultiAdContentCallback(multiAdContentCallback)
        if (onUserRewarded != null) {
            adState.setOnUserRewarded(onUserRewarded)
        }
        if (initialLoad && !adState.isAdAvailable && !adState.isAdLoading) {
            adState.loadAd()
        }
        onDispose {
            adState.destroy()
        }
    }

    return adState
}

@Composable
fun rememberMultiRewardedAd(
    multiAdsConfig: MultiAdsConfig,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
    multiAdLoadCallback: MultiAdLoadCallback? = null,
    multiAdContentCallback: MultiAdContentCallback? = null,
    onUserRewarded: (() -> Unit)? = null
): MultiRewardedAd {
    return rememberMultiRewardedAd(
        waterfallConfig = multiAdsConfig.waterfallConfig ?: error("waterfallConfig is required"),
        testModeEnabled = multiAdsConfig.adConfig.isTestModeEnabled,
        tag = multiAdsConfig.adConfig.tag,
        initialLoad = initialLoad,
        immersiveModeEnabled = immersiveModeEnabled,
        adLoadCallback = adLoadCallback,
        adContentCallback = adContentCallback,
        multiAdLoadCallback = multiAdLoadCallback,
        multiAdContentCallback = multiAdContentCallback,
        onUserRewarded = onUserRewarded
    )
}
