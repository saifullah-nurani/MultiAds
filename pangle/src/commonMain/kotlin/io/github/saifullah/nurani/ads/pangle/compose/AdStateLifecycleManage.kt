package io.github.saifullah.nurani.ads.pangle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import io.github.saifullah.nurani.ads.pangle.FullScreenAdState
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

    /**
     * Manage ad initialization and cleanup.
     */
    DisposableEffect(adUnitId) {
        if (initialLoad && !adState.isAdAvailable && !adState.isAdLoading) {
            adState.loadAd()
        }
        adState.setAdLoadCallback(adLoadCallback)
        adState.setAdContentCallback(adContentCallback)
        adState.isImmersiveModeEnabled = immersiveModeEnabled
        onDispose {
            adState.onDestroy()
        }
    }
}
