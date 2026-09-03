package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import io.github.saifullah.nurani.ads.admob.FullScreenAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdLoadCallback

@Composable
internal fun AdStateLifecycleManage(
    adUnitId: String,
    initialLoad: Boolean,
    immersiveModeEnabled: Boolean,
    adState: FullScreenAdState,
    adLoadCallback: AdLoadCallback?,
    adContentCallback: AdContentCallback?,
) {
    LifecycleStartEffect(adUnitId) {
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
    DisposableEffect(adUnitId) {
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
