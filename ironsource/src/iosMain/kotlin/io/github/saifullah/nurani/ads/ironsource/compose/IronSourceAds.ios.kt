package io.github.saifullah.nurani.ads.ironsource.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.ironsource.IronSourceAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberIronSourceAdsInit(
    androidAppKey: String,
    iosAppKey: String,
    testModeEnabled: Boolean,
    context: PlatformContext
): Boolean {
    val appKey = if (testModeEnabled) IronSourceAds.TEST_APP_KEY else iosAppKey
    var isInitialized by remember(appKey) { mutableStateOf(false) }
    LaunchedEffect(appKey) {
        IronSourceAds.init(context, androidAppKey, iosAppKey) { result: AdInitResult ->
            isInitialized = result.success
        }
    }
    return isInitialized
}
