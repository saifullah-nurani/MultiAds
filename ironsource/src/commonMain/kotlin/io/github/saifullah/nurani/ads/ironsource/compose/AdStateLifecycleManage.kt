package io.github.saifullah.nurani.ads.ironsource.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import io.github.saifullah.nurani.ads.ironsource.FullScreenAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdLoadCallback

@Composable
internal fun AdStateLifecycleManage(
    placementName: String?,
    initialLoad: Boolean,
    immersiveModeEnabled: Boolean,
    adState: FullScreenAdState,
    adLoadCallback: AdLoadCallback?,
    adContentCallback: AdContentCallback?,
) {
    // using a unique key per lifecycle
    val lifecycleKey = placementName ?: "default_iron_source_placement"
    LifecycleStartEffect(lifecycleKey) {
        adState.onStart()
        onStopOrDispose {
            adState.onStop()
        }
    }

    SideEffect {
        adState.setAdLoadCallback(adLoadCallback)
        adState.setAdContentCallback(adContentCallback)
        adState.isImmersiveModeEnabled = immersiveModeEnabled
    }

    /**
     * Manage ad initialization and cleanup.
     */
    DisposableEffect(lifecycleKey) {
        adState.setAdLoadCallback(adLoadCallback)
        adState.setAdContentCallback(adContentCallback)
        adState.isImmersiveModeEnabled = immersiveModeEnabled
        if (initialLoad && !adState.isAdAvailable && !adState.isAdLoading) {
            adState.loadAd()
        }
        onDispose {
            adState.onDestroy()
        }
    }
}
