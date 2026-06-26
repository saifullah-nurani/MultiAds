package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.man.MetaAds

@Composable
actual fun rememberMetaAdsInit(androidPlacementId: String, iosPlacementId: String, context: PlatformContext): Boolean {
    var isInitialized by remember(androidPlacementId) { mutableStateOf(false) }
    LaunchedEffect(androidPlacementId, context) {
        MetaAds.init(context, androidPlacementId, iosPlacementId) { result: AdInitResult ->
            isInitialized = result.success
        }
    }
    return isInitialized
}
