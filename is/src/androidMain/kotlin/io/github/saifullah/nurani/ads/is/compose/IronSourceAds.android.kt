package io.github.saifullah.nurani.ads.`is`.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.`is`.IronSourceAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberIronSourceAdsInit(
    androidAppKey: String,
    iosAppKey: String,
    testModeEnabled: Boolean,
    context: PlatformContext
): Boolean {
    val appKey = if (testModeEnabled) IronSourceAds.TEST_APP_KEY else androidAppKey
    var isInitialized by remember(appKey) { mutableStateOf(false) }
    LaunchedEffect(appKey, context) {
        IronSourceAds.init(context, appKey) { success ->
            isInitialized = success
        }
    }
    return isInitialized
}
