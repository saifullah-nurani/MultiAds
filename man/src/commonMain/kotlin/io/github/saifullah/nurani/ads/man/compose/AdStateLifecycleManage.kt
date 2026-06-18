package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import io.github.saifullah.nurani.ads.man.MetaFullScreenAdState
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdLoadCallback

@Composable
internal fun AdStateLifecycleManage(
    placementId: String,
    initialLoad: Boolean,
    immersiveModeEnabled: Boolean,
    adState: MetaFullScreenAdState,
    adLoadCallback: AdLoadCallback?,
    adContentCallback: AdContentCallback?,
) {
    LifecycleStartEffect(placementId) {
        adState.onStart()
        onStopOrDispose {
            adState.onStop()
        }
    }

    /**
     * Manage ad initialization and cleanup.
     */
    DisposableEffect(placementId) {
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
