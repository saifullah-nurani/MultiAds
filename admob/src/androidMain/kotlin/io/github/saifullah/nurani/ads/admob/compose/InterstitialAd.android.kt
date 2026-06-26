package io.github.saifullah.nurani.ads.admob.compose

import android.content.Context
import android.os.Handler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import io.github.saifullah.nurani.ads.admob.AdmobInterstitialAd
import io.github.saifullah.nurani.ads.admob.FullScreenAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

/**
 * Remembers and manages the lifecycle of a interstitial ad.
 *
 * This function integrates with Compose lifecycle to automatically:
 * - start and stop the ad state
 * - load the ad initially if required
 * - attach load/content callbacks
 */
@Composable
actual fun rememberAdmobInterstitialAd(
    properties: AdmobAdProperties,
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
): FullScreenAdState = rememberAdmobInterstitialAd(
    adUnitId = properties.androidAdUnitId,
    tag = properties.tag,
    initialLoad = initialLoad,
    immersiveModeEnabled = immersiveModeEnabled,
    testModeEnabled = testModeEnabled,
    context = context,
    adRequest = null,
    handler = null,
    adFailedAdRetryRule = adFailedAdRetryRule,
    adRefreshStrategy = adRefreshStrategy,
    adReloadPolicies = adReloadPolicies,
    adLogger = adLogger,
    adLoadCallback = adLoadCallback,
    adContentCallback = adContentCallback
)

/**
 * Creates and remembers an [AdmobInterstitialAd] instance and binds
 * it to the Compose lifecycle.
 */
@Composable
fun rememberAdmobInterstitialAd(
    adUnitId: String,
    tag: String? = null,
    initialLoad: Boolean = true,
    immersiveModeEnabled: Boolean = true,
    testModeEnabled: Boolean = false,
    context: Context = LocalContext.current,
    adRequest: AdRequest? = null,
    handler: Handler? = null,
    adFailedAdRetryRule: AdFailedRetryRule = AdmobDefault.DefaultAdFailedRetryRule,
    adRefreshStrategy: AdRefreshStrategy = AdmobDefault.DefaultAdRefreshStrategy,
    adReloadPolicies: Set<AdReloadPolicy> = AdmobDefault.DefaultAdReloadPolicy,
    adLogger: AdLogger? = null,
    adLoadCallback: AdLoadCallback? = null,
    adContentCallback: AdContentCallback? = null,
): FullScreenAdState {
    val adState = remember(adUnitId) {
        AdmobInterstitialAd(
            context = context,
            adRequest = adRequest,
            adUnitId = adUnitId,
            handler = handler,
            adConfig = adConfig {
                this.adLogger = adLogger
                this.adFailedRetryRule = adFailedAdRetryRule
                this.adRefreshStrategy = adRefreshStrategy
                this.adReloadPolicies = adReloadPolicies
                this.tag = tag
                this.isTestModeEnabled = testModeEnabled
            },
        )
    }

    /**
     * Connect ad lifecycle with Compose lifecycle.
     */
    AdStateLifecycleManage(
        adUnitId = adUnitId,
        initialLoad = initialLoad,
        immersiveModeEnabled = immersiveModeEnabled,
        adState = adState,
        adLoadCallback = adLoadCallback,
        adContentCallback = adContentCallback
    )
    return adState
}
