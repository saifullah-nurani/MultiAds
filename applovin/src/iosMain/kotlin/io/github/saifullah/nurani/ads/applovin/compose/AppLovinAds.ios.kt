package io.github.saifullah.nurani.ads.applovin.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.applovin.AppLovinAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberAppLovinAdsInit(
    androidSdkKey: String,
    iosSdkKey: String,
    context: PlatformContext
): Boolean {
    var isInitialized by remember(iosSdkKey) { mutableStateOf(false) }
    LaunchedEffect(iosSdkKey) {
        AppLovinAds.init(context, iosSdkKey) { success ->
            isInitialized = success
        }
    }
    return isInitialized
}
