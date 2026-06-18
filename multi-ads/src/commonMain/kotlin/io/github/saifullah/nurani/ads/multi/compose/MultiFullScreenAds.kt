package io.github.saifullah.nurani.ads.multi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.multi.MultiInterstitialAd
import io.github.saifullah.nurani.ads.multi.MultiRewardedAd
import io.github.saifullah.nurani.ads.multi.MultiAppOpenAd
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

@Composable
fun rememberMultiInterstitialAd(
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean = false,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null
): MultiInterstitialAd {
    val context = LocalPlatformContext.current
    val adState = remember(waterfallConfig) {
        MultiInterstitialAd(context).apply {
            this.waterfallConfig = waterfallConfig
            this.testModeEnabled = testModeEnabled
            this.isImmersiveModeEnabled = immersiveModeEnabled
        }
    }

    DisposableEffect(adState) {
        adState.setAdLoadCallback(adLoadCallback)
        adState.setAdContentCallback(adContentCallback)
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
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean = false,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
    onUserRewarded: (() -> Unit)? = null
): MultiRewardedAd {
    val context = LocalPlatformContext.current
    val adState = remember(waterfallConfig) {
        MultiRewardedAd(context).apply {
            this.waterfallConfig = waterfallConfig
            this.testModeEnabled = testModeEnabled
            this.isImmersiveModeEnabled = immersiveModeEnabled
        }
    }

    DisposableEffect(adState) {
        adState.setAdLoadCallback(adLoadCallback)
        adState.setAdContentCallback(adContentCallback)
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
fun rememberMultiAppOpenAd(
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean = false,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null
): MultiAppOpenAd {
    val context = LocalPlatformContext.current
    val adState = remember(waterfallConfig) {
        MultiAppOpenAd(context).apply {
            this.waterfallConfig = waterfallConfig
            this.testModeEnabled = testModeEnabled
            this.isImmersiveModeEnabled = immersiveModeEnabled
        }
    }

    DisposableEffect(adState) {
        adState.setAdLoadCallback(adLoadCallback)
        adState.setAdContentCallback(adContentCallback)
        if (initialLoad && !adState.isAdAvailable && !adState.isAdLoading) {
            adState.loadAd()
        }
        onDispose {
            adState.destroy()
        }
    }

    return adState
}

