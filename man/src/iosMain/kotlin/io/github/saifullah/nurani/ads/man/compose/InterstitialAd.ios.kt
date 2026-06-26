package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.man.MetaFullScreenAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

import androidx.compose.runtime.remember
import io.github.saifullah.nurani.ads.man.MetaInterstitialAd
import io.github.saifullah.nurani.ads.core.adConfig
import kotlinx.cinterop.ExperimentalForeignApi

@Composable
actual fun rememberMetaInterstitialAd(
    properties: MetaAdProperties,
    initialLoad: Boolean,
    immersiveModeEnabled: Boolean,
    testModeEnabled: Boolean,
    context: PlatformContext,
    adFailedAdRetryRule: AdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy>,
    adLogger: AdLogger?,
    adLoadCallback: AdLoadCallback?,
    adContentCallback: AdContentCallback?
): MetaFullScreenAdState {
    val adState = remember(properties.iosPlacementId) {
        @OptIn(ExperimentalForeignApi::class)
        MetaInterstitialAd(
            placementId = properties.iosPlacementId,
            uIViewController = null,
            adConfig = adConfig {
                this.adLogger = adLogger
                this.adFailedRetryRule = adFailedAdRetryRule
                this.adRefreshStrategy = adRefreshStrategy
                this.adReloadPolicies = adReloadPolicies
                this.tag = properties.tag
                this.isTestModeEnabled = testModeEnabled
            }
        )
    }

    AdStateLifecycleManage(
        placementId = properties.iosPlacementId,
        initialLoad = initialLoad,
        immersiveModeEnabled = immersiveModeEnabled,
        adState = adState,
        adLoadCallback = adLoadCallback,
        adContentCallback = adContentCallback
    )
    return adState
}
