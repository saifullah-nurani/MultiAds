package io.github.saifullah.nurani.ads.admob.compose

import android.content.Context
import android.os.Handler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import io.github.saifullah.nurani.ads.admob.AdmobAppOpenAd
import io.github.saifullah.nurani.ads.admob.FullScreenAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberAdmobAppOpenAd(
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
): FullScreenAdState = rememberAdmobAppOpenAd(
    adUnitId = properties.androidAdUnitId,
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

@Composable
fun rememberAdmobAppOpenAd(
    adUnitId: String,
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
        AdmobAppOpenAd(
            context = context,
            adRequest = adRequest,
            adUnitId = adUnitId,
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
