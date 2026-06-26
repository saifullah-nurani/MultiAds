package io.github.saifullah.nurani.ads.vungle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.vungle.VungleAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberVungleAdsInit(
    androidAppId: String,
    iosAppId: String,
    context: PlatformContext
): Boolean {
    var isInitialized by remember(iosAppId) { mutableStateOf(false) }
    LaunchedEffect(iosAppId) {
        VungleAds.init(context, androidAppId, iosAppId) { result: AdInitResult ->
            isInitialized = result.success
        }
    }
    return isInitialized
}
