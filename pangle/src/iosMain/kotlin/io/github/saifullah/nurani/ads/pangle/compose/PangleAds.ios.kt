package io.github.saifullah.nurani.ads.pangle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.pangle.PangleAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberPangleAdsInit(
    androidAppId: String,
    iosAppId: String,
    context: PlatformContext
): Boolean {
    var isInitialized by remember(iosAppId) { mutableStateOf(false) }
    LaunchedEffect(iosAppId) {
        PangleAds.init(context, androidAppId, iosAppId) { result: AdInitResult ->
            isInitialized = result.success
        }
    }
    return isInitialized
}
