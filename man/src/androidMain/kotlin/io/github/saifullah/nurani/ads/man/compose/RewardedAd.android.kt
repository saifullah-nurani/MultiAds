package io.github.saifullah.nurani.ads.man.compose

import android.content.Context
import android.os.Handler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.saifullah.nurani.ads.man.MetaRewardedAd
import io.github.saifullah.nurani.ads.man.MetaRewardedAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

/**
 * Remembers and manages the lifecycle of a Meta rewarded ad.
 */
@Composable
actual fun rememberMetaRewardedAd(
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
): MetaRewardedAdState = rememberMetaRewardedAd(
    placementId = properties.androidPlacementId,
    initialLoad = initialLoad,
    immersiveModeEnabled = immersiveModeEnabled,
    testModeEnabled = testModeEnabled,
    context = context,
    handler = null,
    adFailedAdRetryRule = adFailedAdRetryRule,
    adRefreshStrategy = adRefreshStrategy,
    adReloadPolicies = adReloadPolicies,
    adLogger = adLogger,
    adLoadCallback = adLoadCallback,
    adContentCallback = adContentCallback
)

/**
 * Creates and remembers a [MetaRewardedAd] instance and binds
 * it to the Compose lifecycle.
 */
@Composable
fun rememberMetaRewardedAd(
    placementId: String,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    testModeEnabled: Boolean = false,
    context: Context = LocalContext.current,
    handler: Handler? = null,
    adFailedAdRetryRule: AdFailedRetryRule = MetaDefault.DefaultAdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy = MetaDefault.DefaultAdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy> = MetaDefault.DefaultAdReloadPolicy,
    adLogger: AdLogger? = null,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
): MetaRewardedAdState {
    val adState = remember(placementId) {
        MetaRewardedAd(
            context = context,
            placementId = placementId,
            handler = handler,
            adConfig = adConfig {
                this.adLogger = adLogger
                this.adFailedRetryRule = adFailedAdRetryRule
                this.adRefreshStrategy = adRefreshStrategy
                this.adReloadPolicies = adReloadPolicies
                this.isTestModeEnabled = testModeEnabled
            },
        )
    }

    /**
     * Connect ad lifecycle with Compose lifecycle.
     */
    AdStateLifecycleManage(
        placementId = placementId,
        initialLoad = initialLoad,
        immersiveModeEnabled = immersiveModeEnabled,
        adState = adState,
        adLoadCallback = adLoadCallback,
        adContentCallback = adContentCallback
    )
    return adState
}
